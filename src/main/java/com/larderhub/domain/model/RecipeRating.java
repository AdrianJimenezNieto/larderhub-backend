package com.larderhub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeRating {
  private Long id;
  private Long recipeId;
  private Long userId;
  private Integer rating;
  private String comment;
  private LocalDateTime createdAt;
}
