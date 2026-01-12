package ax.gritlab.buy_01.product.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for JWT token operations.
 */
@Service
public final class JwtService {

    /**
     * Secret key for JWT signing.
     */
    @Value("${jwt.secret.key}")
    private String secretKey;

    /**
     * Extract username from JWT token.
     *
     * @param token the JWT token
     * @return the username
     */
    public String extractUsername(final String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from JWT token.
     *
     * @param token the JWT token
     * @return the user ID
     */
    public String extractUserId(final String token) {
        return extractClaim(token,
                claims -> claims.get("userId", String.class));
    }

    /**
     * Extract authorities from JWT token.
     *
     * @param token the JWT token
     * @return list of granted authorities
     */
    public List<GrantedAuthority> extractAuthorities(final String token) {
        List<Map<String, String>> authoritiesMaps = extractClaim(
                token, claims -> claims.get("authorities", List.class));
        return authoritiesMaps.stream()
                .map(authorityMap -> new SimpleGrantedAuthority(
                        authorityMap.get("authority")))
                .collect(Collectors.toList());
    }

    /**
     * Extract a specific claim from JWT token.
     *
     * @param <T> the type of the claim
     * @param token the JWT token
     * @param claimsResolver function to extract claim
     * @return the extracted claim
     */
    public <T> T extractClaim(
            final String token,
            final Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Validate if JWT token is valid.
     *
     * @param token the JWT token
     * @return true if token is valid
     */
    public boolean isTokenValid(final String token) {
        return !isTokenExpired(token);
    }

    /**
     * Check if JWT token is expired.
     *
     * @param token the JWT token
     * @return true if token is expired
     */
    private boolean isTokenExpired(final String token) {
        return extractExpiration(token).before(new java.util.Date());
    }

    /**
     * Extract expiration date from JWT token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    private java.util.Date extractExpiration(final String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract all claims from JWT token.
     *
     * @param token the JWT token
     * @return all claims
     */
    private Claims extractAllClaims(final String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Get signing key for JWT operations.
     *
     * @return the signing key
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

