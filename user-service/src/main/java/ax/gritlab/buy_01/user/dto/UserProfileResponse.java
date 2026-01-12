package ax.gritlab.buy_01.user.dto;

import ax.gritlab.buy_01.user.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing user profile information.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    /** User's unique identifier. */
    private Long id;
    /** User's display name. */
    private String name;
    /** User's email address. */
    private String email;
    /** User's role (CLIENT or SELLER). */
    private Role role;
    /** URL to user's avatar image. */
    private String avatar;
}
