package com.example.catalogservice.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestUpdateCatalog {
    @NotBlank(message = "Product name cannot be empty")
    private String productName;

    @PositiveOrZero(message = "Unit price must be equal or greater than 0")
    private Integer unitPrice;

    @PositiveOrZero(message = "Stock must be equal or greater than 0")
    private Integer stock;
}
