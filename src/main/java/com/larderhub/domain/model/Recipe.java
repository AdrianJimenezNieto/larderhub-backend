package com.larderhub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Recipe {
  private Long id;
  private String title;
  private String description;
  private String imageUrl;
  private Long authorId;
  private LocalDateTime createdAt;
  private Integer cookingTimeMinutes;
  private String difficulty;
  private Integer servings;

  @Builder.Default
  private List<RecipeIngredient> ingredients = new ArrayList<>();
  @Builder.Default
  private List<RecipeStep> steps = new ArrayList<>();
}
