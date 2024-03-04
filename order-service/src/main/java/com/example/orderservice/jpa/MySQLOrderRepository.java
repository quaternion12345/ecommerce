package com.example.orderservice.jpa;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MySQLOrderRepository extends CrudRepository<MySQLOrderEntity, Long> {
    Optional<MySQLOrderEntity> findByOrderId(String orderId);
    Iterable<MySQLOrderEntity> findByUserId(String userId);
}
