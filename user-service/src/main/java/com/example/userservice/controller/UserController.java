package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestUpdateUser;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class UserController {
    private Environment env;
    private UserService userService;

//    @Autowired
//    private Greeting greeting;


    @Autowired
    public UserController(Environment env, UserService userService) {
        this.env = env;
        this.userService = userService;
    }

//    @GetMapping("/welcome")
//    @Timed(value = "users.welcome", longTask = true)
//    public String welcome(){
////        return env.getProperty("greeting.message");
//        return greeting.getMessage();
//    }

    // 서버 health check
    @GetMapping("/health_check")
    @Timed(value = "users.status", longTask = true)
    public String status(){
        return String.format("It's Working in User Service"
                + ", port(local.server.port)=" + env.getProperty("local.server.port")
                + ", port(server.port)=" + env.getProperty("server.port")
//                + ", token secret=" + env.getProperty("token.secret")
//                + ", token expiration time=" + env.getProperty("token.expiration_time")
        );
    }

    // 회원 가입
    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody @Valid RequestUser user){
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserDto userDto = mapper.map(user, UserDto.class);

        UserDto createdUser = userService.createUser(userDto);

        ResponseUser responseUser = mapper.map(createdUser, ResponseUser.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }

    // 전체 회원 정보 조회
    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUsers(){
        Iterable<UserDto> userList = userService.getUserByAll();

        List<ResponseUser> result = new ArrayList<>();
        userList.forEach(v -> {
            result.add(new ModelMapper().map(v, ResponseUser.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    // 개인 회원 정보 조회
    @GetMapping("/{userId}/users")
    public ResponseEntity<ResponseUser> getUser(@PathVariable("userId") String userId){
        return ResponseEntity.status(HttpStatus.OK).body(new ModelMapper().map(userService.getUserByUserId(userId), ResponseUser.class));
    }

    // 개인 회원 정보 수정 -> 주문 정보에도 반영 필요(UPDATE)
    @PatchMapping("/{userId}/users")
    public ResponseEntity<ResponseUser> updateUser(@PathVariable("userId") String userId, @RequestBody @Valid RequestUpdateUser requestUser){
        ModelMapper mapper = new ModelMapper();
        UserDto userDto = mapper.map(requestUser, UserDto.class);

        UserDto updatedUser = userService.updateUser(userId, userDto);

        ResponseUser responseUser = mapper.map(updatedUser, ResponseUser.class);
        return  ResponseEntity.status(HttpStatus.OK).body(responseUser);
    }

    // 회원 탈퇴 -> 주문 정보에도 반영 필요(CASCADE)
    @DeleteMapping("/{userId}/users")
    public ResponseEntity<ResponseUser> deleteUser(@PathVariable("userId") String userId){
        userService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
