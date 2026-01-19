package com.larderhub.domain.ports.out.user;

import com.larderhub.domain.model.User;

import java.util.Optional;

public interface UserPersistencePort {
  User save(User user);

  Optional<User> findById(Long id);

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}
