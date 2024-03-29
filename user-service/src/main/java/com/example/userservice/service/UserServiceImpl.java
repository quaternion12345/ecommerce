package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.exception.BadRequestException;
import com.example.userservice.exception.NotFoundException;
import com.example.userservice.jpa.MongoUserEntity;
import com.example.userservice.jpa.MongoUserRepository;
import com.example.userservice.jpa.MySQLUserEntity;
import com.example.userservice.jpa.MySQLUserRepository;
import com.example.userservice.vo.ResponseOrder;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class UserServiceImpl implements UserService{
    MySQLUserRepository mySQLUserRepository;
    MongoUserRepository mongoUserRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder;
    Environment env;
//    RestTemplate restTemplate;
    OrderServiceClient orderServiceClient;
    CircuitBreakerFactory circuitBreakerFactory;

    KafkaTemplate kafkaTemplate;


    @Autowired
    public UserServiceImpl(MySQLUserRepository mySQLUserRepository, MongoUserRepository mongoUserRepository, BCryptPasswordEncoder bCryptPasswordEncoder, Environment env, OrderServiceClient orderServiceClient, CircuitBreakerFactory circuitBreakerFactory, KafkaTemplate kafkaTemplate){
        this.mySQLUserRepository = mySQLUserRepository;
        this.mongoUserRepository = mongoUserRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.env = env;
//        this.restTemplate = restTemplate;
        this.orderServiceClient = orderServiceClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        // Generate userId
        userDto.setUserId(UUID.randomUUID().toString());

        // Generate encryptPwd
        userDto.setEncryptedPwd(bCryptPasswordEncoder.encode(userDto.getPwd()));

        MySQLUserEntity mySQLUserEntity = MySQLUserEntity.builder()
                .email(userDto.getEmail())
                .name(userDto.getName())
                .userId(userDto.getUserId())
                .encryptedPwd(userDto.getEncryptedPwd())
                .build();

        return new ModelMapper().map(mySQLUserRepository.save(mySQLUserEntity), UserDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUserByAll() {
        return StreamSupport.stream(mongoUserRepository.findAll().spliterator(), false)
                .filter(h -> h.isValid())
                .map(p -> new ModelMapper().map(p, UserDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByUserId(String userId) {
        // Get User Info From Query DB
        MongoUserEntity mongoUserEntity = mongoUserRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException("userId: " + userId));
        if(!mongoUserEntity.isValid()) throw new NotFoundException("userId: " + userId);

        UserDto userDto = new ModelMapper().map(mongoUserEntity, UserDto.class);

        // Get Order Info From Order Microservice
        /*- Using a RestTemplate -*/
//        String orderUrl = String.format(env.getProperty("order_service.url"), userId);
//        ResponseEntity<List<ResponseOrder>> orderListResponse =
//                restTemplate.exchange(orderUrl, HttpMethod.GET, null,
//                new ParameterizedTypeReference<List<ResponseOrder>>() {
//                });
//        List<ResponseOrder> ordersList = orderListResponse.getBody();

        /*- Using a Feign Client -*/
        /* Use Try-Catch Exception Handling */
//        List<ResponseOrder> ordersList = null;
//        try{
//            orderServiceClient.getOrders(userId);
//        }catch(FeignException ex){
//            log.error(ex.getMessage());
//        }

        /* Use Error Decoder */
//        List<ResponseOrder> ordersList = orderServiceClient.getOrders(userId);

        /* Use CircuitBreaker */
        log.info("Before call orders microservice");
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
        List<ResponseOrder> ordersList = circuitBreaker.run(() -> orderServiceClient.getOrders(userId),
                throwable -> new ArrayList<>());
        log.info("After call orders microservice");

        userDto.setOrders(ordersList);

        return userDto;
    }

    @Override
    @Transactional
    public UserDto updateUser(String userId, UserDto userDto) {
        MySQLUserEntity mySQLUserEntity = mySQLUserRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException("userId: " + userId));

        // 값 유효성 확인
        if(userDto.getName() == null && userDto.getPwd() == null) throw new BadRequestException("Empty Input");

        // 수정 수행
        if(userDto.getPwd() != null) userDto.setEncryptedPwd(bCryptPasswordEncoder.encode(userDto.getPwd()));
        mySQLUserEntity.updateUser(userDto);

        return new ModelMapper().map(mySQLUserEntity, UserDto.class);
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        // 삭제 수행
//        mySQLUserRepository.delete(mySQLUserRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException("userId: " + userId)));

        // Soft Delete 수행
        MySQLUserEntity mySQLUserEntity = mySQLUserRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException("userId: " + userId));
        mySQLUserEntity.deleteUser();

        // Order Service에 주문내역 삭제 Event 전송
        kafkaTemplate.send("delete-user", userId);
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Transactional
    public void cleanUp(){
        StreamSupport.stream(mySQLUserRepository.findAll().spliterator(), false)
                .filter(h -> !h.isValid())
                .forEach(p -> mySQLUserRepository.delete(p));
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        MySQLUserEntity mySQLUserEntity = mySQLUserRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        if(!mySQLUserEntity.isValid()) throw new UsernameNotFoundException(email);

        return new ModelMapper().map(mySQLUserEntity, UserDto.class);
    }

    // Spring Security가 DB에서 User 정보를 가져오는데 사용
    // AbstractUserDetailsAuthenticationProvider의 retrieveUser()에서 호출
    // 이 정보를 additionalAuthenticationChecks()에서 authenticate()에서 받은 인자로 받은 유저 입력과 비교
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MySQLUserEntity mySQLUserEntity = mySQLUserRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
        if(!mySQLUserEntity.isValid()) throw new UsernameNotFoundException(username);

        return new User(mySQLUserEntity.getEmail(), mySQLUserEntity.getEncryptedPwd(), true, true, true, true, new ArrayList<>());
    }
}
