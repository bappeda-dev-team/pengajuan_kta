package cc.kertaskerja.pengajuan_kta.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
          HttpServletRequest request,
          HttpServletResponse response,
          FilterChain filterChain
    ) throws ServletException, IOException {

        // 1️⃣ Ambil Authorization header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 2️⃣ Jika tidak ada token, lanjut tanpa autentikasi
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3️⃣ Ambil token
            String token = authHeader.substring(7);

            // 4️⃣ Parse token
            Map<String, Object> claims = jwtTokenProvider.parseToken(token);
            String username = (String) claims.get("sub");
            Long uid = ((Number) claims.get("uid")).longValue();
            String role = (String) claims.get("role");

            // 5️⃣ Set autentikasi ke SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                  new UsernamePasswordAuthenticationToken(username, null, null);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // Token invalid → skip authentication
            SecurityContextHolder.clearContext();
        }

        // 6️⃣ Lanjut ke filter berikutnya
        filterChain.doFilter(request, response);
    }
}
