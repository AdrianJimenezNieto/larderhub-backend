package com.larderhub.infrastructure.adapter.out.persistence.push;

import com.larderhub.infrastructure.adapter.out.persistence.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "push_subscriptions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PushSubscriptionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private UserEntity user;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String endpoint;

  @Column(nullable = false, length = 512)
  private String p256dh;

  @Column(nullable = false, length = 512)
  private String auth;
}
