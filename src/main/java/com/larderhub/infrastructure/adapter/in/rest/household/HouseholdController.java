package com.larderhub.infrastructure.adapter.in.rest.household;

import com.larderhub.domain.ports.in.household.HouseholdUseCase;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.CreateHouseholdRequest;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.HouseholdResponse;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.ChangeRoleRequest;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.InviteMemberRequest;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.JoinHouseholdRequest;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.MemberResponse;
import com.larderhub.infrastructure.adapter.in.rest.household.dto.UserSearchResultDTO;
import com.larderhub.domain.ports.in.recipe.RecipeUseCase;
import com.larderhub.infrastructure.adapter.in.rest.recipe.dto.RecipeSuggestionDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
  private final RecipeUseCase recipeUseCase;

  @PostMapping
  public ResponseEntity<HouseholdResponse> create(
      @Valid @RequestBody CreateHouseholdRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    HouseholdResponse response = householdUseCase.createHousehold(request, userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/join")
  public ResponseEntity<HouseholdResponse> join(
      @Valid @RequestBody JoinHouseholdRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    HouseholdResponse response = householdUseCase.joinHousehold(request, userDetails.getUsername());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/mine")
  public ResponseEntity<List<HouseholdResponse>> mine(
      @AuthenticationPrincipal UserDetails userDetails) {

    return ResponseEntity.ok(householdUseCase.getMyHouseholds(userDetails.getUsername()));
  }

  @GetMapping("/{id}/members")
  public ResponseEntity<List<MemberResponse>> members(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {

    return ResponseEntity.ok(householdUseCase.getMembers(id, userDetails.getUsername()));
  }

  // solo admins pueden invitar o buscar usuarios
  @GetMapping("/{id}/members/search")
  public ResponseEntity<List<UserSearchResultDTO>> searchUser(
      @PathVariable Long id,
      @RequestParam String q,
      @AuthenticationPrincipal UserDetails userDetails) {

    return ResponseEntity.ok(householdUseCase.searchUser(id, q, userDetails.getUsername()));
  }

  @PostMapping("/{id}/members/invite")
  public ResponseEntity<MemberResponse> inviteMember(
      @PathVariable Long id,
      @Valid @RequestBody InviteMemberRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    MemberResponse response = householdUseCase.inviteMember(id, request, userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/{id}/members/{memberId}")
  public ResponseEntity<Void> removeMember(
      @PathVariable Long id,
      @PathVariable Long memberId,
      @AuthenticationPrincipal UserDetails userDetails) {

    householdUseCase.removeMember(id, memberId, userDetails.getUsername());
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/members/{userId}/role")
  public ResponseEntity<MemberResponse> changeRole(
      @PathVariable Long id,
      @PathVariable Long userId,
      @Valid @RequestBody ChangeRoleRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    MemberResponse response = householdUseCase.changeRole(id, userId, request.getRole(), userDetails.getUsername());
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> dissolve(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {

    householdUseCase.dissolveHousehold(id, userDetails.getUsername());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/recipes/suggestions")
  public ResponseEntity<List<RecipeSuggestionDTO>> getRecipeSuggestions(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {

    return ResponseEntity.ok(recipeUseCase.getSuggestionsForHousehold(id, userDetails.getUsername()));
  }

  @PostMapping("/{id}/recipes/{recipeId}/add-missing")
  public ResponseEntity<Map<String, Integer>> addMissingToShoppingList(
      @PathVariable Long id,
      @PathVariable Long recipeId,
      @AuthenticationPrincipal UserDetails userDetails) {

    int count = recipeUseCase.addMissingToShoppingList(id, recipeId, userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("itemsAdded", count));
  }
}
