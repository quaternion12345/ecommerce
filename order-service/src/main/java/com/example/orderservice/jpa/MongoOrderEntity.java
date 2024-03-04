package com.example.orderservice.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;
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

    private Date createdAt;
}
