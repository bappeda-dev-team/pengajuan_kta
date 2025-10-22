package cc.kertaskerja.pengajuan_kta.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();

        // Skip JWT authentication ONLY for auth endpoints
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // For Swagger endpoints, check for Basic Auth first, then skip JWT
        if (isSwaggerEndpoint(requestPath)) {
            String authHeader = request.getHeader("Authorization");

            // If it's Basic Auth, let it through (don't process as JWT)
            if (authHeader != null && authHeader.startsWith("Basic ")) {
                filterChain.doFilter(request, response);
                return;
            }

            // If no auth header for Swagger, let Spring Security handle Basic Auth challenge
            if (authHeader == null) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("No valid Authorization header found for path: {}", requestPath);
                unauthorizedResponse(response, "UNAUTHORIZED. You must input token in Authorization header");
                return;
            }

            // Rest of your JWT processing logic...
            String token = authHeader.substring(7);
            Map<String, Object> claims = jwtTokenProvider.parseToken(token);

            String username = (String) claims.get("sub");
            String role = (String) claims.get("role");
            Long userId = ((Number) claims.get("uid")).longValue();
            String nama = (String) claims.get("nama");
            String email = (String) claims.get("email");

            UsernamePasswordAuthenticationToken authentication =
                  new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                  );

            authentication.setDetails(Map.of(
                  "userId", userId,
                  "nama", nama,
                  "email", email,
                  "role", role
            ));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("✅ JWT Authentication successful for user: {} with role: {}", username, role);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("❌ JWT Authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            unauthorizedResponse(response, e.getMessage());
        }
    }

    private boolean isPublicEndpoint(String path) {
        // Only auth endpoints are truly public
        return path.startsWith("/kta/api/auth/") ||
              path.startsWith("/kta/api/public/") ||
              path.equals("/kta/api/actuator/health");
    }

    private boolean isSwaggerEndpoint(String path) {
        return path.startsWith("/kta/api/v3/api-docs") ||
              path.startsWith("/kta/api/swagger-ui") ||
              path.equals("/kta/api/swagger-ui.html");
    }

    private void unauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write(String.format(
              """
                    {
                      "success": false,
                      "message": "%s",
                      "statusCode": %d,
                      "timestamp": "%s"
                    }
                    """,
              message,
              HttpStatus.UNAUTHORIZED.value(),
              LocalDateTime.now()
        ));
    }
}
