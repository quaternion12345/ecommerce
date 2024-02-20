package com.example.catalogservice.jpa;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MySQLCatalogRepository extends CrudRepository<MySQLCatalogEntity, Long> {
    Optional<MySQLCatalogEntity> findByProductId(String productId);
}
