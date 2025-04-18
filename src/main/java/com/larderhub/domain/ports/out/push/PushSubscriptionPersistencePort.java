package com.larderhub.domain.ports.out.push;

import com.larderhub.domain.model.PushSubscription;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionPersistencePort {
  PushSubscription save(PushSubscription subscription);
  Optional<PushSubscription> findByUserId(Long userId);
  List<PushSubscription> findAll();
  List<PushSubscription> findByUserIds(List<Long> userIds);
  void deleteByUserId(Long userId);
}
