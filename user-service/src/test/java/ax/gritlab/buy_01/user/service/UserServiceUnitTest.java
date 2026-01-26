package ax.gritlab.buy_01.user.service;

import ax.gritlab.buy_01.user.dto.UpdateProfileRequest;
import ax.gritlab.buy_01.user.dto.UserProfileResponse;
import ax.gritlab.buy_01.user.model.Role;
import ax.gritlab.buy_01.user.model.User;
import ax.gritlab.buy_01.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user-123")
                .name("Test User")
                .email("test@example.com")
                .password("encodedPassword123")
                .role(Role.CLIENT)
                .avatar("https://example.com/avatar.jpg")
                .build();
    }

    @Nested
    @DisplayName("getProfile Tests")
    class GetProfileTests {

        @Test
        @DisplayName("Should return user profile response from user entity")
        void getProfile_ReturnsCorrectResponse() {
            UserProfileResponse response = userService.getProfile(testUser);

            assertNotNull(response);
            assertEquals("user-123", response.getId());
            assertEquals("Test User", response.getName());
            assertEquals("test@example.com", response.getEmail());
            assertEquals(Role.CLIENT, response.getRole());
            assertEquals("https://example.com/avatar.jpg", response.getAvatar());
        }

        @Test
        @DisplayName("Should handle user with null avatar")
        void getProfile_HandlesNullAvatar() {
            testUser.setAvatar(null);

            UserProfileResponse response = userService.getProfile(testUser);

            assertNotNull(response);
            assertNull(response.getAvatar());
        }

        @Test
        @DisplayName("Should handle SELLER role correctly")
        void getProfile_HandlesSeller() {
            testUser.setRole(Role.SELLER);

            UserProfileResponse response = userService.getProfile(testUser);

            assertEquals(Role.SELLER, response.getRole());
        }
    }

    @Nested
    @DisplayName("getUserById Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user profile when user exists")
        void getUserById_WhenUserExists_ReturnsProfile() {
            when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

            UserProfileResponse response = userService.getUserById("user-123");

            assertNotNull(response);
            assertEquals("user-123", response.getId());
            assertEquals("Test User", response.getName());
            verify(userRepository, times(1)).findById("user-123");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void getUserById_WhenUserNotFound_ThrowsException() {
            when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> userService.getUserById("nonexistent"));
            verify(userRepository, times(1)).findById("nonexistent");
        }
    }

    @Nested
    @DisplayName("updateProfile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update name successfully")
        void updateProfile_UpdatesName() {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .name("New Name")
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(testUser);

            UserProfileResponse response = userService.updateProfile(testUser, request);

            assertNotNull(response);
            verify(userRepository, times(1)).save(testUser);
        }

        @Test
        @DisplayName("Should update avatar successfully")
        void updateProfile_UpdatesAvatar() {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .avatar("https://newavatar.com/pic.jpg")
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.updateProfile(testUser, request);

            verify(userRepository, times(1)).save(testUser);
        }

        @Test
        @DisplayName("Should update password when current password matches")
        void updateProfile_UpdatesPassword_WhenCurrentPasswordMatches() {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .password("currentPassword")
                    .newPassword("newPassword123")
                    .build();

            when(passwordEncoder.matches("currentPassword", "encodedPassword123")).thenReturn(true);
            when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.updateProfile(testUser, request);

            verify(passwordEncoder, times(1)).matches("currentPassword", "encodedPassword123");
            verify(passwordEncoder, times(1)).encode("newPassword123");
            verify(userRepository, times(1)).save(testUser);
        }

        @Test
        @DisplayName("Should throw exception when new password provided without current password")
        void updateProfile_ThrowsException_WhenNoCurrentPassword() {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .newPassword("newPassword123")
                    .build();

            assertThrows(IllegalArgumentException.class,
                    () -> userService.updateProfile(testUser, request));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when current password is incorrect")
        void updateProfile_ThrowsException_WhenPasswordIncorrect() {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .password("wrongPassword")
                    .newPassword("newPassword123")
                    .build();

            when(passwordEncoder.matches("wrongPassword", "encodedPassword123")).thenReturn(false);

            assertThrows(RuntimeException.class,
                    () -> userService.updateProfile(testUser, request));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle empty name in request")
        void updateProfile_IgnoresEmptyName() {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .name("")
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.updateProfile(testUser, request);

            assertEquals("Test User", testUser.getName());
        }
    }

    @Nested
    @DisplayName("deleteUser Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user and publish Kafka event")
        void deleteUser_DeletesAndPublishesEvent() {
            doNothing().when(userRepository).delete(testUser);
            when(kafkaTemplate.send(anyString(), anyString())).thenReturn(null);

            userService.deleteUser(testUser);

            verify(userRepository, times(1)).delete(testUser);
            verify(kafkaTemplate, times(1)).send("user.deleted", "user-123");
        }

        @Test
        @DisplayName("Should call repository delete")
        void deleteUser_CallsRepositoryDelete() {
            doNothing().when(userRepository).delete(testUser);

            userService.deleteUser(testUser);

            verify(userRepository).delete(testUser);
        }
    }
}
