package com.larderhub.infrastructure.adapter.out.persistence.recipe;

import com.larderhub.domain.model.RecipeRating;
import com.larderhub.domain.ports.out.recipe.RecipeRatingPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RecipeRatingPersistenceAdapter implements RecipeRatingPersistencePort {

  private final RecipeRatingJpaRepository repository;
  private final RecipeRatingPersistenceMapper mapper;

  @Override
  public RecipeRating save(RecipeRating rating) {
    RecipeRatingEntity entity = mapper.toEntity(rating);
    return mapper.toDomain(repository.save(entity));
  }

  @Override
  public List<RecipeRating> findByRecipeId(Long recipeId) {
    return repository.findByRecipe_Id(recipeId).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public boolean existsByRecipeIdAndUserId(Long recipeId, Long userId) {
    return repository.existsByRecipe_IdAndUserId(recipeId, userId);
  }
}
