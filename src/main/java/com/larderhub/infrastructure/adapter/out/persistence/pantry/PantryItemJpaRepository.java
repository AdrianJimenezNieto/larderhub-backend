package com.larderhub.infrastructure.adapter.out.persistence.pantry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PantryItemJpaRepository extends JpaRepository<PantryItemEntity, Long> {
  List<PantryItemEntity> findByHousehold_Id(Long householdId);

  boolean existsByIdAndHousehold_Id(Long id, Long householdId);

  // For idempotent additions (summing quantity if product already exists)
  Optional<PantryItemEntity> findByHousehold_IdAndProduct_Id(Long householdId, Long productId);

  // For expired items alerts (expirationDate < current date)
  List<PantryItemEntity> findByHousehold_IdAndExpirationDateBefore(Long householdId, LocalDate date);

  // For expiring items alerts (current date <= expirationDate <= target date)
  List<PantryItemEntity> findByHousehold_IdAndExpirationDateBetween(
      Long householdId, LocalDate startDate, LocalDate endDate);
}
