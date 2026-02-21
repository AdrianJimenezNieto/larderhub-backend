package com.larderhub.application;

import com.larderhub.domain.model.HouseholdMember;
import com.larderhub.domain.model.PantryItem;
import com.larderhub.domain.model.Product;
import com.larderhub.domain.model.User;
import com.larderhub.domain.ports.in.inventory.InventoryUseCase;
import com.larderhub.domain.ports.out.householdMembers.HouseholdMembersPersistencePort;
import com.larderhub.domain.ports.out.pantry.PantryItemPersistencePort;
import com.larderhub.domain.ports.out.product.ProductPersistencePort;
import com.larderhub.domain.ports.out.user.UserPersistencePort;
import com.larderhub.infrastructure.adapter.in.rest.inventory.dto.PantryItemCreateDTO;
import com.larderhub.infrastructure.adapter.in.rest.inventory.dto.PantryItemResponseDTO;
import com.larderhub.infrastructure.adapter.in.rest.inventory.dto.PantryItemUpdateDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService implements InventoryUseCase {

  private final PantryItemPersistencePort pantryItemPersistencePort;
  private final ProductPersistencePort productPersistencePort;
  private final UserPersistencePort userPersistencePort;
  private final HouseholdMembersPersistencePort householdMembersPersistencePort;

  // Resolve the householdId for the authenticated user
  private Long resolveHouseholdId(String username) {
    User user = userPersistencePort.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

    HouseholdMember membership = householdMembersPersistencePort.findByUserId(user.getId())
        .orElseThrow(() -> new IllegalStateException("User does not belong to any household"));

    return membership.getHouseholdId();
  }

  @Override
  public PantryItemResponseDTO addItem(PantryItemCreateDTO dto, String username) {
    Long householdId = resolveHouseholdId(username);

    // Validate that the product exists in the catalog
    Product product = productPersistencePort.findById(dto.getProductId())
        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + dto.getProductId()));

    PantryItem item = PantryItem.builder()
        .householdId(householdId)
        .productId(product.getId())
        .quantity(dto.getQuantity())
        .expirationDate(dto.getExpirationDate())
        .build();

    PantryItem saved = pantryItemPersistencePort.save(item);
    return toResponseDTO(saved, product);
  }

  @Override
  public List<PantryItemResponseDTO> listItems(String username) {
    Long householdId = resolveHouseholdId(username);

    return pantryItemPersistencePort.findByHouseholdId(householdId).stream()
        .map(item -> {
          Product product = productPersistencePort.findById(item.getProductId())
              .orElseThrow(() -> new IllegalStateException("Orphaned pantry item: product not found"));
          return toResponseDTO(item, product);
        })
        .collect(Collectors.toList());
  }

  @Override
  public PantryItemResponseDTO updateItem(Long itemId, PantryItemUpdateDTO dto, String username) {
    Long householdId = resolveHouseholdId(username);

    // Ensure the item belongs to this user's household (ownership check)
    if (!pantryItemPersistencePort.existsByIdAndHouseholdId(itemId, householdId)) {
      throw new AccessDeniedException("Item does not belong to your household");
    }

    PantryItem existing = pantryItemPersistencePort.findById(itemId)
        .orElseThrow(() -> new IllegalArgumentException("Pantry item not found: " + itemId));

    // Apply partial updates only for non-null fields
    if (dto.getQuantity() != null) {
      existing.setQuantity(dto.getQuantity());
    }
    if (dto.getExpirationDate() != null) {
      existing.setExpirationDate(dto.getExpirationDate());
    }

    PantryItem updated = pantryItemPersistencePort.save(existing);
    Product product = productPersistencePort.findById(updated.getProductId())
        .orElseThrow(() -> new IllegalStateException("Product not found"));

    return toResponseDTO(updated, product);
  }

  @Override
  public void deleteItem(Long itemId, String username) {
    Long householdId = resolveHouseholdId(username);

    // Ownership check before deleting
    if (!pantryItemPersistencePort.existsByIdAndHouseholdId(itemId, householdId)) {
      throw new AccessDeniedException("Item does not belong to your household");
    }

    pantryItemPersistencePort.deleteById(itemId);
  }

  // Map domain objects to response DTO
  private PantryItemResponseDTO toResponseDTO(PantryItem item, Product product) {
    return PantryItemResponseDTO.builder()
        .id(item.getId())
        .productId(product.getId())
        .productName(product.getName())
        .productBarcode(product.getBarcode())
        .productImageUrl(product.getImageUrl())
        .standardUnit(product.getStandardUnit())
        .quantity(item.getQuantity())
        .expirationDate(item.getExpirationDate())
        .build();
  }
}
