package com.larderhub.infrastructure.adapter.out.persistence.user;

import com.larderhub.domain.model.User;
import com.larderhub.domain.ports.out.user.UserPersistencePort;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

  @Override
  public List<User> searchByQuery(String query) {
    // Search for users whose username OR email contains the query
    // (case-insensitive)
    return userJpaRepository
        .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query)
        .stream()
        .map(userPersistenceMapper::toDomain)
        .collect(Collectors.toList());
  }
}
