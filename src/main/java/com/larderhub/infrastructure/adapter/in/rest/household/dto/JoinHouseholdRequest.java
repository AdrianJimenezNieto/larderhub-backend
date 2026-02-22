package com.larderhub.infrastructure.adapter.in.rest.household.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JoinHouseholdRequest {

  @NotBlank(message = "Join code is required")
  @Size(min = 8, max = 8, message = "Join code must be exactly 8 characters")
  private String joinCode;
}
