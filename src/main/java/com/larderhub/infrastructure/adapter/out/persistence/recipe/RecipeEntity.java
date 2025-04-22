package com.larderhub.infrastructure.adapter.out.persistence.recipe;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "image_url", columnDefinition = "TEXT")
  private String imageUrl;

  @Column(name = "author_id", nullable = false)
  private Long authorId;

  @Column(name = "cooking_time_minutes")
  private Integer cookingTimeMinutes;

  @Column(length = 20)
  private String difficulty;

  private Integer servings;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<RecipeIngredientEntity> ingredients = new ArrayList<>();

  @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<RecipeStepEntity> steps = new ArrayList<>();

  // Helper methods to maintain bidirectional sync
  public void addIngredient(RecipeIngredientEntity ingredient) {
    ingredients.add(ingredient);
    ingredient.setRecipe(this);
  }

  public void addStep(RecipeStepEntity step) {
    steps.add(step);
    step.setRecipe(this);
  }
}
