package cc.kertaskerja.pengajuan_kta.security;

import cc.kertaskerja.pengajuan_kta.exception.BadRequestException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
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
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMillis = expirationMillis;
    }

    /**
     * Generate JWT token with username, msisdn, and role
     */
    public String generateToken(Long id, String username, String msisdn, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMillis);

        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", id);
        claims.put("sub", username);
        claims.put("msisdn", msisdn);
        claims.put("role", role);

        return Jwts.builder()
              .setClaims(claims)
              .setIssuedAt(Date.from(now))
              .setExpiration(Date.from(expiry))
              .signWith(secretKey, SignatureAlgorithm.HS256)
              .compact();
    }

    /**
     * Parse and validate JWT token, returning claims as a Map
     */
    public Map<String, Object> parseToken(String token) {
        try {
            var claims = Jwts.parserBuilder()
                  .setSigningKey(secretKey)
                  .build()
                  .parseClaimsJws(token)
                  .getBody();

            log.debug("🔍 Parsed token for subject: {}", claims.get("sub"));
            return claims;
        } catch (Exception e) {
            throw new BadRequestException("Invalid or expired token");
        }
    }
}
