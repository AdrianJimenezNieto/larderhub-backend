package com.larderhub.infrastructure.adapter.out.persistence.household;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseholdJpaRepository extends JpaRepository<HouseholdEntity, Long> {
  Optional<HouseholdEntity> findByJoinCode(String joinCode);

  boolean existsByJoinCode(String joinCode);
}
