package ax.gritlab.buy_01.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user profile.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {
    /** User's new display name. */
    private String name;
    /** URL to user's new avatar image. */
    private String avatar;
    /** Current password for verification. */
    private String password;
    /** New password to set. */
    private String newPassword;
}

