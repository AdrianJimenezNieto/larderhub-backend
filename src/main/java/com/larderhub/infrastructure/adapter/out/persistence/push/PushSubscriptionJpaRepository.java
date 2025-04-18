package com.larderhub.infrastructure.adapter.out.persistence.push;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushSubscriptionJpaRepository extends JpaRepository<PushSubscriptionEntity, Long> {
  Optional<PushSubscriptionEntity> findByUser_Id(Long userId);
  List<PushSubscriptionEntity> findByUser_IdIn(List<Long> userIds);
  void deleteByUser_Id(Long userId);
}
