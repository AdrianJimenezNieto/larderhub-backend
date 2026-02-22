package com.larderhub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingItem {
  private Long id;
  private Long householdId;
  private Long productId;
  private Double quantity;
  // true = the user marked it as bought (moves to pantry)
  private boolean checked;
  private LocalDateTime addedAt;
}
