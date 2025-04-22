package com.larderhub.infrastructure.adapter.in.rest.recipe.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecipeSuggestionDTO {
  private Long id;
  private String title;
  private String description;
  private String imageUrl;
  private Double averageRating;
  private int ratingsCount;

  // The engine magic fields
  private Double matchPercentage;
  private List<MissingIngredientDTO> missingIngredients;

  @Data
  @Builder
  public static class MissingIngredientDTO {
    private Long productId;
    private String productName;
    private Double quantityNeeded;
    private String unit;
  }
}
