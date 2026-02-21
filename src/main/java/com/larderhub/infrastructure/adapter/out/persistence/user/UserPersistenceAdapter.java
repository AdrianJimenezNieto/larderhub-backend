package com.larderhub.infrastructure.adapter.out.persistence.user;

import com.larderhub.domain.model.User;
import com.larderhub.domain.ports.out.user.UserPersistencePort;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

  // Inject dependencies
  private final UserJpaRepository userJpaRepository;
  private final UserPersistenceMapper userPersistenceMapper;

  // Implement methods of UserPersistencePort interface
  @Override
  public User save(User user) {
    UserEntity entity = userPersistenceMapper.toEntity(user);
    UserEntity savedEntity = userJpaRepository.save(entity);
    return userPersistenceMapper.toDomain(savedEntity);
  }

  @Override
  public Optional<User> findById(Long id) {
    return userJpaRepository.findById(id)
        .map(userPersistenceMapper::toDomain);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return userJpaRepository.findByEmail(email)
        .map(userPersistenceMapper::toDomain);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return userJpaRepository.findByUsername(username)
        .map(userPersistenceMapper::toDomain);
  }

  @Override
  public boolean existsByEmail(String email) {
    return userJpaRepository.existsByEmail(email);
  }

  @Override
  public boolean existsByUsername(String username) {
    return userJpaRepository.existsByUsername(username);
  }
}
