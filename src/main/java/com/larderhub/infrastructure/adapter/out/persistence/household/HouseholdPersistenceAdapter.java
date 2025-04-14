package com.larderhub.infrastructure.adapter.out.persistence.household;

import org.springframework.stereotype.Repository;

import com.larderhub.domain.model.Household;
import com.larderhub.domain.ports.out.household.HouseholdPersistencePort;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HouseholdPersistenceAdapter implements HouseholdPersistencePort {
  
  // Inject dependencies
  private final HouseholdJpaRepository householdJpaRepository;
  private final HouseholdPersistenceMapper householdPersistenceMapper;

  // Implement methods of HouseholdPersistencePort interface
  @Override
  public Household save(Household household) {
    HouseholdEntity entity = householdPersistenceMapper.toEntity(household);
    HouseholdEntity savedEntity = householdJpaRepository.save(entity);
    return householdPersistenceMapper.toDomain(savedEntity);
  }

  @Override
  public Optional<Household> findById(Long id) {
    return householdJpaRepository.findById(id)
        .map(householdPersistenceMapper::toDomain);
  }

  @Override
  public Optional<Household> findByJoinCode(String joinCode) {
    return householdJpaRepository.findByJoinCode(joinCode)
        .map(householdPersistenceMapper::toDomain);
  }

  @Override
  public boolean existsByJoinCode(String joinCode) {
    return householdJpaRepository.existsByJoinCode(joinCode);
  }

  @Override
  public void deleteById(Long id) {
    householdJpaRepository.deleteById(id);
  }
}
