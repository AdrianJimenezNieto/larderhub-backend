package com.larderhub.infrastructure.adapter.in.rest.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductCreateDTO {

  @NotBlank(message = "Product name is required")
  private String name;

  private String category;
  private String barcode;
  private String imageUrl;
  private String standardUnit;
}
