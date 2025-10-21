
package cc.kertaskerja.pengajuan_kta.security;

import cc.kertaskerja.pengajuan_kta.exception.UnauthenticationException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMillis;

    public JwtTokenProvider(
          @Value("${jwt.secret:change-this-secret-key}") String secret,
          @Value("${jwt.expirationMillis:3600000}") long expirationMillis
    ) {
        // Ensure secret is properly encoded and has sufficient length
        this.secretKey = generateSecretKey(secret);
        this.expirationMillis = expirationMillis;
        log.info("🔑 JWT Provider initialized with secret length: {} characters", secret.length());
    }

    /**
     * Generate a proper secret key for HMAC-SHA256
     */
    private SecretKey generateSecretKey(String secret) {
        try {
            // Ensure minimum key length for HS256 (32 bytes/256 bits)
            if (secret.length() < 32) {
                log.warn("⚠️ JWT secret is shorter than recommended 32 characters. Padding...");
                // Pad the secret to meet minimum requirements
                secret = secret + "0".repeat(32 - secret.length());
            }

            // Convert to bytes using UTF-8 encoding
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

            // Create HMAC key
            return Keys.hmacShaKeyFor(keyBytes);

        } catch (Exception e) {
            log.error("🚨 Failed to generate secret key", e);
            throw new IllegalArgumentException("Invalid JWT secret configuration", e);
        }
    }

    /**
     * Generate JWT token with username, msisdn, and role
     */
    public String generateToken(Long id, String nama, String email, String username, String msisdn, String role) {
        try {
            Instant now = Instant.now();
            Instant expiry = now.plusMillis(expirationMillis);

            Map<String, Object> claims = new HashMap<>();
            claims.put("uid", id);
            claims.put("nama", nama);
            claims.put("email", email);
            claims.put("sub", username);
            claims.put("msisdn", msisdn);
            claims.put("role", role);

            String token = Jwts.builder()
                  .setClaims(claims)
                  .setIssuedAt(Date.from(now))
                  .setExpiration(Date.from(expiry))
                  .signWith(secretKey, SignatureAlgorithm.HS256)
                  .compact();

            log.debug("🎟️ Generated token for user: {} (expires: {})", username, expiry);
            return token;

        } catch (Exception e) {
            log.error("🚨 Failed to generate token for user: {}", username, e);
            throw new UnauthenticationException("Failed to generate authentication token");
        }
    }

    /**
     * Parse and validate JWT token, returning claims as a Map
     */
    public Map<String, Object> parseToken(String token) {
        // Add null/empty token validation
        if (token == null || token.trim().isEmpty()) {
            log.warn("🔒 Received null or empty token");
            throw new UnauthenticationException("Token is required");
        }

        // Remove Bearer prefix if present (defensive programming)
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Additional validation for token format
        if (token.trim().isEmpty()) {
            log.warn("🔒 Token is empty after Bearer prefix removal");
            throw new UnauthenticationException("Invalid token format");
        }

        try {
            var claims = Jwts.parserBuilder()
                  .setSigningKey(secretKey)
                  .build()
                  .parseClaimsJws(token)
                  .getBody();

            log.debug("🔍 Successfully parsed token for subject: {}", claims.get("sub"));
            return claims;

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("🔒 Token has expired: {}", e.getMessage());
            throw new UnauthenticationException("Token has expired");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("🔒 Malformed JWT token: {}", e.getMessage());
            throw new UnauthenticationException("Malformed token");
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.warn("🔒 Unsupported JWT token: {}", e.getMessage());
            throw new UnauthenticationException("Unsupported token format");
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("🔒 JWT signature validation failed. This usually means:");
            log.error("   - Token was signed with a different secret key");
            log.error("   - Secret key configuration changed");
            log.error("   - Token was tampered with");
            log.error("   Original error: {}", e.getMessage());
            throw new UnauthenticationException("Invalid token signature");
        } catch (IllegalArgumentException e) {
            log.warn("🔒 JWT claims string is empty: {}", e.getMessage());
            throw new UnauthenticationException("Empty token claims");
        } catch (Exception e) {
            log.error("🔒 Unexpected error while parsing token: {}", e.getMessage(), e);
            throw new UnauthenticationException("Invalid or expired token");
        }
    }
}