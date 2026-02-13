package com.larderhub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Recipe {
  private Long id;
  private Long authorId;
  private String title;
  private String description;
  private Integer prepTime;
  private String difficulty;
}
