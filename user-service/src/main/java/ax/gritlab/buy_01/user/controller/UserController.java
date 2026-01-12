package ax.gritlab.buy_01.user.controller;

import ax.gritlab.buy_01.user.dto.UpdateProfileRequest;
import ax.gritlab.buy_01.user.dto.UserProfileResponse;
import ax.gritlab.buy_01.user.model.User;
import ax.gritlab.buy_01.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user operations.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public final class UserController {

    /** User service. */
    private final UserService userService;

    /**
     * Gets current user's profile.
     *
     * @param authentication authentication context
     * @return user profile response
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            final Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getProfile(user));
    }

    /**
     * Gets user profile by ID.
     *
     * @param id user ID
     * @return user profile response
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(
            @PathVariable final String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Updates current user's profile.
     *
     * @param request        update request
     * @param authentication authentication context
     * @return updated user profile response
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @RequestBody final UpdateProfileRequest request,
            final Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateProfile(user, request));
    }

    /**
     * Deletes current user's account.
     *
     * @param authentication authentication context
     * @return empty response
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(
            final Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        userService.deleteUser(user);
        return ResponseEntity.noContent().build();
    }
}
