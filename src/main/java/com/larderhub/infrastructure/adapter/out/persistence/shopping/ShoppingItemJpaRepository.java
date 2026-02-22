package com.larderhub.infrastructure.adapter.out.persistence.shopping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingItemJpaRepository extends JpaRepository<ShoppingItemEntity, Long> {
  // Find all shopping items for a given household
  List<ShoppingItemEntity> findByHousehold_Id(Long householdId);

  // Check if a specific item belongs to the given household (ownership guard)
  boolean existsByIdAndHousehold_Id(Long id, Long householdId);
}
