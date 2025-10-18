package cc.kertaskerja.pengajuan_kta.security;

import cc.kertaskerja.pengajuan_kta.entity.Account;
import cc.kertaskerja.pengajuan_kta.enums.StatusEnum;
import cc.kertaskerja.pengajuan_kta.repository.AccountRepository;
import cc.kertaskerja.pengajuan_kta.service.auth.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AccountRepository accountRepository;
    private final TokenBlacklistService tokenBlacklistService;


    @Override
    protected void doFilterInternal(
          HttpServletRequest request,
          HttpServletResponse response,
          FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (tokenBlacklistService.isBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                    "success": false,
                    "statusCode": 401,
                    "message": "Token has been revoked. Please login again."
                }
            """);
            return;
        }


        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Map<String, Object> claims = jwtTokenProvider.parseToken(token);

            String username = (String) claims.get("sub");
            Long uid = ((Number) claims.get("uid")).longValue();

            Account account = accountRepository.findById(uid)
                  .filter(a -> a.getUsername().equals(username))
                  .orElse(null);

            // 🔒 If account not found or pending, reject all non-auth requests
            String path = request.getRequestURI();
            boolean isAuthEndpoint = path.startsWith("/auth");

            if (account == null || account.getStatus() == StatusEnum.PENDING) {
                if (!isAuthEndpoint) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("""
                        {
                            "success": false,
                            "statusCode": 403,
                            "message": "Your account is pending verification. Access denied."
                        }
                    """);
                    return;
                }
            }

            String role = (String) claims.get("role");
            if (!"ADMIN".equalsIgnoreCase(role) && request.getRequestURI().startsWith("/pengajuan/verify")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("""
                    {
                        "success": false,
                        "statusCode": 401,
                        "message": "Unauthorized: only ADMIN can verify pengajuan"
                    }
                """);

                return;
            }

            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken authentication =
                  new UsernamePasswordAuthenticationToken(username, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

}
