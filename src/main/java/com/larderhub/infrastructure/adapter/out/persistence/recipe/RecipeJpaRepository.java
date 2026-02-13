package com.larderhub.infrastructure.adapter.out.persistence.recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipeJpaRepository extends JpaRepository<RecipeEntity, Long> {
  List<RecipeEntity> findByAuthor_Id(Long authorId);
}
