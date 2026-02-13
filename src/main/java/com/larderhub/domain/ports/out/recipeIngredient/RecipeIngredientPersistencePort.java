package com.larderhub.domain.ports.out.recipeIngredient;

import com.larderhub.domain.model.RecipeIngredient;
import java.util.Optional;
import java.util.List;

public interface RecipeIngredientPersistencePort {
  RecipeIngredient save(RecipeIngredient recipeIngredient);

  Optional<RecipeIngredient> findById(Long id);

  List<RecipeIngredient> findByRecipeId(Long recipeId);

  void deleteById(Long id);
}
