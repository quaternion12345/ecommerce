package com.example.catalogservice.service;

import com.example.catalogservice.dto.CatalogDto;
import com.example.catalogservice.dto.OrderDto;
import com.example.catalogservice.exception.BadRequestException;
import com.example.catalogservice.exception.NotFoundException;
import com.example.catalogservice.jpa.MySQLCatalogEntity;
import com.example.catalogservice.jpa.MySQLCatalogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class KafkaConsumer {
    MySQLCatalogRepository mySQLCatalogRepository;
    KafkaTemplate kafkaTemplate;
    KafkaProducer kafkaProducer;

    @Autowired
    public KafkaConsumer(MySQLCatalogRepository mySQLCatalogRepository, KafkaTemplate kafkaTemplate, KafkaProducer kafkaProducer) {
        this.mySQLCatalogRepository = mySQLCatalogRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProducer = kafkaProducer;
    }

    // userId로 delete orders
    @KafkaListener(topics = "delete-orders", groupId = "group-catalog")
    @Transactional
    public void deleteCatalogs(String message){
        log.info("Delete Orders event occurred");
        List<OrderDto> orderDtoList = null;
        String userId = null;
        try{
            orderDtoList = new ObjectMapper().readValue(message, new TypeReference<List<OrderDto>>() {});
            userId = orderDtoList.get(0).getUserId();

            orderDtoList.forEach(
                    p -> {
                        String productId = p.getProductId();

                        MySQLCatalogEntity mySQLCatalogEntity = mySQLCatalogRepository.findByProductId(productId).orElseThrow(() -> new NotFoundException("productId: " + productId));

                        CatalogDto catalogDto = new CatalogDto();
                        catalogDto.setStock(mySQLCatalogEntity.getStock() + p.getQty());

                        mySQLCatalogEntity.updateCatalog(catalogDto);
                    }
            );
            log.info("{} Delete Orders event success", userId);
        }catch(Exception e){
            log.info("{} Delete Orders event failed", userId);
            kafkaTemplate.send("rollback-delete-orders", userId);
            log.info("{} Rollback Delete Orders event sent", userId);

            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    // orderId로 delete order
    @KafkaListener(topics = "delete-orders", groupId = "group-catalog")
    @Transactional
    public void deleteCatalog(String message){
        log.info("Delete Order event occurred");
        OrderDto orderDto = null;
        try {
            orderDto = new ObjectMapper().readValue(message, OrderDto.class);

            String productId = orderDto.getProductId();

            MySQLCatalogEntity mySQLCatalogEntity = mySQLCatalogRepository.findByProductId(productId).orElseThrow(() -> new NotFoundException("productId: " + productId));

            CatalogDto catalogDto = new CatalogDto();
            catalogDto.setStock(mySQLCatalogEntity.getStock() + orderDto.getQty());

            mySQLCatalogEntity.updateCatalog(catalogDto);
            log.info("{} Delete Order event success", orderDto.getOrderId());
        }catch (Exception e){
            log.info("{} Delete Order event failed", orderDto.getOrderId());
            kafkaTemplate.send("rollback-delete-order", orderDto.getOrderId());
            log.info("{} Rollback Delete Order event sent", orderDto.getOrderId());

            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    // orderId로 update order
    @KafkaListener(topics = "update-order", groupId = "group-catalog")
    @Transactional
    public void updateCatalog(String message){
        log.info("Update Order event occurred");
        OrderDto orderDto = null;
        try{
            orderDto = new ObjectMapper().readValue(message, OrderDto.class);

            String productId = orderDto.getProductId();

            MySQLCatalogEntity mySQLCatalogEntity = mySQLCatalogRepository.findByProductId(productId).orElseThrow(() -> new NotFoundException("productId: " + productId));

            CatalogDto catalogDto = new CatalogDto();
            catalogDto.setStock(mySQLCatalogEntity.getStock() + orderDto.getQty());

            if(catalogDto.getStock() < 0) throw new BadRequestException();

            mySQLCatalogEntity.updateCatalog(catalogDto);
            log.info("{} Update Order event success", orderDto.getOrderId());
        }catch (Exception e){
            log.info("{} Update Order event failed", orderDto.getOrderId());
            kafkaProducer.send("rollback-update-order", orderDto);
            log.info("{} Rollback Update Order event sent", orderDto.getOrderId());

            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
