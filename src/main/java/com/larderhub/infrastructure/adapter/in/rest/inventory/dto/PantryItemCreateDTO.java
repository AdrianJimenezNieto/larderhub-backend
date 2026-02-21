package com.larderhub.infrastructure.adapter.in.rest.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PantryItemCreateDTO {

  @NotNull(message = "Product ID is required")
  private Long productId;

  @NotNull(message = "Quantity is required")
  @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than zero")
  private BigDecimal quantity;

  private LocalDate expirationDate;
}
