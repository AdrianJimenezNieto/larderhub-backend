package com.larderhub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PushSubscription {
  private Long id;
  private Long userId;
  private String endpoint;
  private String p256dh;
  private String auth;
}
