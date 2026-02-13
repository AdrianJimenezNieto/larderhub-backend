package com.larderhub.infrastructure.adapter.out.persistence.recipeIngredient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipeIngredientJpaRepository extends JpaRepository<RecipeIngredientEntity, Long> {
  List<RecipeIngredientEntity> findByRecipe_Id(Long recipeId);
}
