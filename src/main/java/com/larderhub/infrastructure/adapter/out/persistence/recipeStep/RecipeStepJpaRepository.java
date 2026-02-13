package com.larderhub.infrastructure.adapter.out.persistence.recipeStep;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipeStepJpaRepository extends JpaRepository<RecipeStepEntity, Long> {
  List<RecipeStepEntity> findByRecipe_IdOrderByStepOrderAsc(Long recipeId);
}
