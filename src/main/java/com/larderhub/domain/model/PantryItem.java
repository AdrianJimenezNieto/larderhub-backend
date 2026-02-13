package com.larderhub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PantryItem {
  private Long id;
  private Long householdId;
  private Long productId;
  private BigDecimal quantity;
  private LocalDate expirationDate;
}
