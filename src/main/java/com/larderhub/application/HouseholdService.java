package com.larderhub.application;

import com.larderhub.domain.model.Household;
import com.larderhub.domain.model.HouseholdMember;
import com.larderhub.domain.model.User;
import com.larderhub.domain.ports.in.household.HouseholdUseCase;
import com.larderhub.domain.ports.out.household.HouseholdPersistencePort;
import com.larderhub.domain.ports.out.householdMembers.HouseholdMembersPersistencePort;
import com.larderhub.domain.ports.out.pantry.PantryItemPersistencePort;
import com.larderhub.domain.ports.out.shopping.ShoppingItemPersistencePort;
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
  private final PantryItemPersistencePort pantryItemPersistencePort;
  private final ShoppingItemPersistencePort shoppingItemPersistencePort;

  @Override
  public HouseholdResponse createHousehold(CreateHouseholdRequest request, String username) {
    User user = resolveUser(username);

    String joinCode = generateUniqueJoinCode();

    Household household = Household.builder()
        .name(request.getName())
        .joinCode(joinCode)
        .build();

    Household saved = householdPersistencePort.save(household);

    // el que crea el hogar es automáticamente ADMIN
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

    // evitar que alguien entre dos veces al mismo hogar
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

  @Override
  public List<UserSearchResultDTO> searchUser(Long householdId, String query, String adminUsername) {
    requireAdmin(adminUsername, householdId);

    // búsqueda parcial por username o email
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

    User target = userPersistencePort.findByUsername(request.getQuery())
        .or(() -> userPersistencePort.findByEmail(request.getQuery()))
        .orElseThrow(() -> new IllegalArgumentException(
            "No user found with username or email: " + request.getQuery()));

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

    if (admin.getId().equals(targetUserId)) {
      throw new IllegalArgumentException("You cannot remove yourself from the household");
    }

    if (!householdMembersPersistencePort.existsByUserIdAndHouseholdId(targetUserId, householdId)) {
      throw new IllegalArgumentException("User " + targetUserId + " is not a member of this household");
    }

    householdMembersPersistencePort.deleteByUserIdAndHouseholdId(targetUserId, householdId);
  }

  @Override
  public MemberResponse changeRole(Long householdId, Long targetUserId, String newRole, String adminUsername) {
    requireAdmin(adminUsername, householdId);

    HouseholdMember membership = householdMembersPersistencePort
        .findByUserIdAndHouseholdId(targetUserId, householdId)
        .orElseThrow(() -> new IllegalArgumentException("User " + targetUserId + " is not a member of this household"));

    // no puede quedarse el hogar sin ningún admin
    if (ROLE_MEMBER.equals(newRole) && ROLE_ADMIN.equals(membership.getRole())) {
      long adminCount = householdMembersPersistencePort.countByHouseholdIdAndRole(householdId, ROLE_ADMIN);
      if (adminCount <= 1) {
        throw new IllegalArgumentException("El household necesita al menos un ADMIN");
      }
    }

    householdMembersPersistencePort.updateRole(targetUserId, householdId, newRole);

    User targetUser = userPersistencePort.findById(targetUserId)
        .orElseThrow(() -> new IllegalStateException("User not found: " + targetUserId));

    return MemberResponse.builder()
        .userId(targetUserId)
        .username(targetUser.getUsername())
        .role(newRole)
        .joinedAt(membership.getJoinedAt())
        .build();
  }

  @Override
  public void dissolveHousehold(Long householdId, String adminUsername) {
    requireAdmin(adminUsername, householdId);

    // borramos en orden: items → miembros → hogar
    shoppingItemPersistencePort.deleteAllByHouseholdId(householdId);
    pantryItemPersistencePort.deleteAllByHouseholdId(householdId);
    householdMembersPersistencePort.deleteAllByHouseholdId(householdId);
    householdPersistencePort.deleteById(householdId);
  }

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
