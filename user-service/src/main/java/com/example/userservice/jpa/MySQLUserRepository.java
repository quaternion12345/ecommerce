package com.example.userservice.jpa;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MySQLUserRepository extends CrudRepository<MySQLUserEntity, Long> {
    Optional<MySQLUserEntity> findByUserId(String userId);

    Optional<MySQLUserEntity> findByEmail(String username);
}
