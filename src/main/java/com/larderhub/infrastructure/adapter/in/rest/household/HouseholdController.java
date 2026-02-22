package com.larderhub.infrastructure.adapter.in.rest.household;

import com.larderhub.domain.ports.in.household.HouseholdUseCase;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.CreateHouseholdRequest;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.HouseholdResponse;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.InviteMemberRequest;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.JoinHouseholdRequest;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.MemberResponse;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.UserSearchResultDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/households")
@RequiredArgsConstructor
public class HouseholdController {

  private final HouseholdUseCase householdUseCase;

  // POST /api/v1/households — Create a new household (caller becomes ADMIN)
  @PostMapping
  public ResponseEntity<HouseholdResponse> create(
      @Valid @RequestBody CreateHouseholdRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    HouseholdResponse response = householdUseCase.createHousehold(request, userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // POST /api/v1/households/join — Join an existing household via invite code
  @PostMapping("/join")
  public ResponseEntity<HouseholdResponse> join(
      @Valid @RequestBody JoinHouseholdRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    HouseholdResponse response = householdUseCase.joinHousehold(request, userDetails.getUsername());
    return ResponseEntity.ok(response);
  }

  // GET /api/v1/households/mine — List all households the user belongs to
  @GetMapping("/mine")
  public ResponseEntity<List<HouseholdResponse>> mine(
      @AuthenticationPrincipal UserDetails userDetails) {

    return ResponseEntity.ok(householdUseCase.getMyHouseholds(userDetails.getUsername()));
  }

  // GET /api/v1/households/{id}/members — List all members of a household
  @GetMapping("/{id}/members")
  public ResponseEntity<List<MemberResponse>> members(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {

    return ResponseEntity.ok(householdUseCase.getMembers(id, userDetails.getUsername()));
  }

  // GET /api/v1/households/{id}/members/search?q=username_or_email
  // [ADMIN] Search for users to invite by partial username or email
  // (case-insensitive)
  @GetMapping("/{id}/members/search")
  public ResponseEntity<List<UserSearchResultDTO>> searchUser(
      @PathVariable Long id,
      @RequestParam String q,
      @AuthenticationPrincipal UserDetails userDetails) {

    return ResponseEntity.ok(householdUseCase.searchUser(id, q, userDetails.getUsername()));
  }

  // POST /api/v1/households/{id}/members/invite
  // [ADMIN] Directly add a user to the household by username or email
  @PostMapping("/{id}/members/invite")
  public ResponseEntity<MemberResponse> inviteMember(
      @PathVariable Long id,
      @Valid @RequestBody InviteMemberRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    MemberResponse response = householdUseCase.inviteMember(id, request, userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // DELETE /api/v1/households/{id}/members/{memberId}
  // [ADMIN] Remove a member from the household (memberId = userId of the target)
  @DeleteMapping("/{id}/members/{memberId}")
  public ResponseEntity<Void> removeMember(
      @PathVariable Long id,
      @PathVariable Long memberId,
      @AuthenticationPrincipal UserDetails userDetails) {

    householdUseCase.removeMember(id, memberId, userDetails.getUsername());
    return ResponseEntity.noContent().build();
  }
}
