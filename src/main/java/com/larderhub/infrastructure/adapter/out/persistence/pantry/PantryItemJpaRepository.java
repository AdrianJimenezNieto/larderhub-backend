package com.larderhub.infrastructure.adapter.out.persistence.pantry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PantryItemJpaRepository extends JpaRepository<PantryItemEntity, Long> {
  List<PantryItemEntity> findByHousehold_Id(Long householdId);

  boolean existsByIdAndHousehold_Id(Long id, Long householdId);
}
