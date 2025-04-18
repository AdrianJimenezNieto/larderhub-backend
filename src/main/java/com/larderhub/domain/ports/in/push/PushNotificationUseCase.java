package com.larderhub.domain.ports.in.push;

public interface PushNotificationUseCase {
  void subscribe(String endpoint, String p256dh, String auth, String username);
  void unsubscribe(String username);
}
