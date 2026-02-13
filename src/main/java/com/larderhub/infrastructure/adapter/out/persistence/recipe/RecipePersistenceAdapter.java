package com.larderhub.infrastructure.adapter.out.persistence.recipe;

import com.larderhub.domain.model.Recipe;
import com.larderhub.domain.ports.out.recipe.RecipePersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RecipePersistenceAdapter implements RecipePersistencePort {

  private final RecipeJpaRepository recipeJpaRepository;
  private final RecipePersistenceMapper recipePersistenceMapper;

  @Override
  public Recipe save(Recipe recipe) {
    RecipeEntity entity = recipePersistenceMapper.toEntity(recipe);
    return recipePersistenceMapper.toDomain(recipeJpaRepository.save(entity));
  }

  @Override
  public Optional<Recipe> findById(Long id) {
    return recipeJpaRepository.findById(id)
        .map(recipePersistenceMapper::toDomain);
  }

  @Override
  public List<Recipe> findByAuthorId(Long authorId) {
    return recipeJpaRepository.findByAuthor_Id(authorId).stream()
        .map(recipePersistenceMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteById(Long id) {
    recipeJpaRepository.deleteById(id);
  }
}
