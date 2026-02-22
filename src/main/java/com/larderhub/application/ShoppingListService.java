package com.larderhub.application;

import com.larderhub.domain.model.PantryItem;
import com.larderhub.domain.model.Product;
import com.larderhub.domain.model.ShoppingItem;
import com.larderhub.domain.model.User;
import com.larderhub.domain.ports.in.shopping.ShoppingListUseCase;
import com.larderhub.domain.ports.out.householdMembers.HouseholdMembersPersistencePort;
import com.larderhub.domain.ports.out.pantry.PantryItemPersistencePort;
import com.larderhub.domain.ports.out.product.ProductPersistencePort;
import com.larderhub.domain.ports.out.shopping.ShoppingItemPersistencePort;
import com.larderhub.domain.ports.out.user.UserPersistencePort;
import com.larderhub.infrastructure.adapter.in.rest.shopping.dto.ShoppingItemCreateDTO;
import com.larderhub.infrastructure.adapter.in.rest.shopping.dto.ShoppingItemResponseDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingListService implements ShoppingListUseCase {

  private final ShoppingItemPersistencePort shoppingItemPersistencePort;
  private final PantryItemPersistencePort pantryItemPersistencePort;
  private final ProductPersistencePort productPersistencePort;
  private final UserPersistencePort userPersistencePort;
  private final HouseholdMembersPersistencePort householdMembersPersistencePort;

  // Validate membership: throws 403 if the user doesn't belong to the household
  private User resolveAndValidateMembership(String username, Long householdId) {
    User user = userPersistencePort.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

    if (!householdMembersPersistencePort.existsByUserIdAndHouseholdId(user.getId(), householdId)) {
      throw new AccessDeniedException("You are not a member of household " + householdId);
    }

    return user;
  }

  @Override
  public ShoppingItemResponseDTO addItem(Long householdId, ShoppingItemCreateDTO dto, String username) {
    resolveAndValidateMembership(username, householdId);

    // Validate that the product exists in the catalog
    Product product = productPersistencePort.findById(dto.getProductId())
        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + dto.getProductId()));

    ShoppingItem item = ShoppingItem.builder()
        .householdId(householdId)
        .productId(product.getId())
        .quantity(dto.getQuantity())
        .checked(false)
        .build();

    ShoppingItem saved = shoppingItemPersistencePort.save(item);
    return toResponseDTO(saved, product);
  }

  @Override
  public List<ShoppingItemResponseDTO> listItems(Long householdId, String username) {
    resolveAndValidateMembership(username, householdId);

    return shoppingItemPersistencePort.findByHouseholdId(householdId).stream()
        .map(item -> {
          Product product = productPersistencePort.findById(item.getProductId())
              .orElseThrow(() -> new IllegalStateException("Product not found for shopping item"));
          return toResponseDTO(item, product);
        })
        .collect(Collectors.toList());
  }

  @Override
  public ShoppingItemResponseDTO checkItem(Long householdId, Long itemId, String username) {
    resolveAndValidateMembership(username, householdId);

    // Ensure the item belongs to this household
    if (!shoppingItemPersistencePort.existsByIdAndHouseholdId(itemId, householdId)) {
      throw new AccessDeniedException("Item " + itemId + " does not belong to household " + householdId);
    }

    ShoppingItem item = shoppingItemPersistencePort.findById(itemId)
        .orElseThrow(() -> new IllegalArgumentException("Shopping item not found: " + itemId));

    // Mark as bought in the shopping list
    item.setChecked(true);
    ShoppingItem updated = shoppingItemPersistencePort.save(item);

    // Also add the item to the pantry so the pantry is updated automatically
    PantryItem pantryItem = PantryItem.builder()
        .householdId(householdId)
        .productId(item.getProductId())
        // PantryItem.quantity is BigDecimal; convert from Double
        .quantity(BigDecimal.valueOf(item.getQuantity()))
        .build();
    pantryItemPersistencePort.save(pantryItem);

    Product product = productPersistencePort.findById(updated.getProductId())
        .orElseThrow(() -> new IllegalStateException("Product not found"));

    return toResponseDTO(updated, product);
  }

  @Override
  public void deleteItem(Long householdId, Long itemId, String username) {
    resolveAndValidateMembership(username, householdId);

    if (!shoppingItemPersistencePort.existsByIdAndHouseholdId(itemId, householdId)) {
      throw new AccessDeniedException("Item " + itemId + " does not belong to household " + householdId);
    }

    shoppingItemPersistencePort.deleteById(itemId);
  }

  @Override
  public List<ShoppingItemResponseDTO> generateFromPantry(Long householdId, Double threshold, String username) {
    resolveAndValidateMembership(username, householdId);

    // Default threshold: items with quantity <= 1 are considered "low stock"
    BigDecimal effectiveThreshold = BigDecimal.valueOf((threshold != null) ? threshold : 1.0);

    List<PantryItem> lowStockItems = pantryItemPersistencePort.findByHouseholdId(householdId).stream()
        // compareTo returns negative if pantryItem.quantity < effectiveThreshold
        .filter(pi -> pi.getQuantity().compareTo(effectiveThreshold) <= 0)
        .collect(Collectors.toList());

    List<ShoppingItemResponseDTO> generated = new ArrayList<>();

    for (PantryItem pantryItem : lowStockItems) {
      Product product = productPersistencePort.findById(pantryItem.getProductId())
          .orElseThrow(() -> new IllegalStateException("Product not found"));

      // Only add if not already in the shopping list (unchecked)
      boolean alreadyInList = shoppingItemPersistencePort.findByHouseholdId(householdId).stream()
          .anyMatch(s -> s.getProductId().equals(pantryItem.getProductId()) && !s.isChecked());

      if (!alreadyInList) {
        ShoppingItem item = ShoppingItem.builder()
            .householdId(householdId)
            .productId(product.getId())
            // Suggest buying enough to go 1 unit above the threshold
            .quantity(effectiveThreshold.subtract(pantryItem.getQuantity()).add(BigDecimal.ONE).doubleValue())
            .checked(false)
            .build();
        ShoppingItem saved = shoppingItemPersistencePort.save(item);
        generated.add(toResponseDTO(saved, product));
      }
    }

    return generated;
  }

  // Build response DTO from domain objects
  private ShoppingItemResponseDTO toResponseDTO(ShoppingItem item, Product product) {
    return ShoppingItemResponseDTO.builder()
        .id(item.getId())
        .householdId(item.getHouseholdId())
        .productId(product.getId())
        .productName(product.getName())
        .productCategory(product.getCategory())
        .standardUnit(product.getStandardUnit())
        .quantity(item.getQuantity())
        .checked(item.isChecked())
        .addedAt(item.getAddedAt())
        .build();
  }
}
