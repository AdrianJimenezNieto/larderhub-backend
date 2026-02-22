package com.larderhub.domain.ports.in.shopping;

import com.larderhub.infrastructure.adapter.in.rest.shopping.dto.ShoppingItemCreateDTO;
import com.larderhub.infrastructure.adapter.in.rest.shopping.dto.ShoppingItemResponseDTO;

import java.util.List;

public interface ShoppingListUseCase {
  // Add an item manually to the household's shopping list
  ShoppingItemResponseDTO addItem(Long householdId, ShoppingItemCreateDTO dto, String username);

  // List all items (checked + unchecked) in the shopping list
  List<ShoppingItemResponseDTO> listItems(Long householdId, String username);

  // Mark an item as bought and move it to the pantry
  ShoppingItemResponseDTO checkItem(Long householdId, Long itemId, String username);

  // Remove an item from the shopping list
  void deleteItem(Long householdId, Long itemId, String username);

  // Auto-generate shopping list from pantry items below the given quantity
  // threshold
  List<ShoppingItemResponseDTO> generateFromPantry(Long householdId, Double threshold, String username);
}
