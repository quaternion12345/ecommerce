package com.example.catalogservice.jpa;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MongoCatalogRepository extends CrudRepository<MongoCatalogEntity, Long> {
    Optional<MongoCatalogEntity> findByProductId(String productId);
}
