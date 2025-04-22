package com.larderhub.domain.ports.out.shopping;

import com.larderhub.domain.model.ShoppingItem;

import java.util.List;
import java.util.Optional;

public interface ShoppingItemPersistencePort {
  ShoppingItem save(ShoppingItem item);

  Optional<ShoppingItem> findById(Long id);

  // List all items (checked and unchecked) for a household
  List<ShoppingItem> findByHouseholdId(Long householdId);

  // Check if a specific item belongs to the given household (ownership guard)
  boolean existsByIdAndHouseholdId(Long id, Long householdId);

  void deleteById(Long id);

  // [Slice 5] Delete all shopping items for a household (used when dissolving)
  void deleteAllByHouseholdId(Long householdId);
}
