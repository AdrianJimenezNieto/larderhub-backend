package com.larderhub.infrastructure.adapter.in.rest.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRecipeDTO {

  @NotBlank
  private String title;

  private String description;
  private String imageUrl;
  private Integer cookingTimeMinutes;
  private String difficulty;
  private Integer servings;

  @NotEmpty
  private List<RecipeIngredientDTO> ingredients;

  @NotEmpty
  private List<RecipeStepDTO> steps;

  @Data
  public static class RecipeIngredientDTO {
    @NotNull
    private Long productId;

    @NotNull
    @Positive
    private Double quantity;

    private String unit;
  }

  @Data
  public static class RecipeStepDTO {
    @NotNull
    private Integer stepNumber;

    private String title;

    @NotBlank
    private String description;

    private String imageUrl;
    private Integer timerMinutes;
  }
}
