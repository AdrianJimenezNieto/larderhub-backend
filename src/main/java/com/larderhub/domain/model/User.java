package com.larderhub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
  private Long id;
  private String username;
  private String email;
  private String password;
  private String avatarUrl;
  private String role;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
