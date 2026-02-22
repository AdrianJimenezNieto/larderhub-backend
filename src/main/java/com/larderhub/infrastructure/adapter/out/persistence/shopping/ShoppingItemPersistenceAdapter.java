package com.larderhub.infrastructure.adapter.out.persistence.shopping;

import com.larderhub.domain.model.ShoppingItem;
import com.larderhub.domain.ports.out.shopping.ShoppingItemPersistencePort;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ShoppingItemPersistenceAdapter implements ShoppingItemPersistencePort {

  private final ShoppingItemJpaRepository shoppingItemJpaRepository;
  private final ShoppingItemPersistenceMapper shoppingItemPersistenceMapper;

  public ShoppingItemPersistenceAdapter(
      ShoppingItemJpaRepository shoppingItemJpaRepository,
      ShoppingItemPersistenceMapper shoppingItemPersistenceMapper) {
    this.shoppingItemJpaRepository = shoppingItemJpaRepository;
    this.shoppingItemPersistenceMapper = shoppingItemPersistenceMapper;
  }

  @Override
  public ShoppingItem save(ShoppingItem item) {
    ShoppingItemEntity entity = shoppingItemPersistenceMapper.toEntity(item);
    return shoppingItemPersistenceMapper.toDomain(shoppingItemJpaRepository.save(entity));
  }

  @Override
  public Optional<ShoppingItem> findById(Long id) {
    return shoppingItemJpaRepository.findById(id)
        .map(shoppingItemPersistenceMapper::toDomain);
  }

  @Override
  public List<ShoppingItem> findByHouseholdId(Long householdId) {
    return shoppingItemJpaRepository.findByHousehold_Id(householdId).stream()
        .map(shoppingItemPersistenceMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public boolean existsByIdAndHouseholdId(Long id, Long householdId) {
    return shoppingItemJpaRepository.existsByIdAndHousehold_Id(id, householdId);
  }

  @Override
  public void deleteById(Long id) {
    shoppingItemJpaRepository.deleteById(id);
  }
}
