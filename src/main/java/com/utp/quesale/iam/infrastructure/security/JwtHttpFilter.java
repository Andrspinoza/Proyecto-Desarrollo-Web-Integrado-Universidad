package com.utp.quesale.iam.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class JwtHttpFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final boolean enabled;
    public JwtHttpFilter(JwtService jwt, @Value("${app.auth.enabled:true}") boolean enabled) {
        this.jwt = jwt; this.enabled = enabled;
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();
        if (!enabled || "OPTIONS".equals(method) || !path.startsWith("/api/v1/")) {
            chain.doFilter(request, response); return;
        }
        boolean publicRoute = ("POST".equals(method) && (path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/register") || path.equals("/api/v1/auth/resend-verification")
                || path.equals("/api/v1/auth/forgot-password") || path.equals("/api/v1/auth/reset-password")))
                || ("GET".equals(method) && (path.equals("/api/v1/universities") || path.matches("/api/v1/universities/[0-9]+")
                || path.equals("/api/v1/email-verifications/confirm")));
        if (publicRoute) { chain.doFilter(request, response); return; }
        if ((path.equals("/api/v1/students") && "POST".equals(method))
                || ((path.startsWith("/api/v1/universities") || path.startsWith("/api/v1/skills") || path.startsWith("/api/v1/interests")) && !"GET".equals(method))
                || (path.startsWith("/api/v1/reports") && !"POST".equals(method))) {
            response.sendError(403); return;
        }
        String authorization = request.getHeader("Authorization");
        Long id = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try { id = jwt.verify(authorization.substring(7)); } catch (IllegalStateException ignored) { }
        }
        if (id == null) { response.sendError(401); return; }
        final String studentId = id.toString();
        chain.doFilter(new HttpServletRequestWrapper(request) {
            @Override public String getHeader(String name) {
                return "X-Student-Id".equalsIgnoreCase(name) ? studentId : super.getHeader(name);
            }
            @Override public Enumeration<String> getHeaders(String name) {
                return "X-Student-Id".equalsIgnoreCase(name) ? Collections.enumeration(List.of(studentId)) : super.getHeaders(name);
            }
            @Override public Enumeration<String> getHeaderNames() {
                Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                Collections.list(super.getHeaderNames()).forEach(names::add); names.add("X-Student-Id");
                return Collections.enumeration(names);
            }
        }, response);
    }
}
