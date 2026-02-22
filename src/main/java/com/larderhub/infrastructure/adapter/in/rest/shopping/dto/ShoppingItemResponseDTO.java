package com.larderhub.infrastructure.adapter.in.rest.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingItemResponseDTO {
  private Long id;
  private Long householdId;
  private Long productId;
  private String productName;
  private String productCategory;
  private String standardUnit;
  private Double quantity;
  // Whether the item has been purchased
  private boolean checked;
  private LocalDateTime addedAt;
}
