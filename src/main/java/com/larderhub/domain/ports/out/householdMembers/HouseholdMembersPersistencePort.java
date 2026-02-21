package com.larderhub.domain.ports.out.householdMembers;

import com.larderhub.domain.model.HouseholdMember;

import java.util.Optional;

public interface HouseholdMembersPersistencePort {
  HouseholdMember save(HouseholdMember householdMember);

  Optional<HouseholdMember> findByUserId(Long userId);
}
