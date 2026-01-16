package ax.gritlab.buy_01.user.config;

import ax.gritlab.buy_01.user.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter for validating tokens in requests.
 */
@Component
@RequiredArgsConstructor
public final class JwtAuthenticationFilter
                extends OncePerRequestFilter {

        /** Length of "Bearer " prefix in Authorization header. */
        private static final int BEARER_PREFIX_LENGTH = 7;

        /** JWT service for token operations. */
        private final JwtService jwtService;
        /** User details service for loading user data. */
        private final UserDetailsService userDetailsService;

        /**
         * Filters incoming requests to validate JWT tokens.
         *
         * @param request     HTTP request
         * @param response    HTTP response
         * @param filterChain filter chain
         * @throws ServletException if filter processing fails
         * @throws IOException      if I/O error occurs
         */
        @Override
        protected void doFilterInternal(
                        @NonNull final HttpServletRequest request,
                        @NonNull final HttpServletResponse response,
                        @NonNull final FilterChain filterChain)
                        throws ServletException, IOException {
                final String authHeader = request.getHeader("Authorization");
                final String jwt;
                final String userEmail;
                if (authHeader == null
                                || !authHeader.startsWith("Bearer ")) {
                        filterChain.doFilter(request, response);
                        return;
                }
                jwt = authHeader.substring(BEARER_PREFIX_LENGTH);
                userEmail = jwtService.extractUsername(jwt);
                if (userEmail != null
                                && SecurityContextHolder.getContext()
                                                .getAuthentication() == null) {
                        UserDetails userDetails = this.userDetailsService
                                        .loadUserByUsername(userEmail);
                        if (jwtService.isTokenValid(jwt, userDetails)) {
                                UsernamePasswordAuthenticationToken authToken;
                                authToken = new UsernamePasswordAuthenticationToken(
                                                userDetails,
                                                null,
                                                userDetails.getAuthorities());
                                authToken.setDetails(
                                                new WebAuthenticationDetailsSource()
                                                                .buildDetails(request));
                                SecurityContextHolder.getContext()
                                                .setAuthentication(authToken);
                        }
                }
                filterChain.doFilter(request, response);
        }
}
