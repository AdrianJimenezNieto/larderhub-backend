package com.larderhub.infrastructure.adapter.out.persistence.householdMember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseholdMemberJpaRepository extends JpaRepository<HouseholdMemberEntity, Long> {
  Optional<HouseholdMemberEntity> findByUser_Id(Long userId);

  List<HouseholdMemberEntity> findAllByUser_Id(Long userId);

  List<HouseholdMemberEntity> findByHousehold_Id(Long householdId);

  boolean existsByUser_IdAndHousehold_Id(Long userId, Long householdId);

  Optional<HouseholdMemberEntity> findByUser_IdAndHousehold_Id(Long userId, Long householdId);

  // [Slice 5] Update role in-place without a round-trip load
  @Modifying
  @Query("UPDATE HouseholdMemberEntity m SET m.role = :role WHERE m.user.id = :userId AND m.household.id = :householdId")
  void updateRole(@Param("userId") Long userId, @Param("householdId") Long householdId, @Param("role") String role);

  // [Slice 5] Remove all members when dissolving a household
  void deleteAllByHousehold_Id(Long householdId);

  // [Slice 5] Count members by role (used to guard last-admin check)
  long countByHousehold_IdAndRole(Long householdId, String role);
}
