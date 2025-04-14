package com.larderhub.domain.ports.out.householdMembers;

import com.larderhub.domain.model.HouseholdMember;

import java.util.List;
import java.util.Optional;

public interface HouseholdMembersPersistencePort {
  HouseholdMember save(HouseholdMember householdMember);

  // Returns the first household of a user (used by InventoryService)
  Optional<HouseholdMember> findByUserId(Long userId);

  // Returns ALL households a user belongs to (used by HouseholdService)
  List<HouseholdMember> findAllByUserId(Long userId);

  List<HouseholdMember> findByHouseholdId(Long householdId);

  boolean existsByUserIdAndHouseholdId(Long userId, Long householdId);

  // Find a specific membership (needed to resolve the member's DB id for deletion
  // / role check)
  Optional<HouseholdMember> findByUserIdAndHouseholdId(Long userId, Long householdId);

  // Remove a user from a household
  void deleteByUserIdAndHouseholdId(Long userId, Long householdId);

  // [Slice 5] Update a member's role within a household
  void updateRole(Long userId, Long householdId, String role);

  // [Slice 5] Remove all members from a household (used when dissolving)
  void deleteAllByHouseholdId(Long householdId);

  // [Slice 5] Count members with a specific role (used to guard last-admin check)
  long countByHouseholdIdAndRole(Long householdId, String role);
}
