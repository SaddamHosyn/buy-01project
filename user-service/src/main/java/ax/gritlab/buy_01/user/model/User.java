package ax.gritlab.buy_01.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * User entity representing a user in the system.
 * Implements UserDetails for Spring Security integration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public final class User implements UserDetails {

    /** Maximum length for user name. */
    private static final int NAME_MAX_LENGTH = 50;

    /** Minimum length for password. */
    private static final int PASSWORD_MIN_LENGTH = 8;

    /** Maximum length for password. */
    private static final int PASSWORD_MAX_LENGTH = 100;

    /**
     * Unique identifier for the user.
     */
    @Id
    private String id;

    /**
     * User's display name.
     */
    @NotNull
    @Size(min = 2, max = NAME_MAX_LENGTH)
    private String name;

    /**
     * User's email address (unique).
     */
    @NotNull
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+"
            + "\\.[a-zA-Z]{2,}$", message = "Invalid email format")
    @Indexed(unique = true)
    private String email;

    /**
     * User's hashed password.
     */
    @NotNull
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String password;

    /**
     * User's role in the system.
     */
    @NotNull
    private Role role;

    /**
     * URL to user's avatar image.
     */
    private String avatar;

    /**
     * Returns the authorities granted to the user.
     *
     * @return Collection of granted authorities based on user role
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Returns the username used to authenticate the user.
     *
     * @return User's email address
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indicates whether the user's account has expired.
     *
     * @return true (accounts never expire in this implementation)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     *
     * @return true (accounts are never locked in this implementation)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials have expired.
     *
     * @return true (credentials never expire in this implementation)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     *
     * @return true (all users are enabled in this implementation)
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
