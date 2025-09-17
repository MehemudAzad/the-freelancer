package com.thefreelancer.microservices.gateway.filter;

import com.thefreelancer.microservices.gateway.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    // Public endpoints that don't require authentication
    private final List<String> publicEndpoints = Arrays.asList(
        "/api/auth/register",
        "/api/auth/register-basic", 
        "/api/auth/login",
    "/api/auth/public/users",
        "/api/auth/refresh",
        "/api/gigs/search",  // Public search
        "/api/jobs/search",  // Public job search
        "/api/payments/webhooks", // Stripe webhooks (external)
        "/swagger-ui",       // Swagger UI
        "/api-docs",         // OpenAPI docs
        "/v3/api-docs"       // OpenAPI v3 docs
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("Processing request: {} {}", method, requestURI);
        
        // Allow OPTIONS requests (CORS preflight)
        if ("OPTIONS".equals(method)) {
            return true;
        }
        
        // Check if this is a public endpoint
        if (isPublicEndpoint(requestURI, method)) {
            log.debug("Public endpoint, allowing access: {}", requestURI);
            return true;
        }
        
        // Extract Authorization header
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for protected endpoint: {}", requestURI);
            setErrorResponse(response, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            return false;
        }
        
        String token = authHeader.substring(7);
        
        try {
            // Validate JWT
            if (!jwtUtil.isTokenValid(token)) {
                log.warn("Invalid or expired token for endpoint: {}", requestURI);
                setErrorResponse(response, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                return false;
            }
            
            // Extract user information and add to request attributes
            Long userId = jwtUtil.extractUserId(token);
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);
            
            log.debug("Authentication successful for user: {} ({})", email, userId);
            
            // Add user context to request attributes (will be forwarded to downstream services)
            request.setAttribute("X-User-Id", userId.toString());
            request.setAttribute("X-User-Email", email);
            request.setAttribute("X-User-Role", role);
            
            return true;
            
        } catch (Exception e) {
            log.error("Token validation failed for endpoint {}: {}", requestURI, e.getMessage());
            setErrorResponse(response, "Token validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            return false;
        }
    }
    
    private boolean isPublicEndpoint(String requestURI, String method) {
        // Check exact matches first
        if (publicEndpoints.stream().anyMatch(requestURI::startsWith)) {
            return true;
        }
        
        // Special case: GET /api/gigs/{id} is public (individual gig details)
        if ("GET".equals(method) && requestURI.matches("/api/gigs/\\d+")) {
            return true;
        }

        // Special case: GET /api/gigs/{id}/media is public (gig media list)
        if ("GET".equals(method) && requestURI.matches("/api/gigs/\\d+/media")) {
            return true;
        }
        
        // Special case: GET /api/jobs/{id} is public (individual job details)
        if ("GET".equals(method) && requestURI.matches("/api/jobs/\\d+")) {
            return true;
        }
        
        // Special case: Job milestones endpoints are public (GET only)
        if ("GET".equals(method) && requestURI.matches("/api/jobs/\\d+/milestones")) {
            return true;
        }

        // Special case: GET /api/reviews/{id} is public (individual review details)
        if ("GET".equals(method) && requestURI.matches("/api/reviews/\\d+")) {
            return true;
        }

        // Special case: GET /api/reviews/freelancers/{id} is public (freelancer reviews)
        if ("GET".equals(method) && requestURI.matches("/api/reviews/freelancers/\\d+")) {
            return true;
        }

        // Special case: GET /api/reviews/gigs/{id} is public (gig reviews)
        if ("GET".equals(method) && requestURI.matches("/api/reviews/gigs/\\d+")) {
            return true;
        }

        // Special case: Job attachments by kind should be public (GET only)
        if ("GET".equals(method) && requestURI.matches("/api/jobs/\\d+/attachments/\\w+")) {
            return true;
        }

        // Special case: Job attachments list should be public (GET only)
        if ("GET".equals(method) && requestURI.matches("/api/jobs/\\d+/attachments")) {
            return true;
        }

        // Special case: Proposal milestones endpoints are public (GET only)
        if ("GET".equals(method) && requestURI.matches("/api/proposals/\\d+/milestones")) {
            return true;
        }

        // Special case: Individual proposal milestone details are public (GET only)
        if ("GET".equals(method) && requestURI.matches("/api/proposals/\\d+/milestones/\\d+")) {
            return true;
        }

        // Special case: Individual proposal milestone details are public (GET only)
        if ("GET".equals(method) && requestURI.matches("/api/reviews/\\d+")) {
            return true;
        }

        // Special case: Individual proposal milestone details are public (GET only)
        if ("GET".equals(method) && requestURI.matches("/api/reviews/freelancers/\\d+")) {
            return true;
        }

        // Special case: Individual proposal milestone details are public (GET only)
        if ("GET".equals(method) && requestURI.matches("/api/reviews/freelancers/\\d+/summary")) {
            return true;
        }

        // Special case: Payment webhook endpoints are public (for external services like Stripe)
        if (requestURI.startsWith("/api/payments/webhooks/")) {
            return true;
        }

        // Special case: Webhook health check endpoints are public
        if ("GET".equals(method) && requestURI.matches("/api/payments/webhooks/.*/health")) {
            return true;
        }

        // Add specific public contract endpoints if needed in the future
        // For now, all contract endpoints require authentication
        
        return false;
    }
    
    private void setErrorResponse(HttpServletResponse response, String message, HttpStatus status) throws Exception {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"error\": \"%s\", \"status\": %d}", message, status.value()));
    }
}
