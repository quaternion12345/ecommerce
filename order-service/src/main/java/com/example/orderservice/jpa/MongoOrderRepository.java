package com.example.orderservice.jpa;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MongoOrderRepository extends CrudRepository<MongoOrderEntity, Long> {
    Optional<MongoOrderEntity> findByOrderId(String orderId);
    Iterable<MongoOrderEntity> findByUserId(String userId);
}
