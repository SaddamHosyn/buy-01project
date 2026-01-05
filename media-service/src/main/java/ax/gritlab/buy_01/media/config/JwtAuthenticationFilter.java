package ax.gritlab.buy_01.media.config;

import ax.gritlab.buy_01.media.model.User;
import ax.gritlab.buy_01.media.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT authentication filter for processing JWT tokens.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    /**
     * JWT service for token operations.
     */
    private final JwtService jwtService;

    /**
     * Determines if the filter should not be applied.
     *
     * @param request the HTTP request
     * @return true if filter should be skipped
     * @throws ServletException if an error occurs
     */
    @Override
    protected boolean shouldNotFilter(
            final HttpServletRequest request)
            throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Skip JWT filter for GET requests to images
        return "GET".equals(method)
                && path.contains("/media/images/");
    }

    /**
     * Performs the JWT authentication.
     *
     * @param request     the HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if an error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);

            if (SecurityContextHolder.getContext()
                    .getAuthentication() == null
                    && jwtService.isTokenValid(jwt)) {
                String userEmail = jwtService.extractUsername(jwt);
                String userId = jwtService.extractUserId(jwt);
                List<GrantedAuthority> authorities = jwtService.extractAuthorities(jwt);

                // Create UserDetails object on the fly from
                // token claims
                User userDetails = new User();
                userDetails.setId(userId);
                userDetails.setEmail(userEmail);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        authorities);
                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);

                log.debug(
                        "JWT authentication successful for "
                                + "user: {}",
                        userEmail);
            }
        } catch (Exception e) {
            // Log the error but continue - let Spring
            // Security handle the unauthenticated request
            log.error("JWT validation failed: {}",
                    e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}
