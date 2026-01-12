package ax.gritlab.buy_01.user.dto;

import ax.gritlab.buy_01.user.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing authentication token and user details.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    /** JWT authentication token. */
    private String token;
    /** User's unique identifier. */
    private String id;
    /** User's email address. */
    private String email;
    /** User's display name. */
    private String name;
    /** User's role (CLIENT or SELLER). */
    private Role role;
    /** URL to user's avatar image. */
    private String avatarUrl;
}
