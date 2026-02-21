package com.larderhub.infrastructure.adapter.out.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByEmail(String email);

  Optional<UserEntity> findByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}
