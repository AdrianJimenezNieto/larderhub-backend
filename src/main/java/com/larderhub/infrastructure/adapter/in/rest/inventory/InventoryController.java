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

import org.springframework.web.bind.annotation.RequestParam;

@RestController
// anidado bajo households, el householdId va en la URL
@RequestMapping("/api/v1/households/{householdId}/inventory")
@RequiredArgsConstructor
public class InventoryController {

  private final InventoryUseCase inventoryUseCase;

  @PostMapping
  public ResponseEntity<PantryItemResponseDTO> addItem(
      @PathVariable Long householdId,
      @Valid @RequestBody PantryItemCreateDTO dto,
      @AuthenticationPrincipal UserDetails userDetails) {

    PantryItemResponseDTO response = inventoryUseCase.addItem(householdId, dto, userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<List<PantryItemResponseDTO>> listItems(
      @PathVariable Long householdId,
      @AuthenticationPrincipal UserDetails userDetails) {

    return ResponseEntity.ok(inventoryUseCase.listItems(householdId, userDetails.getUsername()));
  }

  @PutMapping("/{itemId}")
  public ResponseEntity<PantryItemResponseDTO> updateItem(
      @PathVariable Long householdId,
      @PathVariable Long itemId,
      @Valid @RequestBody PantryItemUpdateDTO dto,
      @AuthenticationPrincipal UserDetails userDetails) {

    PantryItemResponseDTO response = inventoryUseCase.updateItem(householdId, itemId, dto, userDetails.getUsername());
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{itemId}")
  public ResponseEntity<Void> deleteItem(
      @PathVariable Long householdId,
      @PathVariable Long itemId,
      @AuthenticationPrincipal UserDetails userDetails) {

    inventoryUseCase.deleteItem(householdId, itemId, userDetails.getUsername());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/expired")
  public ResponseEntity<List<PantryItemResponseDTO>> getExpiredItems(
      @PathVariable Long householdId,
      @AuthenticationPrincipal UserDetails userDetails) {

    return ResponseEntity.ok(inventoryUseCase.getExpiredItems(householdId, userDetails.getUsername()));
  }

  @GetMapping("/expiring")
  public ResponseEntity<List<PantryItemResponseDTO>> getExpiringItems(
      @PathVariable Long householdId,
      @RequestParam(defaultValue = "7") int daysAhead,
      @AuthenticationPrincipal UserDetails userDetails) {

    return ResponseEntity.ok(inventoryUseCase.getExpiringItems(householdId, daysAhead, userDetails.getUsername()));
  }
}
