package com.larderhub.infrastructure.adapter.in.rest.household.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HouseholdResponse {
  private Long id;
  private String name;
  private String joinCode;
  private LocalDateTime createdAt;
  // Role of the requesting user within this household
  private String myRole;
}
