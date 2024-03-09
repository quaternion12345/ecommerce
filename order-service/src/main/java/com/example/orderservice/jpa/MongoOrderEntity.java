package com.example.orderservice.jpa;

import jakarta.persistence.Id;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Getter
@Document(collection = "ecommerce_orders")
public class MongoOrderEntity implements Serializable {
    @Id
    private Object id;

    private String productId;

    private Integer qty;

    private Integer unitPrice;

    private Integer totalPrice;

    private String userId;

    private String orderId;

    private boolean valid;

    private Date createdAt;
}
