package cc.kertaskerja.pengajuan_kta.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                        AuthenticationException authException) throws IOException {
        
        response.addHeader("WWW-Authenticate", "Basic realm=\"" + getRealmName() + "\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // For Swagger paths, force Basic Auth popup
        String requestPath = request.getRequestURI();
        if (requestPath.contains("swagger-ui") || requestPath.contains("api-docs")) {
            response.getWriter().write("""
                {
                    "success": false,
                    "statusCode": 401,
                    "message": "Basic authentication required for Swagger UI access"
                }
                """);
        } else {
            response.getWriter().write("""
                {
                    "success": false,
                    "statusCode": 401,
                    "message": "Basic authentication required"
                }
                """);
        }
    }

    @Override
    public void afterPropertiesSet() {
        setRealmName("Swagger UI Access");
        super.afterPropertiesSet();
    }
}
