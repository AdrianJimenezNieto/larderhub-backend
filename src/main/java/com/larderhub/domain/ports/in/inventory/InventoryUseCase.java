package com.larderhub.domain.ports.in.inventory;

import com.larderhub.infrastructure.adapter.in.rest.inventory.dto.PantryItemCreateDTO;
import com.larderhub.infrastructure.adapter.in.rest.inventory.dto.PantryItemResponseDTO;
import com.larderhub.infrastructure.adapter.in.rest.inventory.dto.PantryItemUpdateDTO;

import java.util.List;

public interface InventoryUseCase {
  PantryItemResponseDTO addItem(PantryItemCreateDTO dto, String username);

  List<PantryItemResponseDTO> listItems(String username);

  PantryItemResponseDTO updateItem(Long itemId, PantryItemUpdateDTO dto, String username);

  void deleteItem(Long itemId, String username);
}
