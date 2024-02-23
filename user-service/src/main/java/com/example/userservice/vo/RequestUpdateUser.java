package com.example.userservice.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestUpdateUser {
    @Size(min = 2, message = "Email cannot be less than two characters")
    @Email(message = "Invalid email Format")
    private String email;

    @Size(min = 2, message = "Name cannot be less than two characters")
    private String name;

    @Size(min = 8, message = "Password must be equal or greater than 8 characters")
    private String pwd;
}
