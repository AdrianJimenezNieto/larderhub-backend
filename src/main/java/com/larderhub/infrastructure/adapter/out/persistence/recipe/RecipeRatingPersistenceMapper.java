package com.larderhub.infrastructure.adapter.out.persistence.recipe;

import com.larderhub.domain.model.RecipeRating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecipeRatingPersistenceMapper {

  @Mapping(target = "recipe.id", source = "recipeId")
  RecipeRatingEntity toEntity(RecipeRating domain);

  @Mapping(target = "recipeId", source = "recipe.id")
  RecipeRating toDomain(RecipeRatingEntity entity);
}
