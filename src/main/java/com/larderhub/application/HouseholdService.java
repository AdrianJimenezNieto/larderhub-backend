package com.larderhub.application;

import com.larderhub.domain.model.Household;
import com.larderhub.domain.model.HouseholdMember;
import com.larderhub.domain.model.User;
import com.larderhub.domain.ports.in.household.HouseholdUseCase;
import com.larderhub.domain.ports.out.household.HouseholdPersistencePort;
import com.larderhub.domain.ports.out.householdMembers.HouseholdMembersPersistencePort;
import com.larderhub.domain.ports.out.user.UserPersistencePort;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.CreateHouseholdRequest;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.HouseholdResponse;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.InviteMemberRequest;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.JoinHouseholdRequest;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.MemberResponse;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.UserSearchResultDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HouseholdService implements HouseholdUseCase {

  private static final String ROLE_ADMIN = "ADMIN";
  private static final String ROLE_MEMBER = "MEMBER";

  private final HouseholdPersistencePort householdPersistencePort;
  private final HouseholdMembersPersistencePort householdMembersPersistencePort;
  private final UserPersistencePort userPersistencePort;

  // --- Use Cases ---

  @Override
  public HouseholdResponse createHousehold(CreateHouseholdRequest request, String username) {
    User user = resolveUser(username);

    // Generate a unique 8-character alphanumeric invite code
    String joinCode = generateUniqueJoinCode();

    Household household = Household.builder()
        .name(request.getName())
        .joinCode(joinCode)
        .build();

    Household saved = householdPersistencePort.save(household);

    // Creator automatically becomes ADMIN of the household
    HouseholdMember membership = HouseholdMember.builder()
        .userId(user.getId())
        .householdId(saved.getId())
        .role(ROLE_ADMIN)
        .build();
    householdMembersPersistencePort.save(membership);

    return toResponse(saved, ROLE_ADMIN);
  }

  @Override
  public HouseholdResponse joinHousehold(JoinHouseholdRequest request, String username) {
    User user = resolveUser(username);

    Household household = householdPersistencePort.findByJoinCode(request.getJoinCode())
        .orElseThrow(() -> new IllegalArgumentException("Invalid join code"));

    // Prevent a user from joining the same household twice
    if (householdMembersPersistencePort.existsByUserIdAndHouseholdId(user.getId(), household.getId())) {
      throw new IllegalArgumentException("You are already a member of this household");
    }

    HouseholdMember membership = HouseholdMember.builder()
        .userId(user.getId())
        .householdId(household.getId())
        .role(ROLE_MEMBER)
        .build();
    householdMembersPersistencePort.save(membership);

    return toResponse(household, ROLE_MEMBER);
  }

  @Override
  public List<HouseholdResponse> getMyHouseholds(String username) {
    User user = resolveUser(username);

    return householdMembersPersistencePort.findAllByUserId(user.getId()).stream()
        .map(membership -> {
          Household household = householdPersistencePort.findById(membership.getHouseholdId())
              .orElseThrow(() -> new IllegalStateException("Household not found for existing membership"));
          return toResponse(household, membership.getRole());
        })
        .collect(Collectors.toList());
  }

  @Override
  public List<MemberResponse> getMembers(Long householdId, String username) {
    User user = resolveUser(username);

    // Only members of the household can list its members
    if (!householdMembersPersistencePort.existsByUserIdAndHouseholdId(user.getId(), householdId)) {
      throw new AccessDeniedException("You are not a member of this household");
    }

    return householdMembersPersistencePort.findByHouseholdId(householdId).stream()
        .map(member -> {
          User memberUser = userPersistencePort.findById(member.getUserId())
              .orElseThrow(() -> new IllegalStateException("User not found for member"));
          return MemberResponse.builder()
              .userId(member.getUserId())
              .username(memberUser.getUsername())
              .role(member.getRole())
              .joinedAt(member.getJoinedAt())
              .build();
        })
        .collect(Collectors.toList());
  }

  // --- Slice 5: Admin member management ---

  @Override
  public List<UserSearchResultDTO> searchUser(Long householdId, String query, String adminUsername) {
    // Only an ADMIN of the household can search users
    requireAdmin(adminUsername, householdId);

    // Partial, case-insensitive search: username CONTAINS query OR email CONTAINS
    // query
    return userPersistencePort.searchByQuery(query).stream()
        .map(user -> UserSearchResultDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .alreadyMember(householdMembersPersistencePort
                .existsByUserIdAndHouseholdId(user.getId(), householdId))
            .build())
        .collect(Collectors.toList());
  }

  @Override
  public MemberResponse inviteMember(Long householdId, InviteMemberRequest request, String adminUsername) {
    requireAdmin(adminUsername, householdId);

    // Resolve target user by username or email
    User target = userPersistencePort.findByUsername(request.getQuery())
        .or(() -> userPersistencePort.findByEmail(request.getQuery()))
        .orElseThrow(() -> new IllegalArgumentException(
            "No user found with username or email: " + request.getQuery()));

    // Prevent adding someone already in the household
    if (householdMembersPersistencePort.existsByUserIdAndHouseholdId(target.getId(), householdId)) {
      throw new IllegalArgumentException(target.getUsername() + " is already a member of this household");
    }

    HouseholdMember membership = HouseholdMember.builder()
        .userId(target.getId())
        .householdId(householdId)
        .role(ROLE_MEMBER)
        .build();
    HouseholdMember saved = householdMembersPersistencePort.save(membership);

    return MemberResponse.builder()
        .userId(target.getId())
        .username(target.getUsername())
        .role(saved.getRole())
        .joinedAt(saved.getJoinedAt())
        .build();
  }

  @Override
  public void removeMember(Long householdId, Long targetUserId, String adminUsername) {
    User admin = requireAdmin(adminUsername, householdId);

    // An ADMIN cannot remove themselves
    if (admin.getId().equals(targetUserId)) {
      throw new IllegalArgumentException("You cannot remove yourself from the household");
    }

    // The target must actually be a member
    if (!householdMembersPersistencePort.existsByUserIdAndHouseholdId(targetUserId, householdId)) {
      throw new IllegalArgumentException("User " + targetUserId + " is not a member of this household");
    }

    householdMembersPersistencePort.deleteByUserIdAndHouseholdId(targetUserId, householdId);
  }

  // --- Helpers ---

  private User resolveUser(String username) {
    return userPersistencePort.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
  }

  private User requireAdmin(String username, Long householdId) {
    User user = resolveUser(username);
    HouseholdMember membership = householdMembersPersistencePort
        .findByUserIdAndHouseholdId(user.getId(), householdId)
        .orElseThrow(() -> new AccessDeniedException("You are not a member of household " + householdId));
    if (!ROLE_ADMIN.equals(membership.getRole())) {
      throw new AccessDeniedException("Only an ADMIN can perform this action");
    }
    return user;
  }

  private String generateUniqueJoinCode() {
    String code;
    do {
      // Use first 8 chars of a UUID (uppercase, no hyphens) as invite code
      code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    } while (householdPersistencePort.existsByJoinCode(code));
    return code;
  }

  private HouseholdResponse toResponse(Household household, String role) {
    return HouseholdResponse.builder()
        .id(household.getId())
        .name(household.getName())
        .joinCode(household.getJoinCode())
        .createdAt(household.getCreatedAt())
        .myRole(role)
        .build();
  }
}
