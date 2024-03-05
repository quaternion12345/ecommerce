package com.example.userservice.exception;

import com.example.userservice.controller.UserController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice(assignableTypes = UserController.class)
public class ExceptionHandlers {
    // Bean Validation Exception Handler
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<Map<String, String>>> beanValidationHandler(MethodArgumentNotValidException ex){
        List<Map<String, String>> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(
                error -> {
                    errors.add(
                            new HashMap<>(){{
                                put("fieldName", error.getField());
                                put("rejectedValue", (error.getRejectedValue() == null) ? null : error.getRejectedValue().toString());
                                put("message", error.getDefaultMessage());
                            }}
                    );
                });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // BadRequest Exception Handler
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> badRequestHandler(BadRequestException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<>(){{
            put("message", ex.getMessage());
        }});
    }

    // Forbidden Exception Handler
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> forbiddenHandler(ForbiddenException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new HashMap<>(){{
            put("message", "You don't have permission to edit" + ex.getMessage());
        }});
    }

    // NotFound Exception Handler
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> notFoundHandler(NotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<>(){{
            put("message", ex.getMessage() + " not found");
        }});
    }
}
