package com.larderhub.infrastructure.adapter.out.persistence.shoppingList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShoppingListItemJpaRepository extends JpaRepository<ShoppingListItemEntity, Long> {
  List<ShoppingListItemEntity> findByHousehold_Id(Long householdId);
}
