package com.example.userservice.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Document(collection = "ecommerce_users")
public class MongoUserEntity {
    @Id
    private Object id;

    private String email;

    private String name;

    private String userId;

    private String encryptedPwd;

    private Date createdAt;
}
