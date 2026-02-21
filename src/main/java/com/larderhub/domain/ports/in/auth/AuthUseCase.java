package com.larderhub.domain.ports.in.auth;

import com.larderhub.infrastructure.adapter.in.rest.auth.dto.AuthResponse;
import com.larderhub.infrastructure.adapter.in.rest.auth.dto.LoginRequest;
import com.larderhub.infrastructure.adapter.in.rest.auth.dto.RegisterRequest;

public interface AuthUseCase {
  AuthResponse register(RegisterRequest request);

  AuthResponse login(LoginRequest request);
}
