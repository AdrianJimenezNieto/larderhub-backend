package com.larderhub.infrastructure.adapter.in.rest.auth;

import com.larderhub.domain.ports.in.auth.AuthUseCase;
import com.larderhub.infrastructure.adapter.in.rest.auth.dto.AuthResponse;
import com.larderhub.infrastructure.adapter.in.rest.auth.dto.LoginRequest;
import com.larderhub.infrastructure.adapter.in.rest.auth.dto.RegisterRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthUseCase authUseCase;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.ok(authUseCase.register(request));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authUseCase.login(request));
  }

  @GetMapping("/me")
  public ResponseEntity<Map<String, String>> me(@AuthenticationPrincipal UserDetails userDetails) {
    // sacamos el username del token, sin ir a la DB
    return ResponseEntity.ok(Map.of(
        "username", userDetails.getUsername(),
        "role", userDetails.getAuthorities().iterator().next().getAuthority()));
  }
}
