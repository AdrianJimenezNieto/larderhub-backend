package com.larderhub.domain.ports.in.inventory;

import com.larderhub.infrastructure.adapter.in.rest.inventory.dto.PantryItemCreateDTO;
import com.larderhub.infrastructure.adapter.in.rest.inventory.dto.PantryItemResponseDTO;
import com.larderhub.infrastructure.adapter.in.rest.inventory.dto.PantryItemUpdateDTO;

import java.util.List;

public interface InventoryUseCase {
  // householdId is now explicit — client decides which household to operate on
  PantryItemResponseDTO addItem(Long householdId, PantryItemCreateDTO dto, String username);

  List<PantryItemResponseDTO> listItems(Long householdId, String username);

  PantryItemResponseDTO updateItem(Long householdId, Long itemId, PantryItemUpdateDTO dto, String username);

  void deleteItem(Long householdId, Long itemId, String username);

  // Alerts
  List<PantryItemResponseDTO> getExpiredItems(Long householdId, String username);

  List<PantryItemResponseDTO> getExpiringItems(Long householdId, int daysAhead, String username);
}
