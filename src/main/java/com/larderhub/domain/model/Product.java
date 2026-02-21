package com.larderhub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
  private Long id;
  private String barcode;
  private String name;
  private String category;
  private String imageUrl;
  private String standardUnit;
}
