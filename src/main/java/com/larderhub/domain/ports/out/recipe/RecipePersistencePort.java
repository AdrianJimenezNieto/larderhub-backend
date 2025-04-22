package com.larderhub.domain.ports.out.recipe;

import com.larderhub.domain.model.Recipe;
import java.util.Optional;
import java.util.List;

public interface RecipePersistencePort {
  Recipe save(Recipe recipe);

  Optional<Recipe> findById(Long id);

  List<Recipe> findByAuthorId(Long authorId);

  List<Recipe> findAll();

  void deleteById(Long id);
}
