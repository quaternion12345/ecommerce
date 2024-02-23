package com.example.userservice.jpa;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MongoUserRepository extends CrudRepository<MongoUserEntity, Long> {
    Optional<MongoUserEntity> findByUserId(String userId);
    Optional<MongoUserEntity> findByEmail(String username);
}
