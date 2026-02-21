package com.larderhub.infrastructure.adapter.in.rest.inventory;

import com.larderhub.domain.ports.in.inventory.InventoryUseCase;
import com.larderhub.infrastructure.adapter.in.rest.inventory.dto.PantryItemCreateDTO;
import com.larderhub.infrastructure.adapter.in.rest.inventory.dto.PantryItemResponseDTO;
import com.larderhub.infrastructure.adapter.in.rest.inventory.dto.PantryItemUpdateDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

  private final InventoryUseCase inventoryUseCase;

  // POST /api/v1/inventory — Add a new item to the authenticated user's pantry
  @PostMapping
  public ResponseEntity<PantryItemResponseDTO> addItem(
      @Valid @RequestBody PantryItemCreateDTO dto,
      @AuthenticationPrincipal UserDetails userDetails) {

    PantryItemResponseDTO response = inventoryUseCase.addItem(dto, userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // GET /api/v1/inventory — List all items from the authenticated user's pantry
  @GetMapping
  public ResponseEntity<List<PantryItemResponseDTO>> listItems(
      @AuthenticationPrincipal UserDetails userDetails) {

    return ResponseEntity.ok(inventoryUseCase.listItems(userDetails.getUsername()));
  }

  // PUT /api/v1/inventory/{itemId} — Update quantity or expiration date of an
  // item
  @PutMapping("/{itemId}")
  public ResponseEntity<PantryItemResponseDTO> updateItem(
      @PathVariable Long itemId,
      @Valid @RequestBody PantryItemUpdateDTO dto,
      @AuthenticationPrincipal UserDetails userDetails) {

    PantryItemResponseDTO response = inventoryUseCase.updateItem(itemId, dto, userDetails.getUsername());
    return ResponseEntity.ok(response);
  }

  // DELETE /api/v1/inventory/{itemId} — Remove an item from the pantry
  @DeleteMapping("/{itemId}")
  public ResponseEntity<Void> deleteItem(
      @PathVariable Long itemId,
      @AuthenticationPrincipal UserDetails userDetails) {

    inventoryUseCase.deleteItem(itemId, userDetails.getUsername());
    return ResponseEntity.noContent().build();
  }
}
