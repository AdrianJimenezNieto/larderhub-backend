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
}
