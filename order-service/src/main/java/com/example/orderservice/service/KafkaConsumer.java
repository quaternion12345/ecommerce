package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.exception.NotFoundException;
import com.example.orderservice.jpa.MySQLOrderEntity;
import com.example.orderservice.jpa.MySQLOrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class KafkaConsumer {
    MySQLOrderRepository mySQLOrderRepository;
    OrderService orderService;
    KafkaTemplate kafkaTemplate;

    @Autowired
    public KafkaConsumer(MySQLOrderRepository mySQLOrderRepository, OrderService orderService, KafkaTemplate kafkaTemplate) {
        this.mySQLOrderRepository = mySQLOrderRepository;
        this.orderService = orderService;
        this.kafkaTemplate = kafkaTemplate;
    }

    // userId로 delete orders Event
    @KafkaListener(topics = "delete-user", groupId = "group-order")
    public void deleteOrders(String userId){
        try {
            log.info("{} Delete User event occurred", userId);
            orderService.deleteOrdersByUserId(userId); // order -> catalog
        }catch (Exception e){
            log.info("{} Delete User event failed", userId);
            kafkaTemplate.send("rollback-user", userId); // order -> user
            log.info("{} Rollback User event sent", userId);
        }
    }

    // userId로 delete orders rollback Event
    @KafkaListener(topics = "rollback-delete-orders", groupId = "group-order")
    public void rollbackOrders(String userId){
        log.info("{} Rollback event occurred", userId);
        mySQLOrderRepository.findByUserId(userId).forEach(
                p -> p.rollbackOrder()
        );
        kafkaTemplate.send("rollback-user", userId); // order -> user
        log.info("{} Rollback User event sent", userId);
    }

    // orderId로 delete order rollback Event
    @KafkaListener(topics = "rollback-delete-order", groupId = "group-order")
    public void rollbackDeleteOrder(String orderId){
        log.info("{} Rollback Delete Order event occurred", orderId);
        mySQLOrderRepository.findByOrderId(orderId).orElseThrow(() -> new NotFoundException("orderId: " + orderId)).rollbackOrder();
        log.info("{} Rollback Delete Order event finished", orderId);
    }

    // orderId로 update order rollback Event
    @KafkaListener(topics = "rollback-update-order", groupId = "group-order")
    @Transactional
    public void rollbackUpdateOrder(String message){
        log.info("Rollback Update Order event occurred");

        OrderDto orderDto = null;
        try{
            orderDto = new ObjectMapper().readValue(message, OrderDto.class);
        }catch(JsonProcessingException e){
            e.printStackTrace();
        }
        String orderId = orderDto.getOrderId();

        log.info("{} Rollback Update Order event successfully deserialized", orderId);
        MySQLOrderEntity mySQLOrderEntity = mySQLOrderRepository.findByOrderId(orderId).orElseThrow(() -> new NotFoundException("orderId: " + orderId));
        if(!mySQLOrderEntity.isValid()) throw new NotFoundException("orderId: " + orderId);

        orderDto.setQty(mySQLOrderEntity.getQty() + orderDto.getQty());
        orderDto.setUnitPrice(mySQLOrderEntity.getUnitPrice());
        mySQLOrderEntity.updateOrder(orderDto);

        log.info("{} Rollback Update Order event finished", orderId);
    }
}
