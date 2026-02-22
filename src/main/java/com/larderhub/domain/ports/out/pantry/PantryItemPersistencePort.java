package com.larderhub.domain.ports.out.pantry;

import com.larderhub.domain.model.PantryItem;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface PantryItemPersistencePort {
  PantryItem save(PantryItem pantryItem);

  Optional<PantryItem> findById(Long id);

  List<PantryItem> findByHouseholdId(Long householdId);

  void deleteById(Long id);

  boolean existsByIdAndHouseholdId(Long id, Long householdId);

  Optional<PantryItem> findByHouseholdIdAndProductId(Long householdId, Long productId);

  List<PantryItem> findExpiredItems(Long householdId, LocalDate currentDate);

  List<PantryItem> findExpiringItems(Long householdId, LocalDate startDate, LocalDate endDate);
}
