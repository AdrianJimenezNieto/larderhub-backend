package com.larderhub.domain.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
public class Household {
  private Long id;
  private String name;
  private String joinCode;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
