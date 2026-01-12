package ax.gritlab.buy_01.user.service;

import ax.gritlab.buy_01.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT service for token generation and validation.
 */
@Service
public final class JwtService {

    /** Milliseconds in one second. */
    private static final int MILLIS_PER_SECOND = 1000;
    /** Seconds in one minute. */
    private static final int SECONDS_PER_MINUTE = 60;
    /** Minutes in one hour. */
    private static final int MINUTES_PER_HOUR = 60;
    /** Hours for token validity. */
    private static final int TOKEN_VALIDITY_HOURS = 24;

    /** Secret key for JWT signing. */
    @Value("${jwt.secret.key}")
    private String secretKey;

    /**
     * Extracts username from JWT token.
     *
     * @param token JWT token
     * @return username from token
     */
    public String extractUsername(final String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts specific claim from JWT token.
     *
     * @param <T> type of claim
     * @param token JWT token
     * @param claimsResolver function to extract claim
     * @return extracted claim value
     */
    public <T> T extractClaim(final String token,
            final Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates JWT token for user.
     *
     * @param userDetails user details
     * @return generated JWT token
     */
    public String generateToken(final UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates JWT token with extra claims.
     *
     * @param extraClaims additional claims to include
     * @param userDetails user details
     * @return generated JWT token
     */
    public String generateToken(final Map<String, Object> extraClaims,
            final UserDetails userDetails) {
        // Add custom claims for user ID and authorities (roles)
        extraClaims.put("authorities", userDetails.getAuthorities());
        if (userDetails instanceof User) {
            extraClaims.put("userId", ((User) userDetails).getId());
        }

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()
                        + MILLIS_PER_SECOND * SECONDS_PER_MINUTE
                        * MINUTES_PER_HOUR * TOKEN_VALIDITY_HOURS))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates JWT token against user details.
     *
     * @param token JWT token
     * @param userDetails user details to validate against
     * @return true if token is valid
     */
    public boolean isTokenValid(final String token,
            final UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()))
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(final String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(final String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(final String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
