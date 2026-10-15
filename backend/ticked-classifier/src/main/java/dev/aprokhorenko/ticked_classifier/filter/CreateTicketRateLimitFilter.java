package dev.aprokhorenko.ticked_classifier.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import dev.aprokhorenko.ticked_classifier.dto.GlobalResponseDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Creating a ticket costs one moderation call plus one classification call, so an open endpoint
 * is an easy way to burn the Mistral quota. The counters live in memory on purpose: they only
 * have to blunt bursts, and losing them on restart is acceptable for that.
 */
public class CreateTicketRateLimitFilter extends OncePerRequestFilter {

    // Bound on the tracked clients map. Without it a bot rotating source addresses would grow
    // the map without limit.
    private static final int MAX_TRACKED_CLIENTS = 10_000;

    private static final String MESSAGE = "Demasiadas peticiones, inténtalo de nuevo en unos instantes";

    private final int maxRequests;
    private final Duration window;
    private final boolean trustForwardedFor;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public CreateTicketRateLimitFilter(int maxRequests, Duration window, boolean trustForwardedFor,
            ObjectMapper objectMapper) {
        this(maxRequests, window, trustForwardedFor, objectMapper, Clock.systemUTC());
    }

    CreateTicketRateLimitFilter(int maxRequests, Duration window, boolean trustForwardedFor,
            ObjectMapper objectMapper, Clock clock) {
        this.maxRequests = maxRequests;
        this.window = window;
        this.trustForwardedFor = trustForwardedFor;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (registerRequest(clientKey(request))) {
            respondTooManyRequests(response);
            return;
        }

        chain.doFilter(request, response);
    }

    /** Returns true when the client has already spent its quota for the current window. */
    private boolean registerRequest(String client) {
        Instant now = clock.instant();
        evictExpiredClients(now);

        Counter counter = counters.compute(client, (key, current) -> {
            if (current == null || !current.isActive(now)) {
                return new Counter(now, 1);
            }
            current.hits++;
            return current;
        });

        return counter.hits > maxRequests;
    }

    // Only sweeps when the map grew past the cap, so the common request path stays O(1).
    private void evictExpiredClients(Instant now) {
        if (counters.size() <= MAX_TRACKED_CLIENTS) {
            return;
        }
        counters.values().removeIf(counter -> !counter.isActive(now));
    }

    private String clientKey(HttpServletRequest request) {
        if (trustForwardedFor) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                // Left-most entry is the original client, the rest are the proxies it crossed.
                return forwardedFor.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    private void respondTooManyRequests(HttpServletResponse response) throws IOException {
        GlobalResponseDTO body = new GlobalResponseDTO(MESSAGE, HttpStatus.TOO_MANY_REQUESTS.value());

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(Math.max(1, window.toSeconds())));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private final class Counter {

        private final Instant startedAt;
        private int hits;

        private Counter(Instant startedAt, int hits) {
            this.startedAt = startedAt;
            this.hits = hits;
        }

        private boolean isActive(Instant now) {
            return startedAt.plus(window).isAfter(now);
        }
    }
}