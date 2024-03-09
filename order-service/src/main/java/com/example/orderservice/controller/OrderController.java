package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.RequestUpdateOrder;
import com.example.orderservice.vo.ResponseOrder;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
@Slf4j
public class OrderController {
    Environment env;
    OrderService orderService;
//    KafkaProducer kafkaProducer;
//    OrderProducer orderProducer;

    @Autowired
    public OrderController(Environment env, OrderService orderService) {
        this.env = env;
        this.orderService = orderService;
//        this.kafkaProducer = kafkaProducer;
//        this.orderProducer = orderProducer;
    }

    @GetMapping("/health_check")
    public String status(){
        return String.format("It's Working in Order Service on PORT %s", env.getProperty("local.server.port"));
    }

    /* 상품 주문 */
    @PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createOrder(@PathVariable("userId") String userId, @RequestBody @Valid RequestOrder order){
        log.info("Before add orders data");
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderDto orderDto = mapper.map(order, OrderDto.class);
        orderDto.setUserId(userId);

        /* JPA */
        OrderDto createdOrder = orderService.createOrder(orderDto);
        ResponseOrder responseOrder = mapper.map(createdOrder, ResponseOrder.class);

        /* Kafka */
//        orderDto.setOrderId(UUID.randomUUID().toString());
//        orderDto.setTotalPrice(order.getQty() * order.getUnitPrice());

        /* Send this order to the kafka */
        ////kafkaProducer.send("example-catalog-topic", orderDto);
//        orderProducer.send("orders", orderDto);

//        ResponseOrder responseOrder = mapper.map(orderDto, ResponseOrder.class);
        log.info("After add orders data");

        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }

    /* userId로 주문 정보 조회 */
    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> getOrders(@PathVariable("userId") String userId){
        log.info("Before retrieve orders data");
        Iterable<OrderDto> orderList = orderService.getOrdersByUserId(userId);

        List<ResponseOrder> result = new ArrayList<>();

        orderList.forEach(v -> {
            result.add(new ModelMapper().map(v, ResponseOrder.class));
        });

        log.info("After retrieve orders data");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /* userId로 주문 취소 */
    @DeleteMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> deleteOrders(@PathVariable("userId") String userId){
        orderService.deleteOrdersByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /* orderId로 주문 정보 조회 */
    @GetMapping("/{orderId}/order")
    public ResponseEntity<ResponseOrder> getOrder(@PathVariable("orderId") String orderId){
        OrderDto orderDto = orderService.getOrderByOrderId(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(new ModelMapper().map(orderDto, ResponseOrder.class));
    }

    /* orderId로 주문 수정 */
    @PatchMapping("/{orderId}/order")
    public ResponseEntity<ResponseOrder> updateOrder(@PathVariable("orderId") String orderId, @RequestBody @Valid RequestUpdateOrder requestOrder){
        ModelMapper mapper = new ModelMapper();
        OrderDto orderDto = mapper.map(requestOrder, OrderDto.class);
        orderDto.setOrderId(orderId);

        OrderDto updatedOrder = orderService.updateOrderByOrderId(orderDto);

        ResponseOrder responseOrder = mapper.map(updatedOrder, ResponseOrder.class);
        return ResponseEntity.status(HttpStatus.OK).body(responseOrder);
    }

    /* orderId로 주문 삭제 */
    @DeleteMapping("/{orderId}/order")
    public ResponseEntity<ResponseOrder> deleteOrder(@PathVariable("orderId") String orderId){
        orderService.deleteOrderByOrderId(orderId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
