package com.larderhub.infrastructure.adapter.out.persistence.householdMember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HouseholdMemberJpaRepository extends JpaRepository<HouseholdMemberEntity, Long> {
  // Find the household membership for a given user
  Optional<HouseholdMemberEntity> findByUser_Id(Long userId);
}
