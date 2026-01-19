package com.larderhub.domain.ports.out.householdMembers;

import com.larderhub.domain.model.HouseholdMember;

public interface HouseholdMembersPersistencePort {
  HouseholdMember save(HouseholdMember householdMember);
}
