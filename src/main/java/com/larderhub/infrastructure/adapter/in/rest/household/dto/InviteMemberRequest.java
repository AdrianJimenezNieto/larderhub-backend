package com.larderhub.infrastructure.adapter.in.rest.household.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for the ADMIN to invite a user to a household by
 * username or email (whichever is provided).
 */
@Data
public class InviteMemberRequest {

  @NotBlank(message = "query is required (username or email)")
  // Can be either a username or an email address
  private String query;
}
