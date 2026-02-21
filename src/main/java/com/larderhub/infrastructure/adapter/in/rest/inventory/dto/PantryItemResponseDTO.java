package com.larderhub.infrastructure.adapter.in.rest.inventory.dto;

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
public class PantryItemResponseDTO {
  private Long id;
  private Long productId;
  private String productName;
  private String productBarcode;
  private String productImageUrl;
  private String standardUnit;
  private BigDecimal quantity;
  private LocalDate expirationDate;
}
