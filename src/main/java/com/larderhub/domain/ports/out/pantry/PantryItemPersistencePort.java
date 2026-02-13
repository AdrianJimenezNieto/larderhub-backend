package com.larderhub.domain.ports.out.pantry;

import com.larderhub.domain.model.PantryItem;
import java.util.Optional;
import java.util.List;

public interface PantryItemPersistencePort {
  PantryItem save(PantryItem pantryItem);

  Optional<PantryItem> findById(Long id);

  List<PantryItem> findByHouseholdId(Long householdId);

  void deleteById(Long id);
}
