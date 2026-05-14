package com.larderhub.application;

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
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService implements InventoryUseCase {

  private final PantryItemPersistencePort pantryItemPersistencePort;
  private final ProductPersistencePort productPersistencePort;
  private final UserPersistencePort userPersistencePort;
  private final HouseholdMembersPersistencePort householdMembersPersistencePort;
  private final PushSenderService pushSenderService;

  // comprueba que el usuario sea miembro del household antes de tocar nada
  private User resolveAndValidateMembership(String username, Long householdId) {
    User user = userPersistencePort.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

    if (!householdMembersPersistencePort.existsByUserIdAndHouseholdId(user.getId(), householdId)) {
      throw new AccessDeniedException("You are not a member of household " + householdId);
    }

    return user;
  }

  @Override
  public PantryItemResponseDTO addItem(Long householdId, PantryItemCreateDTO dto, String username) {
    User actor = resolveAndValidateMembership(username, householdId);

    Product product = productPersistencePort.findById(dto.getProductId())
        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + dto.getProductId()));

    // si ya existe el producto en la despensa, sumamos cantidad
    PantryItemResponseDTO result = pantryItemPersistencePort.findByHouseholdIdAndProductId(householdId, product.getId())
        .map(existingItem -> {
          existingItem.setQuantity(existingItem.getQuantity().add(dto.getQuantity()));

          // guardamos la más cercana: más conservadora para seguridad alimentaria
          LocalDate existingDate = existingItem.getExpirationDate();
          LocalDate incomingDate = dto.getExpirationDate();
          if (existingDate != null && incomingDate != null) {
            existingItem.setExpirationDate(existingDate.isBefore(incomingDate) ? existingDate : incomingDate);
          } else if (incomingDate != null) {
            existingItem.setExpirationDate(incomingDate);
          }

          PantryItem updated = pantryItemPersistencePort.save(existingItem);
          return toResponseDTO(updated, product);
        })
        .orElseGet(() -> {
          PantryItem newItem = PantryItem.builder()
              .householdId(householdId)
              .productId(product.getId())
              .quantity(dto.getQuantity())
              .expirationDate(dto.getExpirationDate())
              .build();

          PantryItem saved = pantryItemPersistencePort.save(newItem);
          return toResponseDTO(saved, product);
        });

    // push a los demás miembros, si falla no corta el flujo
    try {
      pushSenderService.sendToHouseholdMembers(
          householdId,
          actor.getId(),
          "LarderHub",
          actor.getUsername() + " ha añadido " + product.getName() + " a la despensa"
      );
    } catch (Exception e) {
      log.warn("Push notification failed for household {}: {}", householdId, e.getMessage());
    }

    return result;
  }

  @Override
  public List<PantryItemResponseDTO> listItems(Long householdId, String username) {
    resolveAndValidateMembership(username, householdId);

    Map<Long, Product> productsById = productPersistencePort.findAll().stream()
        .collect(Collectors.toMap(Product::getId, p -> p));

    return pantryItemPersistencePort.findByHouseholdId(householdId).stream()
        .map(item -> {
          Product product = productsById.get(item.getProductId());
          if (product == null) {
            log.warn("Orphaned pantry item id={} references missing product id={}", item.getId(), item.getProductId());
            return null;
          }
          return toResponseDTO(item, product);
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public PantryItemResponseDTO updateItem(Long householdId, Long itemId, PantryItemUpdateDTO dto, String username) {
    resolveAndValidateMembership(username, householdId);

    // que el item sea de este household y no de otro
    if (!pantryItemPersistencePort.existsByIdAndHouseholdId(itemId, householdId)) {
      throw new AccessDeniedException("Item " + itemId + " does not belong to household " + householdId);
    }

    PantryItem existing = pantryItemPersistencePort.findById(itemId)
        .orElseThrow(() -> new IllegalArgumentException("Pantry item not found: " + itemId));

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
  public void deleteItem(Long householdId, Long itemId, String username) {
    resolveAndValidateMembership(username, householdId);

    if (!pantryItemPersistencePort.existsByIdAndHouseholdId(itemId, householdId)) {
      throw new AccessDeniedException("Item " + itemId + " does not belong to household " + householdId);
    }

    pantryItemPersistencePort.deleteById(itemId);
  }

  @Override
  public List<PantryItemResponseDTO> getExpiredItems(Long householdId, String username) {
    resolveAndValidateMembership(username, householdId);

    LocalDate today = LocalDate.now();
    Map<Long, Product> productsById = productPersistencePort.findAll().stream()
        .collect(Collectors.toMap(Product::getId, p -> p));

    return pantryItemPersistencePort.findExpiredItems(householdId, today).stream()
        .map(item -> {
          Product product = productsById.get(item.getProductId());
          if (product == null) {
            log.warn("Orphaned pantry item id={} references missing product id={}", item.getId(), item.getProductId());
            return null;
          }
          return toResponseDTO(item, product);
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public List<PantryItemResponseDTO> getExpiringItems(Long householdId, int daysAhead, String username) {
    resolveAndValidateMembership(username, householdId);

    LocalDate today = LocalDate.now();
    LocalDate limitDate = today.plusDays(daysAhead);
    Map<Long, Product> productsById = productPersistencePort.findAll().stream()
        .collect(Collectors.toMap(Product::getId, p -> p));

    return pantryItemPersistencePort.findExpiringItems(householdId, today, limitDate).stream()
        .map(item -> {
          Product product = productsById.get(item.getProductId());
          if (product == null) {
            log.warn("Orphaned pantry item id={} references missing product id={}", item.getId(), item.getProductId());
            return null;
          }
          return toResponseDTO(item, product);
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

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
