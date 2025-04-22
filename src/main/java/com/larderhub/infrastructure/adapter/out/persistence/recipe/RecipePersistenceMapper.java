package com.larderhub.infrastructure.adapter.out.persistence.recipe;

import com.larderhub.domain.model.Recipe;
import com.larderhub.domain.model.RecipeIngredient;
import com.larderhub.domain.model.RecipeStep;
import org.mapstruct.Mapper;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RecipePersistenceMapper {

  default RecipeEntity toEntity(Recipe domain) {
    if (domain == null)
      return null;

    RecipeEntity entity = RecipeEntity.builder()
        .id(domain.getId())
        .title(domain.getTitle())
        .description(domain.getDescription())
        .imageUrl(domain.getImageUrl())
        .authorId(domain.getAuthorId())
        .createdAt(domain.getCreatedAt())
        .cookingTimeMinutes(domain.getCookingTimeMinutes())
        .difficulty(domain.getDifficulty())
        .servings(domain.getServings())
        .build();

    if (domain.getIngredients() != null) {
      domain.getIngredients().forEach(ing -> {
        RecipeIngredientEntity ingEntity = RecipeIngredientEntity.builder()
            .id(ing.getId())
            .productId(ing.getProductId())
            .quantity(ing.getQuantity())
            .unit(ing.getUnit())
            .recipe(entity) // Maintain bidirectional sync manually
            .build();
        entity.addIngredient(ingEntity);
      });
    }

    if (domain.getSteps() != null) {
      domain.getSteps().forEach(step -> {
        RecipeStepEntity stepEntity = RecipeStepEntity.builder()
            .id(step.getId())
            .stepNumber(step.getStepNumber())
            .stepTitle(step.getTitle())
            .description(step.getDescription())
            .imageUrl(step.getImageUrl())
            .timerMinutes(step.getTimerMinutes())
            .recipe(entity) // Maintain bidirectional sync manually
            .build();
        entity.addStep(stepEntity);
      });
    }

    return entity;
  }

  default Recipe toDomain(RecipeEntity entity) {
    if (entity == null)
      return null;

    Recipe domain = Recipe.builder()
        .id(entity.getId())
        .title(entity.getTitle())
        .description(entity.getDescription())
        .imageUrl(entity.getImageUrl())
        .authorId(entity.getAuthorId())
        .createdAt(entity.getCreatedAt())
        .cookingTimeMinutes(entity.getCookingTimeMinutes())
        .difficulty(entity.getDifficulty())
        .servings(entity.getServings())
        .build();

    if (entity.getIngredients() != null) {
      domain.setIngredients(entity.getIngredients().stream().map(ingEntity -> RecipeIngredient.builder()
          .id(ingEntity.getId())
          .recipeId(entity.getId())
          .productId(ingEntity.getProductId())
          .quantity(ingEntity.getQuantity())
          .unit(ingEntity.getUnit())
          .build()).collect(Collectors.toList()));
    }

    if (entity.getSteps() != null) {
      domain.setSteps(entity.getSteps().stream().map(stepEntity -> RecipeStep.builder()
          .id(stepEntity.getId())
          .recipeId(entity.getId())
          .stepNumber(stepEntity.getStepNumber())
          .title(stepEntity.getStepTitle())
          .description(stepEntity.getDescription())
          .imageUrl(stepEntity.getImageUrl())
          .timerMinutes(stepEntity.getTimerMinutes())
          .build()).collect(Collectors.toList()));
    }

    return domain;
  }
}
