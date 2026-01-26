package ax.gritlab.buy_01.user.service;

import ax.gritlab.buy_01.user.dto.AuthenticationRequest;
import ax.gritlab.buy_01.user.dto.AuthenticationResponse;
import ax.gritlab.buy_01.user.dto.RegisterRequest;
import ax.gritlab.buy_01.user.dto.UserProfileResponse;
import ax.gritlab.buy_01.user.model.Role;
import ax.gritlab.buy_01.user.model.User;
import ax.gritlab.buy_01.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private RegisterRequest registerRequest;
    private AuthenticationRequest authRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user-123")
                .name("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.CLIENT)
                .avatar("https://example.com/avatar.jpg")
                .build();

        registerRequest = RegisterRequest.builder()
                .name("New User")
                .email("newuser@example.com")
                .password("password123")
                .role(Role.CLIENT)
                .build();

        authRequest = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Nested
    @DisplayName("register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void register_Success() {
            User savedUser = User.builder()
                    .id("new-user-id")
                    .name("New User")
                    .email("newuser@example.com")
                    .password("encodedPassword")
                    .role(Role.CLIENT)
                    .build();

            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            UserProfileResponse response = authenticationService.register(registerRequest);

            assertNotNull(response);
            assertEquals("new-user-id", response.getId());
            assertEquals("New User", response.getName());
            assertEquals("newuser@example.com", response.getEmail());
            assertEquals(Role.CLIENT, response.getRole());
        }

        @Test
        @DisplayName("Should encode password during registration")
        void register_EncodesPassword() {
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            authenticationService.register(registerRequest);

            verify(passwordEncoder, times(1)).encode("password123");
        }

        @Test
        @DisplayName("Should save user with correct details")
        void register_SavesUserWithCorrectDetails() {
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(userCaptor.capture())).thenAnswer(i -> {
                User u = i.getArgument(0);
                u.setId("generated-id");
                return u;
            });

            authenticationService.register(registerRequest);

            User capturedUser = userCaptor.getValue();
            assertEquals("New User", capturedUser.getName());
            assertEquals("newuser@example.com", capturedUser.getEmail());
            assertEquals("encodedPassword", capturedUser.getPassword());
            assertEquals(Role.CLIENT, capturedUser.getRole());
        }

        @Test
        @DisplayName("Should register SELLER role correctly")
        void register_SellerRole() {
            registerRequest.setRole(Role.SELLER);
            User savedUser = User.builder()
                    .id("seller-id")
                    .name("New User")
                    .email("newuser@example.com")
                    .password("encodedPassword")
                    .role(Role.SELLER)
                    .build();

            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            UserProfileResponse response = authenticationService.register(registerRequest);

            assertEquals(Role.SELLER, response.getRole());
        }
    }

    @Nested
    @DisplayName("authenticate Tests")
    class AuthenticateTests {

        @Test
        @DisplayName("Should authenticate user successfully")
        void authenticate_Success() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(testUser)).thenReturn("jwt-token");

            AuthenticationResponse response = authenticationService.authenticate(authRequest);

            assertNotNull(response);
            assertEquals("jwt-token", response.getToken());
            assertEquals("user-123", response.getId());
            assertEquals("test@example.com", response.getEmail());
            assertEquals("Test User", response.getName());
            assertEquals(Role.CLIENT, response.getRole());
        }

        @Test
        @DisplayName("Should call authentication manager with correct credentials")
        void authenticate_CallsAuthManager() {
            ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor = ArgumentCaptor
                    .forClass(UsernamePasswordAuthenticationToken.class);

            when(authenticationManager.authenticate(tokenCaptor.capture())).thenReturn(null);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(any())).thenReturn("token");

            authenticationService.authenticate(authRequest);

            UsernamePasswordAuthenticationToken capturedToken = tokenCaptor.getValue();
            assertEquals("test@example.com", capturedToken.getPrincipal());
            assertEquals("password123", capturedToken.getCredentials());
        }

        @Test
        @DisplayName("Should generate JWT token for authenticated user")
        void authenticate_GeneratesToken() {
            when(authenticationManager.authenticate(any())).thenReturn(null);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(testUser)).thenReturn("generated-jwt-token");

            AuthenticationResponse response = authenticationService.authenticate(authRequest);

            assertEquals("generated-jwt-token", response.getToken());
            verify(jwtService, times(1)).generateToken(testUser);
        }

        @Test
        @DisplayName("Should return user avatar URL in response")
        void authenticate_ReturnsAvatarUrl() {
            when(authenticationManager.authenticate(any())).thenReturn(null);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(testUser)).thenReturn("token");

            AuthenticationResponse response = authenticationService.authenticate(authRequest);

            assertEquals("https://example.com/avatar.jpg", response.getAvatarUrl());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void authenticate_UserNotFound_ThrowsException() {
            when(authenticationManager.authenticate(any())).thenReturn(null);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

            assertThrows(Exception.class, () -> authenticationService.authenticate(authRequest));
        }
    }
}
