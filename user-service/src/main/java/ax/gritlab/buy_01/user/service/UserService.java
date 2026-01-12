package ax.gritlab.buy_01.user.service;

import ax.gritlab.buy_01.user.dto.UpdateProfileRequest;
import ax.gritlab.buy_01.user.dto.UserProfileResponse;
import ax.gritlab.buy_01.user.model.User;
import ax.gritlab.buy_01.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * User service for profile management and user operations.
 */
@Service
@RequiredArgsConstructor
public final class UserService {

    /** Kafka template for messaging. */
    private final KafkaTemplate<String, String> kafkaTemplate;
    /** User repository for database access. */
    private final UserRepository userRepository;
    /** Password encoder for hashing. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Builds user profile response from user entity.
     *
     * @param user user entity
     * @return user profile response
     */
    public UserProfileResponse getProfile(final User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .build();
    }

    /**
     * Gets user profile by ID.
     *
     * @param id user ID
     * @return user profile response
     */
    public UserProfileResponse getUserById(final String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "User not found"));
        return getProfile(user);
    }

    /**
     * Updates user profile with new information.
     *
     * @param user    user to update
     * @param request update request with new values
     * @return updated user profile response
     */
    public UserProfileResponse updateProfile(final User user,
            final UpdateProfileRequest request) {
        // 1. Handle Password Change Logic
        if (request.getNewPassword() != null
                && !request.getNewPassword().isEmpty()) {
            // Must provide current password to change it
            if (request.getPassword() == null
                    || request.getPassword().isEmpty()) {
                throw new IllegalArgumentException(
                        "Current password required to set new password");
            }

            // Verify current password matches DB
            if (!passwordEncoder.matches(request.getPassword(),
                    user.getPassword())) {
                throw new RuntimeException("Incorrect current password");
            }

            // Update to new password
            user.setPassword(passwordEncoder.encode(
                    request.getNewPassword()));
        }

        // 2. Handle Name Update
        if (request.getName() != null && !request.getName().isEmpty()) {
            user.setName(request.getName());
        }

        // 3. Handle Avatar Update
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        User updatedUser = userRepository.save(user);
        return getProfile(updatedUser);
    }

    /**
     * Deletes user and publishes deletion event.
     *
     * @param user user to delete
     */
    public void deleteUser(final User user) {
        userRepository.delete(user);
        // Publish Kafka event for user deletion
        kafkaTemplate.send("user.deleted", user.getId());
    }
}
