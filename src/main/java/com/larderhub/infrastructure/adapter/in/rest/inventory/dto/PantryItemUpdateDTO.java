package com.larderhub.infrastructure.adapter.in.rest.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PantryItemUpdateDTO {

  @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than zero")
  private BigDecimal quantity;

  private LocalDate expirationDate;
}
