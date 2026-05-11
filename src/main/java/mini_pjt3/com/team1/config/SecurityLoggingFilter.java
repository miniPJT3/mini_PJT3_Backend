package mini_pjt3.com.team1.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mini_pjt3.com.team1.service.AdminSecurityService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityLoggingFilter extends OncePerRequestFilter {

    private final AdminSecurityService adminSecurityService;

    public SecurityLoggingFilter(AdminSecurityService adminSecurityService) {
        this.adminSecurityService = adminSecurityService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/admin");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } finally {
            String username = resolveUsername();
            String ipAddress = resolveClientIp(request);
            String method = request.getMethod();
            String path = request.getRequestURI();
            int statusCode = response.getStatus();
            String userAgent = request.getHeader("User-Agent");

            adminSecurityService.recordAdminAccess(
                    username,
                    ipAddress,
                    method,
                    path,
                    statusCode,
                    userAgent
            );

            if (statusCode == 401 || statusCode == 403) {
                adminSecurityService.recordViolation(
                        ipAddress,
                        method,
                        path,
                        statusCode,
                        userAgent
                );
            }
        }
    }

    private String resolveUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return "anonymous";
        }

        return authentication.getName();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");

        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}