package com.larderhub.infrastructure.adapter.out.persistence.recipeIngredient;

import com.larderhub.domain.model.RecipeIngredient;
import com.larderhub.domain.ports.out.recipeIngredient.RecipeIngredientPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RecipeIngredientPersistenceAdapter implements RecipeIngredientPersistencePort {

  private final RecipeIngredientJpaRepository recipeIngredientJpaRepository;
  private final RecipeIngredientPersistenceMapper recipeIngredientPersistenceMapper;

  @Override
  public RecipeIngredient save(RecipeIngredient recipeIngredient) {
    RecipeIngredientEntity entity = recipeIngredientPersistenceMapper.toEntity(recipeIngredient);
    return recipeIngredientPersistenceMapper.toDomain(recipeIngredientJpaRepository.save(entity));
  }

  @Override
  public Optional<RecipeIngredient> findById(Long id) {
    return recipeIngredientJpaRepository.findById(id)
        .map(recipeIngredientPersistenceMapper::toDomain);
  }

  @Override
  public List<RecipeIngredient> findByRecipeId(Long recipeId) {
    return recipeIngredientJpaRepository.findByRecipe_Id(recipeId).stream()
        .map(recipeIngredientPersistenceMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteById(Long id) {
    recipeIngredientJpaRepository.deleteById(id);
  }
}
