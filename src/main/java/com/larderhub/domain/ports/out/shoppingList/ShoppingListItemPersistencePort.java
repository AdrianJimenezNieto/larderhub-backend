package com.larderhub.domain.ports.out.shoppingList;

import com.larderhub.domain.model.ShoppingListItem;
import java.util.Optional;
import java.util.List;

public interface ShoppingListItemPersistencePort {
  ShoppingListItem save(ShoppingListItem shoppingListItem);

  Optional<ShoppingListItem> findById(Long id);

  List<ShoppingListItem> findByHouseholdId(Long householdId);

  void deleteById(Long id);
}
