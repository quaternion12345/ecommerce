package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    UserDto createUser(UserDto userDto);
    UserDto getUserByUserId(String userId);
    List<UserDto> getUserByAll();
    UserDto getUserDetailsByEmail(String userName);
    UserDto updateUser(String userId, UserDto userDto);
    void deleteUser(String userId);
}
