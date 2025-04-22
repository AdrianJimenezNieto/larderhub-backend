package com.larderhub.application;

import com.larderhub.domain.model.User;
import com.larderhub.domain.ports.in.auth.AuthUseCase;
import com.larderhub.domain.ports.out.user.UserPersistencePort;
import com.larderhub.infrastructure.adapter.in.rest.auth.dto.AuthResponse;
import com.larderhub.infrastructure.adapter.in.rest.auth.dto.LoginRequest;
import com.larderhub.infrastructure.adapter.in.rest.auth.dto.RegisterRequest;
import com.larderhub.infrastructure.adapter.in.rest.config.JwtService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

  private final UserPersistencePort userPersistencePort;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @Override
  public AuthResponse register(RegisterRequest request) {
    if (userPersistencePort.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already registered");
    }
    if (userPersistencePort.existsByUsername(request.getUsername())) {
      throw new IllegalArgumentException("Username already taken");
    }

    User newUser = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role("USER")
        .build();

    User savedUser = userPersistencePort.save(newUser);

    // devolvemos token + datos para que el frontend no haga otro request
    String token = jwtService.generateToken(savedUser.getUsername());
    return AuthResponse.builder()
        .token(token)
        .username(savedUser.getUsername())
        .email(savedUser.getEmail())
        .user(AuthResponse.UserPayload.builder()
            .id(savedUser.getId())
            .name(savedUser.getUsername())
            .email(savedUser.getEmail())
            .build())
        .build();
  }

  @Override
  public AuthResponse login(LoginRequest request) {
    User user = userPersistencePort.findByEmail(request.getEmail())
        .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("Invalid credentials");
    }

    String token = jwtService.generateToken(user.getUsername());
    return AuthResponse.builder()
        .token(token)
        .username(user.getUsername())
        .email(user.getEmail())
        .user(AuthResponse.UserPayload.builder()
            .id(user.getId())
            .name(user.getUsername())
            .email(user.getEmail())
            .build())
        .build();
  }
}
