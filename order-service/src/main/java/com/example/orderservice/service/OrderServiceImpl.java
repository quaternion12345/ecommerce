package com.example.orderservice.service;

import com.example.orderservice.client.CatalogServiceClient;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.exception.BadRequestException;
import com.example.orderservice.exception.NotFoundException;
import com.example.orderservice.jpa.MongoOrderEntity;
import com.example.orderservice.jpa.MongoOrderRepository;
import com.example.orderservice.jpa.MySQLOrderEntity;
import com.example.orderservice.jpa.MySQLOrderRepository;
import com.example.orderservice.vo.ResponseCatalog;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService{
    MySQLOrderRepository mySQLOrderRepository;
    MongoOrderRepository mongoOrderRepository;
    CatalogServiceClient catalogServiceClient;
    CircuitBreakerFactory circuitBreakerFactory;
    KafkaProducer kafkaProducer;

    @Autowired
    public OrderServiceImpl(MySQLOrderRepository mySQLOrderRepository, MongoOrderRepository mongoOrderRepository, CatalogServiceClient catalogServiceClient, CircuitBreakerFactory circuitBreakerFactory, KafkaProducer kafkaProducer) {
        this.mySQLOrderRepository = mySQLOrderRepository;
        this.mongoOrderRepository = mongoOrderRepository;
        this.catalogServiceClient = catalogServiceClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
        // Generate orderId
        orderDto.setOrderId(UUID.randomUUID().toString());

        // Calculate Total Price
        // Get data from catalog service
        log.info("Before call catalogs microservice");
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
        ResponseCatalog responseCatalog = circuitBreaker.run(() -> catalogServiceClient.getCatalog(orderDto.getProductId()),
                throwable -> new ResponseCatalog());
        log.info("After call catalogs microservice");

        if(responseCatalog.getUnitPrice() == null || responseCatalog.getStock() == null) throw new NotFoundException("Product with productId: " +orderDto.getProductId());
        if(responseCatalog.getStock() < orderDto.getQty()) throw new BadRequestException("You cannot order more products than stocks we have");

        orderDto.setUnitPrice(responseCatalog.getUnitPrice());
        orderDto.setTotalPrice(orderDto.getQty() * orderDto.getUnitPrice());

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        MySQLOrderEntity mySQLOrderEntity = MySQLOrderEntity.builder()
                .productId(orderDto.getProductId())
                .qty(orderDto.getQty())
                .unitPrice(orderDto.getUnitPrice())
                .totalPrice(orderDto.getTotalPrice())
                .userId(orderDto.getUserId())
                .orderId(orderDto.getOrderId())
                .build();

        return new ModelMapper().map(mySQLOrderRepository.save(mySQLOrderEntity), OrderDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<OrderDto> getOrdersByUserId(String userId) {
        return StreamSupport.stream(mongoOrderRepository.findByUserId(userId).spliterator(), false)
                .filter(h -> h.isValid())
                .map(p -> new ModelMapper().map(p, OrderDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteOrdersByUserId(String userId) {
        List<OrderDto> orderDtoList = new ArrayList<>();

        // userId로 된 주문 내역 전부 취소 -> Soft Delete로 개선
        StreamSupport.stream(mySQLOrderRepository.findByUserId(userId).spliterator(), false)
                .filter(h -> h.isValid())
                        .forEach(p -> {
                            OrderDto orderDto = new OrderDto();
                            orderDto.setUserId(userId);
                            orderDto.setOrderId(p.getOrderId());
                            orderDto.setProductId(p.getProductId());
                            orderDto.setQty(p.getQty());

                            p.deleteOrder();

                            orderDtoList.add(orderDto);
                        });

        // Catalog Service에 재고 업데이트 Event 전송
        kafkaProducer.send("delete-orders", orderDtoList); // order -> catalog
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderId(String orderId) {
        MongoOrderEntity mongoOrderEntity = mongoOrderRepository.findByOrderId(orderId).orElseThrow(() -> new NotFoundException("orderId: " + orderId));
        if(!mongoOrderEntity.isValid()) throw new NotFoundException("orderId: " + orderId);

        OrderDto orderDto = new ModelMapper().map(mongoOrderEntity, OrderDto.class);

        return orderDto;
    }

    @Override
    @Transactional
    public OrderDto updateOrderByOrderId(OrderDto orderDto) {
        MySQLOrderEntity mySQLOrderEntity = mySQLOrderRepository.findByOrderId(orderDto.getOrderId()).orElseThrow(() -> new NotFoundException("orderId: " + orderDto.getOrderId()));
        if(!mySQLOrderEntity.isValid()) throw new NotFoundException("orderId: " + orderDto.getOrderId());

        OrderDto dto = new OrderDto();
        dto.setOrderId(mySQLOrderEntity.getOrderId());
        dto.setProductId(mySQLOrderEntity.getProductId());
        dto.setQty(mySQLOrderEntity.getQty() - orderDto.getQty()); // 변화량만큼 재고에 반대로 반영

        // 수정 수행
        orderDto.setUnitPrice(mySQLOrderEntity.getUnitPrice());
        mySQLOrderEntity.updateOrder(orderDto);

        // Catalog Service에 재고 업데이트 Event 전송
        kafkaProducer.send("update-order", dto); // order -> catalog

        return new ModelMapper().map(mySQLOrderEntity, OrderDto.class);
    }

    @Override
    @Transactional
    public void deleteOrderByOrderId(String orderId) {
        MySQLOrderEntity mySQLOrderEntity = mySQLOrderRepository.findByOrderId(orderId).orElseThrow(() -> new NotFoundException("orderId: " + orderId));
        if(!mySQLOrderEntity.isValid()) throw new NotFoundException("orderId: " + orderId);
        mySQLOrderEntity.deleteOrder();

        // Catalog Service에 재고 업데이트 Event 전송
        OrderDto orderDto = new OrderDto();
        orderDto.setOrderId(orderId);
        orderDto.setProductId(mySQLOrderEntity.getProductId());
        orderDto.setQty(mySQLOrderEntity.getQty());

        kafkaProducer.send("delete-order", orderDto); // order -> catalog
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Transactional
    public void cleanUp(){
        StreamSupport.stream(mySQLOrderRepository.findAll().spliterator(), false)
                .filter(h -> !h.isValid())
                .forEach(p -> mySQLOrderRepository.delete(p));
    }
}
