package com.larderhub.infrastructure.adapter.out.persistence.recipeStep;

import com.larderhub.domain.model.RecipeStep;
import com.larderhub.infrastructure.adapter.out.persistence.recipe.RecipeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecipeStepPersistenceMapper {

  @Mapping(source = "recipeId", target = "recipe")
  RecipeStepEntity toEntity(RecipeStep domain);

  @Mapping(source = "recipe.id", target = "recipeId")
  RecipeStep toDomain(RecipeStepEntity entity);

  default RecipeEntity mapRecipeIdToEntity(Long id) {
    if (id == null)
      return null;
    return RecipeEntity.builder().id(id).build();
  }
}
