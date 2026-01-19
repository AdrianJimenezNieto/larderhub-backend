package com.larderhub.domain.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class HouseholdMember {
  private Long userId;
  private Long householdId;
  private String role;
  private LocalDateTime joinedAt;
}
