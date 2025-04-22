package com.larderhub.infrastructure.adapter.out.persistence.shopping;

import com.larderhub.domain.model.ShoppingItem;
import com.larderhub.domain.ports.out.shopping.ShoppingItemPersistencePort;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
// @Transactional ensures the session stays open when MapStruct resolves
// lazy-loaded relationships (household.id, product.id) after JPA save/find
@Transactional
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
    // The transaction stays open here, so lazy proxies (household/product)
    // can be initialized by MapStruct when toDomain() accesses their IDs
    ShoppingItemEntity saved = shoppingItemJpaRepository.save(entity);
    shoppingItemJpaRepository.flush(); // ensure the entity is fully persisted before mapping
    return shoppingItemPersistenceMapper.toDomain(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ShoppingItem> findById(Long id) {
    return shoppingItemJpaRepository.findById(id)
        .map(shoppingItemPersistenceMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ShoppingItem> findByHouseholdId(Long householdId) {
    return shoppingItemJpaRepository.findByHousehold_Id(householdId).stream()
        .map(shoppingItemPersistenceMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByIdAndHouseholdId(Long id, Long householdId) {
    return shoppingItemJpaRepository.existsByIdAndHousehold_Id(id, householdId);
  }

  @Override
  public void deleteById(Long id) {
    shoppingItemJpaRepository.deleteById(id);
  }

  @Override
  public void deleteAllByHouseholdId(Long householdId) {
    shoppingItemJpaRepository.deleteAllByHousehold_Id(householdId);
  }
}
