package com.larderhub.infrastructure.adapter.out.persistence.push;

import com.larderhub.domain.model.PushSubscription;
import com.larderhub.domain.ports.out.push.PushSubscriptionPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PushSubscriptionPersistenceAdapter implements PushSubscriptionPersistencePort {

  private final PushSubscriptionJpaRepository jpaRepository;
  private final PushSubscriptionPersistenceMapper mapper;

  @Override
  public PushSubscription save(PushSubscription subscription) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(subscription)));
  }

  @Override
  public Optional<PushSubscription> findByUserId(Long userId) {
    return jpaRepository.findByUser_Id(userId).map(mapper::toDomain);
  }

  @Override
  public List<PushSubscription> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<PushSubscription> findByUserIds(List<Long> userIds) {
    if (userIds.isEmpty()) return List.of();
    return jpaRepository.findByUser_IdIn(userIds).stream().map(mapper::toDomain).toList();
  }

  @Override
  @Transactional
  public void deleteByUserId(Long userId) {
    jpaRepository.deleteByUser_Id(userId);
  }
}
