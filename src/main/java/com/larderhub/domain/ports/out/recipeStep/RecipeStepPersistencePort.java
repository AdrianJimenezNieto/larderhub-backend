package com.larderhub.domain.ports.out.recipeStep;

import com.larderhub.domain.model.RecipeStep;
import java.util.Optional;
import java.util.List;

public interface RecipeStepPersistencePort {
  RecipeStep save(RecipeStep recipeStep);

  Optional<RecipeStep> findById(Long id);

  List<RecipeStep> findByRecipeId(Long recipeId);

  void deleteById(Long id);
}
