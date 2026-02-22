package com.larderhub.infrastructure.adapter.out.persistence.householdMember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseholdMemberJpaRepository extends JpaRepository<HouseholdMemberEntity, Long> {
  // Find the household membership for a given user (returns first match)
  Optional<HouseholdMemberEntity> findByUser_Id(Long userId);

  // Find ALL memberships for a given user (multi-household support)
  List<HouseholdMemberEntity> findAllByUser_Id(Long userId);

  // Find all members of a given household
  List<HouseholdMemberEntity> findByHousehold_Id(Long householdId);

  // Check if a user is already member of a household (prevent duplicates)
  boolean existsByUser_IdAndHousehold_Id(Long userId, Long householdId);

  // Find a specific membership by userId + householdId (used for removal and role
  // check)
  Optional<HouseholdMemberEntity> findByUser_IdAndHousehold_Id(Long userId, Long householdId);
}
