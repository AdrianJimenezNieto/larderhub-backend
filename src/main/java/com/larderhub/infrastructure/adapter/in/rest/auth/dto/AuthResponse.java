package com.larderhub.infrastructure.adapter.in.rest.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
  private String token;
  private String username;
  private String email;

  /** Nested user object expected by the frontend AuthResponse type. */
  private UserPayload user;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class UserPayload {
    private Long id;
    private String name;
    private String email;
  }
}
