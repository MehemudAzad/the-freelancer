package com.thefreelancer.microservices.workspace_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket handshake interceptor for authentication
 */
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, 
                                 @NonNull ServerHttpResponse response,
                                 @NonNull WebSocketHandler wsHandler, 
                                 @NonNull Map<String, Object> attributes) {
        
        // Extract authentication info from headers
        String userId = extractHeader(request, "X-User-Id");
        String userRole = extractHeader(request, "X-User-Role");
        String userEmail = extractHeader(request, "X-User-Email");
        
        if (userId != null && userRole != null) {
            // Store user info in WebSocket session
            attributes.put("userId", userId);
            attributes.put("userRole", userRole);
            attributes.put("userEmail", userEmail);
            
            log.info("WebSocket connection authenticated for user: {}", userId);
            return true;
        } else {
            log.warn("WebSocket connection rejected - missing authentication headers");
            return false; // Reject connection
        }
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, 
                             @NonNull ServerHttpResponse response,
                             @NonNull WebSocketHandler wsHandler, 
                             @Nullable Exception exception) {
        // No action needed after handshake
    }
    
    private String extractHeader(ServerHttpRequest request, String headerName) {
        return request.getHeaders().getFirst(headerName);
    }
}
