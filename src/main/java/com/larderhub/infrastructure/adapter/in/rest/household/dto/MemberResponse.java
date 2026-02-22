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
public class MemberResponse {
  private Long userId;
  private String username;
  private String role;
  private LocalDateTime joinedAt;
}
