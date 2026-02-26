package com.project.payroll.config;

import com.project.payroll.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        
        String requestURI = request.getRequestURI();
        
        // Public endpoints that don't require authentication
        if (isPublicEndpoint(requestURI)) {
            return true;
        }
        
        // Check if user is authenticated (has session)
        HttpSession session = request.getSession(false);
        User user = null;
        
        if (session != null) {
            user = (User) session.getAttribute("user");
        }
        
        // If user is not authenticated, redirect to login
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        
        // User is authenticated, allow the request
        return true;
    }
    
    /**
     * Determines if an endpoint is public and doesn't require authentication
     */
    private boolean isPublicEndpoint(String requestURI) {
        // We don't modify the URI; just check directly.  The previous logic
        // stripped the first segment which caused "/login" to become "" and
        // therefore it was never seen as public, leading to a redirect loop.
        
        // List of public endpoints (any request that starts with one of these
        // paths should be allowed without authentication).
        String[] publicPaths = {
            "/login",
            "/signup",
            "/",
            "/css",
            "/js",
            "/uploads",
            "/error"  // Spring error page may be served here
        };

        for (String path : publicPaths) {
            if (requestURI.equals(path) || requestURI.startsWith(path + "/")) {
                return true;
            }
        }

        return false;
    }
}
