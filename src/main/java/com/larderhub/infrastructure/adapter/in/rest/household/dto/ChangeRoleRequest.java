package com.larderhub.infrastructure.adapter.in.rest.household.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangeRoleRequest {

  @NotBlank(message = "role is required")
  @Pattern(regexp = "ADMIN|MEMBER", message = "role must be ADMIN or MEMBER")
  private String role;
}
