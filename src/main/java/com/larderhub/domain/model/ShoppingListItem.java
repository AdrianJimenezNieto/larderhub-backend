package com.larderhub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingListItem {
  private Long id;
  private Long householdId;
  private Long productId;
  private BigDecimal quantity;
  private boolean isChecked;
}
