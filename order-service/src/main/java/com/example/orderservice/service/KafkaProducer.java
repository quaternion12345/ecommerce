package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class KafkaProducer {
    private KafkaTemplate kafkaTemplate;

    @Autowired
    public KafkaProducer(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, OrderDto orderDto){
        String jsonInString = "";
        try{
            jsonInString = new ObjectMapper().writeValueAsString(orderDto);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }

        kafkaTemplate.send(topic, jsonInString);
    }

    public void send(String topic, List<OrderDto> orderDtoList){
        String jsonInString = "";
        try{
            jsonInString = new ObjectMapper().writeValueAsString(orderDtoList);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }

        kafkaTemplate.send(topic, jsonInString);
    }
}
