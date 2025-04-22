package com.larderhub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeStep {
  private Long id;
  private Long recipeId;
  private Integer stepNumber;
  private String title;
  private String description;
  private String imageUrl;
  private Integer timerMinutes;
}
