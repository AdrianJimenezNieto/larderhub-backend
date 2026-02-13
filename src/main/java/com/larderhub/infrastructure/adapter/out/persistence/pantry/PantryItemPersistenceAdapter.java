package com.larderhub.infrastructure.adapter.out.persistence.pantry;

import com.larderhub.domain.model.PantryItem;
import com.larderhub.domain.ports.out.pantry.PantryItemPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PantryItemPersistenceAdapter implements PantryItemPersistencePort {

  private final PantryItemJpaRepository pantryItemJpaRepository;
  private final PantryItemPersistenceMapper pantryItemPersistenceMapper;

  @Override
  public PantryItem save(PantryItem pantryItem) {
    PantryItemEntity entity = pantryItemPersistenceMapper.toEntity(pantryItem);
    return pantryItemPersistenceMapper.toDomain(pantryItemJpaRepository.save(entity));
  }

  @Override
  public Optional<PantryItem> findById(Long id) {
    return pantryItemJpaRepository.findById(id)
        .map(pantryItemPersistenceMapper::toDomain);
  }

  @Override
  public List<PantryItem> findByHouseholdId(Long householdId) {
    return pantryItemJpaRepository.findByHousehold_Id(householdId).stream()
        .map(pantryItemPersistenceMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteById(Long id) {
    pantryItemJpaRepository.deleteById(id);
  }
}
