package cc.kertaskerja.pengajuan_kta.config;

import org.springframework.http.HttpStatus;
// import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

// @Component  // Comment this out temporarily
public class MultipartValidationFilter implements Filter {

    private static final Logger logger = Logger.getLogger(MultipartValidationFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // Only validate multipart requests
        if (httpRequest.getContentType() != null && 
            httpRequest.getContentType().startsWith("multipart/")) {
            
            // Basic validation
            if (httpRequest.getContentLength() <= 0) {
                logger.warning("Multipart request with zero or negative content length");
                ((HttpServletResponse) response).sendError(HttpStatus.BAD_REQUEST.value(), 
                    "Invalid multipart request: empty content");
                return;
            }
            
            // Check for boundary in Content-Type
            if (!httpRequest.getContentType().contains("boundary=")) {
                logger.warning("Multipart request missing boundary parameter");
                ((HttpServletResponse) response).sendError(HttpStatus.BAD_REQUEST.value(), 
                    "Invalid multipart request: missing boundary");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
}