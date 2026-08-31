package com.contactmanagement.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {

    private static final String SECRET_BASE64 =
            Base64.getEncoder().encodeToString(
                    "test-secret-key-32-bytes-minimum!!".getBytes());
    private static final long EXPIRATION_MS = 3_600_000L;

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET_BASE64, EXPIRATION_MS);
    }

    private Authentication buildAuthentication(String username) {
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("returns a non-null token string")
        void generateToken_returnsNonNull() {
            Authentication auth = buildAuthentication("user@example.com");
            String token = jwtTokenProvider.generateToken(auth);
            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("token subject matches the authenticated username")
        void generateToken_subjectMatchesUsername() {
            Authentication auth = buildAuthentication("user@example.com");
            String token = jwtTokenProvider.generateToken(auth);
            assertThat(jwtTokenProvider.getUsernameFromToken(token))
                    .isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("token is valid immediately after generation")
        void generateToken_validImmediately() {
            Authentication auth = buildAuthentication("user@example.com");
            String token = jwtTokenProvider.generateToken(auth);
            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        }
    }

    @Nested
    @DisplayName("generateTokenFromUsername()")
    class GenerateTokenFromUsername {

        @Test
        @DisplayName("round-trips subject correctly")
        void generateTokenFromUsername_roundTripsSubject() {
            String token = jwtTokenProvider.generateTokenFromUsername("phone-user@test.com");
            assertThat(jwtTokenProvider.getUsernameFromToken(token))
                    .isEqualTo("phone-user@test.com");
        }

        @Test
        @DisplayName("token is valid immediately after creation")
        void generateTokenFromUsername_isValid() {
            String token = jwtTokenProvider.generateTokenFromUsername("+923001234567");
            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("two tokens for the same username are not identical (issued-at differs)")
        void generateTokenFromUsername_uniqueTokensPerCall() throws InterruptedException {
            String t1 = jwtTokenProvider.generateTokenFromUsername("user@example.com");
            Thread.sleep(1001);
            String t2 = jwtTokenProvider.generateTokenFromUsername("user@example.com");
            assertThat(t1).isNotEqualTo(t2);
        }
    }

    @Nested
    @DisplayName("getUsernameFromToken()")
    class GetUsernameFromToken {

        @Test
        @DisplayName("extracts email subject")
        void getUsernameFromToken_emailSubject() {
            String token = jwtTokenProvider.generateTokenFromUsername("admin@example.com");
            assertThat(jwtTokenProvider.getUsernameFromToken(token))
                    .isEqualTo("admin@example.com");
        }

        @Test
        @DisplayName("extracts phone subject")
        void getUsernameFromToken_phoneSubject() {
            String token = jwtTokenProvider.generateTokenFromUsername("+15551234567");
            assertThat(jwtTokenProvider.getUsernameFromToken(token))
                    .isEqualTo("+15551234567");
        }
    }

    @Nested
    @DisplayName("validateToken()")
    class ValidateToken {

        @Test
        @DisplayName("returns true for a freshly generated valid token")
        void validateToken_validToken_returnsTrue() {
            String token = jwtTokenProvider.generateTokenFromUsername("user@example.com");
            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("returns false for an expired token")
        void validateToken_expiredToken_returnsFalse() {
            JwtTokenProvider shortLived = new JwtTokenProvider(SECRET_BASE64, 1L);
            String token = shortLived.generateTokenFromUsername("user@example.com");
            assertThat(shortLived.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("returns false for a completely malformed string")
        void validateToken_malformedToken_returnsFalse() {
            assertThat(jwtTokenProvider.validateToken("not.a.jwt")).isFalse();
        }

        @Test
        @DisplayName("returns false for an empty string")
        void validateToken_emptyString_returnsFalse() {
            assertThat(jwtTokenProvider.validateToken("")).isFalse();
        }

        @Test
        @DisplayName("returns false for a token signed with a different key")
        void validateToken_wrongKey_returnsFalse() {
            String differentSecret = Base64.getEncoder().encodeToString(
                    "completely-different-secret-key!!".getBytes());
            byte[] keyBytes = Decoders.BASE64.decode(differentSecret);
            String tamperedToken = Jwts.builder()
                    .setSubject("user@example.com")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                    .signWith(Keys.hmacShaKeyFor(keyBytes), SignatureAlgorithm.HS256)
                    .compact();
            assertThat(jwtTokenProvider.validateToken(tamperedToken)).isFalse();
        }

        @Test
        @DisplayName("returns false for a token with a truncated signature")
        void validateToken_truncatedSignature_returnsFalse() {
            String token = jwtTokenProvider.generateTokenFromUsername("user@example.com");
            String truncated = token.substring(0, token.lastIndexOf('.') + 1);
            assertThat(jwtTokenProvider.validateToken(truncated)).isFalse();
        }
    }
}
