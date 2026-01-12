package ax.gritlab.buy_01.product.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * User model for Spring Security.
 * This is a local representation not persisted in this service's database.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class User implements UserDetails {

    /**
     * User ID.
     */
    private String id;

    /**
     * User name.
     */
    private String name;

    /**
     * User email.
     */
    private String email;

    /**
     * User password (will be null in this context).
     */
    private String password;

    /**
     * User role.
     */
    private Role role;

    /**
     * User avatar.
     */
    private String avatar;

    /**
     * Get authorities for this user.
     *
     * @return collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Get username.
     *
     * @return the email
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Check if account is non-expired.
     *
     * @return true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Check if account is non-locked.
     *
     * @return true
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Check if credentials are non-expired.
     *
     * @return true
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Check if user is enabled.
     *
     * @return true
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
