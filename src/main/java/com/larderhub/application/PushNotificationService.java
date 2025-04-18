package com.larderhub.application;

import com.larderhub.domain.model.PushSubscription;
import com.larderhub.domain.model.User;
import com.larderhub.domain.ports.in.push.PushNotificationUseCase;
import com.larderhub.domain.ports.out.push.PushSubscriptionPersistencePort;
import com.larderhub.domain.ports.out.user.UserPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushNotificationService implements PushNotificationUseCase {

  private final PushSubscriptionPersistencePort subscriptionPort;
  private final UserPersistencePort userPort;

  @Override
  @Transactional
  public void subscribe(String endpoint, String p256dh, String auth, String username) {
    User user = userPort.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

    // Delete existing subscription if any (upsert)
    subscriptionPort.findByUserId(user.getId()).ifPresent(s -> subscriptionPort.deleteByUserId(user.getId()));

    PushSubscription subscription = PushSubscription.builder()
        .userId(user.getId())
        .endpoint(endpoint)
        .p256dh(p256dh)
        .auth(auth)
        .build();
    subscriptionPort.save(subscription);
  }

  @Override
  @Transactional
  public void unsubscribe(String username) {
    User user = userPort.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    subscriptionPort.deleteByUserId(user.getId());
  }
}
