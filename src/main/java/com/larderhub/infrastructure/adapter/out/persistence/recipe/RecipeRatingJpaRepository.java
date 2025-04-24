package com.larderhub.infrastructure.adapter.out.persistence.recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRatingJpaRepository extends JpaRepository<RecipeRatingEntity, Long> {
  List<RecipeRatingEntity> findByRecipe_Id(Long recipeId);

  boolean existsByRecipe_IdAndUserId(Long recipeId, Long userId);
}
