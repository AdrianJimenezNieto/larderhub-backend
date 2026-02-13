package com.larderhub.infrastructure.adapter.out.persistence.shoppingList;

import com.larderhub.domain.model.ShoppingListItem;
import com.larderhub.domain.ports.out.shoppingList.ShoppingListItemPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ShoppingListItemPersistenceAdapter implements ShoppingListItemPersistencePort {

  private final ShoppingListItemJpaRepository shoppingListItemJpaRepository;
  private final ShoppingListItemPersistenceMapper shoppingListItemPersistenceMapper;

  @Override
  public ShoppingListItem save(ShoppingListItem shoppingListItem) {
    ShoppingListItemEntity entity = shoppingListItemPersistenceMapper.toEntity(shoppingListItem);
    return shoppingListItemPersistenceMapper.toDomain(shoppingListItemJpaRepository.save(entity));
  }

  @Override
  public Optional<ShoppingListItem> findById(Long id) {
    return shoppingListItemJpaRepository.findById(id)
        .map(shoppingListItemPersistenceMapper::toDomain);
  }

  @Override
  public List<ShoppingListItem> findByHouseholdId(Long householdId) {
    return shoppingListItemJpaRepository.findByHousehold_Id(householdId).stream()
        .map(shoppingListItemPersistenceMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteById(Long id) {
    shoppingListItemJpaRepository.deleteById(id);
  }
}
