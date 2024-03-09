//package com.example.orderservice.messagequeue;
//
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.core.DefaultKafkaProducerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.core.ProducerFactory;
//import org.springframework.kafka.support.serializer.JsonSerializer;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@EnableKafka
//@Configuration
//public class KafkaProducerConfig {
//    @Value("${spring.kafka.consumer.bootstrap-servers}")
//    String kafkaAddress;
//
//    @Bean
//    public KafkaTemplate<String, String> kafkaTemplate() {
//        return new KafkaTemplate<>(
//                new DefaultKafkaProducerFactory<>(
//                        new HashMap<>() {{
//                            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaAddress);
//                            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//                            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//                        }}
//                )
//        );
//    }
//
//    @Bean
//    public KafkaTemplate<String, Object> jsonKafkaTemplate() {
//        return new KafkaTemplate<>(
//                new DefaultKafkaProducerFactory<>(
//                        new HashMap<>() {{
//                            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaAddress);
//                            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//                            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//                        }}
//                )
//        );
//    }
//}
