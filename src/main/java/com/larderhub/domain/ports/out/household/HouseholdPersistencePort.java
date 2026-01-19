package com.larderhub.domain.ports.out.household;

import java.util.Optional;

import com.larderhub.domain.model.Household;

public interface HouseholdPersistencePort {
  Household save(Household household);

  Optional<Household> findById(Long id);

  Optional<Household> findByJoinCode(String joinCode);

  boolean existsByJoinCode(String joinCode);
}
