package com.larderhub.domain.ports.out.recipe;

import com.larderhub.domain.model.RecipeRating;

import java.util.List;

public interface RecipeRatingPersistencePort {
  RecipeRating save(RecipeRating rating);

  List<RecipeRating> findByRecipeId(Long recipeId);

  boolean existsByRecipeIdAndUserId(Long recipeId, Long userId);
}
