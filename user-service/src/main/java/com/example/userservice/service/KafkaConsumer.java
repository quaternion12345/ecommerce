package com.example.userservice.service;

import com.example.userservice.exception.NotFoundException;
import com.example.userservice.jpa.MySQLUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumer {
    MySQLUserRepository mySQLUserRepository;

    @Autowired
    public KafkaConsumer(MySQLUserRepository mySQLUserRepository) {
        this.mySQLUserRepository = mySQLUserRepository;
    }

    @KafkaListener(topics = "rollback-user", groupId = "group-user")
    public void rollbackUser(String userId){
        log.info("{} rollback event occurred", userId);
        mySQLUserRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException("userId: " + userId)).rollbackUser();
    }
}
