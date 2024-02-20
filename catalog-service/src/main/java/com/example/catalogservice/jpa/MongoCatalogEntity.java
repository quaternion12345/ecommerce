package com.example.catalogservice.jpa;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Getter
@Document(collection = "ecommerce_catalogs")
public class MongoCatalogEntity implements Serializable {
    @Id
    private Object id;

    private String productId;

    private String productName;

    private Integer stock;

    private Integer unitPrice;

    private Date createdAt;
}
