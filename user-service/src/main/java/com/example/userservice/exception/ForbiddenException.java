package com.example.userservice.exception;

public class ForbiddenException extends RuntimeException{
    public ForbiddenException() {
    }
    public ForbiddenException(String message) {
        super(message);
    }
}
