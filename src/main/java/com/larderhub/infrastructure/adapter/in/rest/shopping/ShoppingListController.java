package com.larderhub.infrastructure.adapter.in.rest.shopping;

import com.larderhub.domain.ports.in.shopping.ShoppingListUseCase;
import com.larderhub.infrastructure.adapter.in.rest.shopping.dto.ShoppingItemCreateDTO;
import com.larderhub.infrastructure.adapter.in.rest.shopping.dto.ShoppingItemResponseDTO;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
// Nested under households, consistent with the inventory pattern
@RequestMapping("/api/v1/households/{householdId}/shopping-list")
@RequiredArgsConstructor
public class ShoppingListController {

  private final ShoppingListUseCase shoppingListUseCase;

  // POST /api/v1/households/{householdId}/shopping-list — Add an item manually
  @PostMapping
  public ResponseEntity<ShoppingItemResponseDTO> addItem(
      @PathVariable Long householdId,
      @Valid @RequestBody ShoppingItemCreateDTO dto,
      @AuthenticationPrincipal UserDetails userDetails) {

    ShoppingItemResponseDTO response = shoppingListUseCase.addItem(householdId, dto, userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // GET /api/v1/households/{householdId}/shopping-list — List all items
  @GetMapping
  public ResponseEntity<List<ShoppingItemResponseDTO>> listItems(
      @PathVariable Long householdId,
      @AuthenticationPrincipal UserDetails userDetails) {

    return ResponseEntity.ok(shoppingListUseCase.listItems(householdId, userDetails.getUsername()));
  }

  // PUT /api/v1/households/{householdId}/shopping-list/{itemId}/check — Mark as
  // bought
  @PutMapping("/{itemId}/check")
  public ResponseEntity<ShoppingItemResponseDTO> checkItem(
      @PathVariable Long householdId,
      @PathVariable Long itemId,
      @AuthenticationPrincipal UserDetails userDetails) {

    ShoppingItemResponseDTO response = shoppingListUseCase.checkItem(householdId, itemId, userDetails.getUsername());
    return ResponseEntity.ok(response);
  }

  // DELETE /api/v1/households/{householdId}/shopping-list/{itemId} — Remove an
  // item
  @DeleteMapping("/{itemId}")
  public ResponseEntity<Void> deleteItem(
      @PathVariable Long householdId,
      @PathVariable Long itemId,
      @AuthenticationPrincipal UserDetails userDetails) {

    shoppingListUseCase.deleteItem(householdId, itemId, userDetails.getUsername());
    return ResponseEntity.noContent().build();
  }

  // POST /api/v1/households/{householdId}/shopping-list/generate?threshold=1.0
  // Auto-generate shopping list from low-stock pantry items
  @PostMapping("/generate")
  public ResponseEntity<List<ShoppingItemResponseDTO>> generateFromPantry(
      @PathVariable Long householdId,
      @RequestParam(required = false) Double threshold,
      @AuthenticationPrincipal UserDetails userDetails) {

    List<ShoppingItemResponseDTO> generated = shoppingListUseCase.generateFromPantry(householdId, threshold,
        userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).body(generated);
  }
}
