package com.larderhub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeIngredient {
  private Long id;
  private Long recipeId;
  private Long productId;
  private Double quantity;
  private String unit;
}
