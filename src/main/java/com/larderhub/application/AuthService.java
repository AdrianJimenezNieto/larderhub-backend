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
    // Validate that the user does not already exist
    if (userPersistencePort.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already registered");
    }
    if (userPersistencePort.existsByUsername(request.getUsername())) {
      throw new IllegalArgumentException("Username already taken");
    }

    // Build and persist the new user with hashed password
    User newUser = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role("USER")
        .build();

    User savedUser = userPersistencePort.save(newUser);

    // Generate JWT and return response
    String token = jwtService.generateToken(savedUser.getUsername());
    return AuthResponse.builder()
        .token(token)
        .username(savedUser.getUsername())
        .email(savedUser.getEmail())
        .build();
  }

  @Override
  public AuthResponse login(LoginRequest request) {
    // Find user by email and validate credentials
    User user = userPersistencePort.findByEmail(request.getEmail())
        .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("Invalid credentials");
    }

    // Generate JWT and return response
    String token = jwtService.generateToken(user.getUsername());
    return AuthResponse.builder()
        .token(token)
        .username(user.getUsername())
        .email(user.getEmail())
        .build();
  }
}
