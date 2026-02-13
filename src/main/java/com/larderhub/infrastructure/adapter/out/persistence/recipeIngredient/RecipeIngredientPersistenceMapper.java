package com.larderhub.infrastructure.adapter.out.persistence.recipeIngredient;

import com.larderhub.domain.model.RecipeIngredient;
import com.larderhub.infrastructure.adapter.out.persistence.recipe.RecipeEntity;
import com.larderhub.infrastructure.adapter.out.persistence.product.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecipeIngredientPersistenceMapper {

  @Mapping(source = "recipeId", target = "recipe")
  @Mapping(source = "productId", target = "product")
  RecipeIngredientEntity toEntity(RecipeIngredient domain);

  @Mapping(source = "recipe.id", target = "recipeId")
  @Mapping(source = "product.id", target = "productId")
  RecipeIngredient toDomain(RecipeIngredientEntity entity);

  default RecipeEntity mapRecipeIdToEntity(Long id) {
    if (id == null)
      return null;
    return RecipeEntity.builder().id(id).build();
  }

  default ProductEntity mapProductIdToEntity(Long id) {
    if (id == null)
      return null;
    return ProductEntity.builder().id(id).build();
  }
}
