package com.larderhub.infrastructure.adapter.in.rest.recipe.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RecipeResponseDTO {
  private Long id;
  private String title;
  private String description;
  private String imageUrl;
  private Long authorId;
  private String authorUsername;
  private Double averageRating;
  private int ratingsCount;
  private LocalDateTime createdAt;
  private Integer cookingTimeMinutes;
  private String difficulty;
  private Integer servings;

  private List<IngredientResponseDTO> ingredients;
  private List<StepResponseDTO> steps;

  @Data
  @Builder
  public static class IngredientResponseDTO {
    private Long id;
    private Long productId;
    private String productName; // Inflated via ProductCatalog
    private String productImageUrl;
    private Double quantity;
    private String unit;
  }

  @Data
  @Builder
  public static class StepResponseDTO {
    private Long id;
    private Integer stepNumber;
    private String title;
    private String description;
    private String imageUrl;
    private Integer timerMinutes;
  }
}
