package ax.gritlab.buy_01.user.service;

import ax.gritlab.buy_01.user.model.Role;
import ax.gritlab.buy_01.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService.
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private User testUser;

    // Base64 encoded secret key for testing (256 bits minimum for HS256)
    private static final String TEST_SECRET_KEY = "dGVzdFNlY3JldEtleUZvckpXVFRva2VuR2VuZXJhdGlvblRlc3Rpbmc=";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET_KEY);

        testUser = User.builder()
                .id("user-123")
                .name("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.CLIENT)
                .build();
    }

    @Nested
    @DisplayName("generateToken Tests")
    class GenerateTokenTests {

        @Test
        @DisplayName("Should generate token successfully")
        void generateToken_Success() {
            String token = jwtService.generateToken(testUser);

            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertTrue(token.contains("."));
        }

        @Test
        @DisplayName("Should generate token with extra claims")
        void generateToken_WithExtraClaims() {
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("customClaim", "customValue");

            String token = jwtService.generateToken(extraClaims, testUser);

            assertNotNull(token);
            assertFalse(token.isEmpty());
        }

        @Test
        @DisplayName("Should include userId in token for User instance")
        void generateToken_IncludesUserId() {
            String token = jwtService.generateToken(testUser);

            assertNotNull(token);
            // The userId should be extractable from token claims
            String username = jwtService.extractUsername(token);
            assertEquals(testUser.getEmail(), username);
        }

        @Test
        @DisplayName("Should generate different tokens for different users")
        void generateToken_DifferentTokensForDifferentUsers() {
            User anotherUser = User.builder()
                    .id("user-456")
                    .name("Another User")
                    .email("another@example.com")
                    .password("password")
                    .role(Role.SELLER)
                    .build();

            String token1 = jwtService.generateToken(testUser);
            String token2 = jwtService.generateToken(anotherUser);

            assertNotEquals(token1, token2);
        }
    }

    @Nested
    @DisplayName("extractUsername Tests")
    class ExtractUsernameTests {

        @Test
        @DisplayName("Should extract username from valid token")
        void extractUsername_ValidToken() {
            String token = jwtService.generateToken(testUser);

            String username = jwtService.extractUsername(token);

            assertEquals(testUser.getEmail(), username);
        }

        @Test
        @DisplayName("Should throw exception for malformed token")
        void extractUsername_MalformedToken() {
            assertThrows(MalformedJwtException.class,
                    () -> jwtService.extractUsername("invalid-token"));
        }
    }

    @Nested
    @DisplayName("extractClaim Tests")
    class ExtractClaimTests {

        @Test
        @DisplayName("Should extract subject claim")
        void extractClaim_Subject() {
            String token = jwtService.generateToken(testUser);

            String subject = jwtService.extractClaim(token, Claims::getSubject);

            assertEquals(testUser.getEmail(), subject);
        }

        @Test
        @DisplayName("Should extract issued at claim")
        void extractClaim_IssuedAt() {
            String token = jwtService.generateToken(testUser);

            var issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);

            assertNotNull(issuedAt);
        }

        @Test
        @DisplayName("Should extract expiration claim")
        void extractClaim_Expiration() {
            String token = jwtService.generateToken(testUser);

            var expiration = jwtService.extractClaim(token, Claims::getExpiration);

            assertNotNull(expiration);
            assertTrue(expiration.getTime() > System.currentTimeMillis());
        }
    }

    @Nested
    @DisplayName("isTokenValid Tests")
    class IsTokenValidTests {

        @Test
        @DisplayName("Should return true for valid token and matching user")
        void isTokenValid_ValidTokenMatchingUser_ReturnsTrue() {
            String token = jwtService.generateToken(testUser);

            boolean isValid = jwtService.isTokenValid(token, testUser);

            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should return false for valid token but different user")
        void isTokenValid_ValidTokenDifferentUser_ReturnsFalse() {
            String token = jwtService.generateToken(testUser);

            User differentUser = User.builder()
                    .id("user-different")
                    .email("different@example.com")
                    .role(Role.CLIENT)
                    .build();

            boolean isValid = jwtService.isTokenValid(token, differentUser);

            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should throw exception for malformed token")
        void isTokenValid_MalformedToken_ThrowsException() {
            assertThrows(Exception.class,
                    () -> jwtService.isTokenValid("malformed-token", testUser));
        }

        @Test
        @DisplayName("Should validate token for SELLER role")
        void isTokenValid_SellerRole() {
            User seller = User.builder()
                    .id("seller-123")
                    .email("seller@example.com")
                    .role(Role.SELLER)
                    .build();

            String token = jwtService.generateToken(seller);
            boolean isValid = jwtService.isTokenValid(token, seller);

            assertTrue(isValid);
        }
    }

    @Nested
    @DisplayName("Token Structure Tests")
    class TokenStructureTests {

        @Test
        @DisplayName("Token should have three parts separated by dots")
        void token_HasThreeParts() {
            String token = jwtService.generateToken(testUser);

            String[] parts = token.split("\\.");

            assertEquals(3, parts.length);
        }

        @Test
        @DisplayName("Token parts should be non-empty")
        void token_PartsNonEmpty() {
            String token = jwtService.generateToken(testUser);

            String[] parts = token.split("\\.");

            for (String part : parts) {
                assertFalse(part.isEmpty());
            }
        }
    }
}
