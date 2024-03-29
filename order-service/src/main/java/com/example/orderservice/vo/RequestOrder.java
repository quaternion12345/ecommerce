package com.example.orderservice.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestOrder {
    @NotNull(message = "ProductId cannot be null")
    @Size(min = 2, message = "ProductId cannot be less than two characters")
    private String productId;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be greater than zero")
    private Integer qty;
}
