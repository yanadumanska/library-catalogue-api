package com.library.catalogue.controller;

import com.library.catalogue.dto.*;
import com.library.catalogue.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private UUID userId;
    private UserDto userDto;
    private JwtResponse jwtResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userDto = UserDto.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .role("PATRON")
                .build();

        jwtResponse = JwtResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600)
                .user(userDto)
                .build();
    }

    @Test
    void register_ShouldReturnCreated() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .build();

        when(userService.register(any(RegisterRequest.class))).thenReturn(jwtResponse);

        ResponseEntity<JwtResponse> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("access-token", response.getBody().getAccessToken());
        assertEquals("testuser", response.getBody().getUser().getUsername());
    }

    @Test
    void login_ShouldReturnOk() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        when(userService.login(any(LoginRequest.class))).thenReturn(jwtResponse);

        ResponseEntity<JwtResponse> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Bearer", response.getBody().getTokenType());
    }

    @Test
    void getCurrentUser_ShouldReturnUser() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userId.toString());
        when(userService.getCurrentUser(userId)).thenReturn(userDto);

        ResponseEntity<UserDto> response = authController.getCurrentUser(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("testuser", response.getBody().getUsername());
    }

    @Test
    void getAllUsers_ShouldReturnList() {
        when(userService.getAllUsers()).thenReturn(List.of(userDto));

        ResponseEntity<List<UserDto>> response = authController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void updateUserStatus_ShouldReturnUpdatedUser() {
        Map<String, String> body = Map.of("status", "ACTIVE", "role", "LIBRARIAN");

        UserDto updated = UserDto.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .role("LIBRARIAN")
                .build();

        when(userService.updateUserStatus(eq(userId), eq("ACTIVE"), eq("LIBRARIAN")))
                .thenReturn(updated);

        ResponseEntity<UserDto> response = authController.updateUserStatus(userId, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("LIBRARIAN", response.getBody().getRole());
    }
}
