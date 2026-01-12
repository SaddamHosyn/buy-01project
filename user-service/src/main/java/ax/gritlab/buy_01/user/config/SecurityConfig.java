package ax.gritlab.buy_01.user.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for JWT authentication and authorization.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public final class SecurityConfig {

    /** JWT authentication filter. */
    private final JwtAuthenticationFilter jwtAuthFilter;
    /** Authentication provider. */
    private final AuthenticationProvider authenticationProvider;

    /**
     * Configures HTTP security filter chain.
     *
     * @param http HTTP security configuration
     * @return SecurityFilterChain configured with JWT authentication
     * @throws Exception if security configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (Login, Register, Health)
                        .requestMatchers("/auth/**",
                                "/actuator/**")
                        .permitAll()

                        // Allow anyone to VIEW user profiles
                        .requestMatchers(HttpMethod.GET,
                                "/users/**")
                        .permitAll()

                        // Protected - require authentication
                        .requestMatchers(HttpMethod.PUT,
                                "/users/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.DELETE,
                                "/users/**")
                        .authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
