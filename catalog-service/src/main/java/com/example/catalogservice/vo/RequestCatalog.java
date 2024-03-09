package com.example.catalogservice.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestCatalog {
    @NotNull(message = "Product name cannot be null")
    @NotBlank(message = "Product name cannot be empty")
    private String productName;

    @NotNull(message = "Unit price cannot be null")
    @Positive(message = "Unit price must be larger than 0")
    private Integer unitPrice;

    @NotNull(message = "Stock cannot be null")
    @Positive(message = "Stock must be larger than 0")
    private Integer stock;
}
