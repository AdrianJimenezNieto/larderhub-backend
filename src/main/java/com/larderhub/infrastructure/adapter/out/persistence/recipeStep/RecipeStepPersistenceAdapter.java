package com.larderhub.infrastructure.adapter.out.persistence.recipeStep;

import com.larderhub.domain.model.RecipeStep;
import com.larderhub.domain.ports.out.recipeStep.RecipeStepPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RecipeStepPersistenceAdapter implements RecipeStepPersistencePort {

  private final RecipeStepJpaRepository recipeStepJpaRepository;
  private final RecipeStepPersistenceMapper recipeStepPersistenceMapper;

  @Override
  public RecipeStep save(RecipeStep recipeStep) {
    RecipeStepEntity entity = recipeStepPersistenceMapper.toEntity(recipeStep);
    return recipeStepPersistenceMapper.toDomain(recipeStepJpaRepository.save(entity));
  }

  @Override
  public Optional<RecipeStep> findById(Long id) {
    return recipeStepJpaRepository.findById(id)
        .map(recipeStepPersistenceMapper::toDomain);
  }

  @Override
  public List<RecipeStep> findByRecipeId(Long recipeId) {
    return recipeStepJpaRepository.findByRecipe_IdOrderByStepOrderAsc(recipeId).stream()
        .map(recipeStepPersistenceMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteById(Long id) {
    recipeStepJpaRepository.deleteById(id);
  }
}
