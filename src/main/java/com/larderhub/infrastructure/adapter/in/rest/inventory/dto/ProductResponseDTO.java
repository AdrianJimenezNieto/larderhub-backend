package com.larderhub.infrastructure.adapter.in.rest.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO {
  private Long id;
  private String name;
  private String barcode;
  private String imageUrl;
  private String standardUnit;
}
