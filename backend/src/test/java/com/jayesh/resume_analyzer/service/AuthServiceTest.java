package com.jayesh.resume_analyzer.service;

import com.jayesh.resume_analyzer.dto.AuthResponse;
import com.jayesh.resume_analyzer.dto.LoginRequest;
import com.jayesh.resume_analyzer.dto.RegisterRequest;
import com.jayesh.resume_analyzer.dto.UserResponse;
import com.jayesh.resume_analyzer.entity.Role;
import com.jayesh.resume_analyzer.entity.User;
import com.jayesh.resume_analyzer.repository.UserRepository;
import com.jayesh.resume_analyzer.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerRejectsTheAdminRole() {
        RegisterRequest request = new RegisterRequest("Eve", "eve@example.com", "secret123", Role.ADMIN);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register(request));

        assertEquals(403, ex.getStatusCode().value());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsADuplicateEmail() {
        when(userRepository.findByEmail("a@example.com")).thenReturn(Optional.of(new User()));
        RegisterRequest request = new RegisterRequest("Ann", "a@example.com", "secret123", Role.CANDIDATE);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.register(request));

        assertEquals(409, ex.getStatusCode().value());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerStoresAHashedPasswordNeverThePlainOne() {
        when(userRepository.findByEmail("a@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        UserResponse response = authService.register(
                new RegisterRequest("Ann", "a@example.com", "secret123", Role.CANDIDATE));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("HASHED", captor.getValue().getPasswordHash());
        assertNotEquals("secret123", captor.getValue().getPasswordHash());
        assertEquals(Role.CANDIDATE, response.role());
        assertEquals("a@example.com", response.email());
    }

    @Test
    void loginWithAnUnknownEmailIsUnauthorized() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest("nobody@example.com", "secret123")));

        assertEquals(401, ex.getStatusCode().value());
        verifyNoInteractions(passwordEncoder, jwtUtil);
    }

    @Test
    void loginWithTheWrongPasswordIsUnauthorized() {
        User user = new User();
        user.setEmail("a@example.com");
        user.setPasswordHash("HASH");
        user.setRole(Role.CANDIDATE);
        when(userRepository.findByEmail("a@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "HASH")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest("a@example.com", "wrong")));

        assertEquals(401, ex.getStatusCode().value());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void loginWithTheRightPasswordReturnsAToken() {
        User user = new User();
        user.setId(5L);
        user.setName("Ann");
        user.setEmail("a@example.com");
        user.setPasswordHash("HASH");
        user.setRole(Role.CANDIDATE);
        when(userRepository.findByEmail("a@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "HASH")).thenReturn(true);
        when(jwtUtil.generateToken("a@example.com", "CANDIDATE")).thenReturn("TOKEN");

        AuthResponse response = authService.login(new LoginRequest("a@example.com", "secret123"));

        assertEquals("TOKEN", response.token());
        assertEquals("a@example.com", response.user().email());
        assertEquals(Role.CANDIDATE, response.user().role());
    }
}
