package com.aifp.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final int maxRequestsPerMinute;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public RateLimitFilter(@Value("${aifp.security.rate-limit-per-minute:60}") int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientKey = request.getRemoteAddr() + ":" + (request.getRequestURI().startsWith("/api/v1/chat") ? "AI" : "API");
        long currentMinute = Instant.now().getEpochSecond() / 60;
        WindowCounter counter = counters.compute(clientKey, (k, existing) -> {
            if (existing == null || existing.minuteSlot != currentMinute) {
                return new WindowCounter(currentMinute, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        if (counter.count.get() > maxRequestsPerMinute) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"error\":\"Rate limit exceeded. Please wait before retrying.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private record WindowCounter(long minuteSlot, AtomicInteger count) {}
}

