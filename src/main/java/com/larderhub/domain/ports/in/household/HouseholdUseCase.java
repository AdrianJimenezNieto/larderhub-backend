package com.larderhub.domain.ports.in.household;

import com.larderhub.infrastructure.adapter.in.rest.household.dto.CreateHouseholdRequest;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.HouseholdResponse;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.JoinHouseholdRequest;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.MemberResponse;

import java.util.List;

public interface HouseholdUseCase {
  // Create a new household; the creator automatically becomes ADMIN
  HouseholdResponse createHousehold(CreateHouseholdRequest request, String username);

  // Join an existing household using its invite code
  HouseholdResponse joinHousehold(JoinHouseholdRequest request, String username);

  // List all households the authenticated user belongs to
  List<HouseholdResponse> getMyHouseholds(String username);

  // List all members of a household (user must be a member)
  List<MemberResponse> getMembers(Long householdId, String username);
}
