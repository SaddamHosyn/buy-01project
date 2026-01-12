package ax.gritlab.buy_01.product.config;

import ax.gritlab.buy_01.product.model.User;
import ax.gritlab.buy_01.product.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
 * JWT authentication filter for Product Service.
 */
@Component
@RequiredArgsConstructor
public final class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Length of Bearer prefix in Authorization header.
     */
    private static final int BEARER_PREFIX_LENGTH = 7;

    /**
     * JWT service for token operations.
     */
    /**
     * JWT service for token operations.
     */
    private final JwtService jwtService;

    /**
     * Determine if filter should not be applied.
     *
     * @param request the HTTP request
     * @return true if filter should be skipped
     * @throws ServletException if an error occurs
     */
    @Override
    protected boolean shouldNotFilter(
            final HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Skip JWT filter for inter-service calls and public endpoints
        return path.contains("/remove-media/")
                || path.contains("/cleanup-orphaned-media")
                || ("GET".equals(method) && path.startsWith("/products"));
    }

    /**
     * Perform JWT authentication filter logic.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(BEARER_PREFIX_LENGTH);
        if (SecurityContextHolder.getContext().getAuthentication() == null
                && jwtService.isTokenValid(jwt)) {
            String userEmail = jwtService.extractUsername(jwt);
            String userId = jwtService.extractUserId(jwt);
            List<GrantedAuthority> authorities =
                    jwtService.extractAuthorities(jwt);

            // Create UserDetails object on the fly from token claims
            User userDetails = new User();
            userDetails.setId(userId);
            userDetails.setEmail(userEmail);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities);
            authToken.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request));
            SecurityContextHolder.getContext()
                    .setAuthentication(authToken);
        }
        filterChain.doFilter(request, response);
    }
}
