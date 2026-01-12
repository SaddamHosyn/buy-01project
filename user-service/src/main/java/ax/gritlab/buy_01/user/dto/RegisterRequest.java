package ax.gritlab.buy_01.user.dto;

import ax.gritlab.buy_01.user.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user registration.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    /** Minimum password length. */
    private static final int PASSWORD_MIN_LENGTH = 8;

    /** Maximum password length. */
    private static final int PASSWORD_MAX_LENGTH = 100;

    /** Maximum length for user name. */
    private static final int NAME_MAX_LENGTH = 50;

    /**
     * User's display name.
     */
    @NotNull(message = "Name is required")
    @Size(min = 2, max = NAME_MAX_LENGTH, message = "Name must be between 2 "
            + "and 50 characters")
    private String name;

    /**
     * User's email address.
     */
    @NotNull(message = "Email is required")
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+"
            + "\\.[a-zA-Z]{2,}$",
            message = "Invalid email format")
    private String email;

    /**
     * User's password.
     */
    @NotNull(message = "Password is required")
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH,
            message = "Password must be at least 8 characters")
    private String password;

    /**
     * User's role in the system.
     */
    @NotNull(message = "Role is required")
    private Role role;
}

