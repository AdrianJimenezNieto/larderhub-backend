package com.larderhub.infrastructure.adapter.in.rest.household.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO used when an ADMIN searches for a user to invite.
 * Only exposes safe, non-sensitive fields.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchResultDTO {
  private Long id;
  private String username;
  private String email;
  // True if the user is already a member of the household being managed
  private boolean alreadyMember;
}
