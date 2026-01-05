package ax.gritlab.buy_01.media.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the media service.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public final class SecurityConfig {

    /**
     * JWT authentication filter.
     */
    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Configures the security filter chain.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            final HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - anyone can VIEW
                        // images
                        .requestMatchers(HttpMethod.GET,
                                         "/media/images/**")
                        .permitAll()
                        .requestMatchers("/actuator/**")
                        .permitAll()

                        // Protected endpoints - authenticated
                        // users can upload/modify images
                        .requestMatchers(HttpMethod.POST,
                                         "/media/images/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.PUT,
                                         "/media/images/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.DELETE,
                                         "/media/images/**")
                        .authenticated()

                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
