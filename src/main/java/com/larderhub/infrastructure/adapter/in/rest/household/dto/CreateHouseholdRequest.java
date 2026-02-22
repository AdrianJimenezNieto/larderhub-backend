package com.larderhub.infrastructure.adapter.in.rest.household.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateHouseholdRequest {

  @NotBlank(message = "Household name is required")
  @Size(max = 50, message = "Name must not exceed 50 characters")
  private String name;
}
