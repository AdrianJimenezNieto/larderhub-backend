package com.larderhub.application;

import com.larderhub.domain.model.User;
import com.larderhub.domain.ports.out.user.UserPersistencePort;
import com.larderhub.infrastructure.adapter.in.rest.auth.dto.AuthResponse;
import com.larderhub.infrastructure.adapter.in.rest.auth.dto.LoginRequest;
import com.larderhub.infrastructure.adapter.in.rest.auth.dto.RegisterRequest;
import com.larderhub.infrastructure.adapter.in.rest.config.JwtService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserPersistencePort userPersistencePort;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService authService;

    // --- register ---

    @Test
    void register_whenEmailAlreadyExists_throwsIllegalArgumentException() {
        when(userPersistencePort.existsByEmail("taken@mail.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("juan");
        request.setEmail("taken@mail.com");
        request.setPassword("secret123");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");

        verify(userPersistencePort, never()).save(any());
    }

    @Test
    void register_whenUsernameAlreadyTaken_throwsIllegalArgumentException() {
        when(userPersistencePort.existsByEmail(anyString())).thenReturn(false);
        when(userPersistencePort.existsByUsername("juan")).thenReturn(true);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("juan");
        request.setEmail("new@mail.com");
        request.setPassword("secret123");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already taken");

        verify(userPersistencePort, never()).save(any());
    }

    @Test
    void register_happyPath_savesUserAndReturnsToken() {
        when(userPersistencePort.existsByEmail(anyString())).thenReturn(false);
        when(userPersistencePort.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");

        User savedUser = User.builder()
                .id(1L)
                .username("juan")
                .email("juan@test.com")
                .password("hashed")
                .role("USER")
                .build();
        when(userPersistencePort.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken("juan")).thenReturn("jwt.token.value");

        RegisterRequest request = new RegisterRequest();
        request.setUsername("juan");
        request.setEmail("juan@test.com");
        request.setPassword("secret123");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt.token.value");
        assertThat(response.getUsername()).isEqualTo("juan");
        assertThat(response.getEmail()).isEqualTo("juan@test.com");
        verify(userPersistencePort).save(any(User.class));
    }

    // --- login ---

    @Test
    void login_whenUserNotFound_throwsBadCredentialsException() {
        when(userPersistencePort.findByEmail("nobody@x.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setEmail("nobody@x.com");
        request.setPassword("any");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_whenPasswordWrong_throwsBadCredentialsException() {
        User user = User.builder()
                .id(2L)
                .username("ana")
                .email("ana@test.com")
                .password("hashed")
                .build();
        when(userPersistencePort.findByEmail("ana@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setEmail("ana@test.com");
        request.setPassword("wrong");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_happyPath_returnsToken() {
        User user = User.builder()
                .id(2L)
                .username("ana")
                .email("ana@test.com")
                .password("hashed")
                .build();
        when(userPersistencePort.findByEmail("ana@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct", "hashed")).thenReturn(true);
        when(jwtService.generateToken("ana")).thenReturn("ana.jwt.token");

        LoginRequest request = new LoginRequest();
        request.setEmail("ana@test.com");
        request.setPassword("correct");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("ana.jwt.token");
        assertThat(response.getUsername()).isEqualTo("ana");
    }
}
