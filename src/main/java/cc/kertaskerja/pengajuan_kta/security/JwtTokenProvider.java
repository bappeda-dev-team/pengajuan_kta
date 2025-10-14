package cc.kertaskerja.pengajuan_kta.security;

import cc.kertaskerja.pengajuan_kta.exception.BadRequestException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMillis;

    public JwtTokenProvider(
            @Value("${jwt.secret:change-this-secret-key}") String secret,
            @Value("${jwt.expirationMillis:3600000}") long expirationMillis
    ) {
        // Build a key from the provided secret
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(Long id, String username, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMillis);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("uid", id);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Map<String, Object> parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                  .setSigningKey(secretKey)
                  .build()
                  .parseClaimsJws(token)
                  .getBody();
        } catch (Exception e) {
            throw new BadRequestException("Invalid or expired token");
        }
    }
}
