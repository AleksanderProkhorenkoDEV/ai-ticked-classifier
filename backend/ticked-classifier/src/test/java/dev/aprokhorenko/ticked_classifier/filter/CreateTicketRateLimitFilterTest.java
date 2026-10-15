package dev.aprokhorenko.ticked_classifier.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class CreateTicketRateLimitFilterTest {

    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final int MAX_REQUESTS = 3;
    private static final String CLIENT = "10.0.0.1";
    private static final String TOO_MANY_REQUESTS_BODY =
            "{\"message\":\"Demasiadas peticiones, inténtalo de nuevo en unos instantes\",\"status\":429}";

    @Mock
    private FilterChain chain;

    // Mutable so the window can move forward without the test sleeping.
    private MutableClock clock;
    private CreateTicketRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-15T10:00:00Z"));
        filter = new CreateTicketRateLimitFilter(MAX_REQUESTS, WINDOW, false, new ObjectMapper(), clock);
    }

    private MockHttpServletRequest requestFrom(String client) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/tickets/create");
        request.setRemoteAddr(client);
        return request;
    }

    private MockHttpServletResponse call(String client) throws IOException, ServletException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(requestFrom(client), response, chain);
        return response;
    }

    @Test
    @DisplayName("Deja pasar las peticiones que estan dentro del limite")
    void allowsTheRequestsUnderTheLimit() throws Exception {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertThat(call(CLIENT).getStatus()).isEqualTo(200);
        }

        verify(chain, times(MAX_REQUESTS)).doFilter(any(), any());
    }

    @Test
    @DisplayName("Bloquea con 429 en cuanto se supera el limite, sin llegar al controlador")
    void blocksOnceTheLimitIsExceeded() throws Exception {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            call(CLIENT);
        }

        MockHttpServletResponse response = call(CLIENT);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getHeader("Retry-After")).isEqualTo("60");
        assertThat(response.getContentAsString()).isEqualTo(TOO_MANY_REQUESTS_BODY);

        verify(chain, times(MAX_REQUESTS)).doFilter(any(), any());
    }

    @Test
    @DisplayName("El limite se cuenta por cliente, no de forma global")
    void countsEachClientSeparately() throws Exception {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            call(CLIENT);
        }

        assertThat(call(CLIENT).getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(call("10.0.0.2").getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("La ventana se reinicia pasado el tiempo configurado")
    void resetsTheCounterWhenTheWindowExpires() throws Exception {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            call(CLIENT);
        }
        assertThat(call(CLIENT).getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());

        clock.advance(WINDOW.plusSeconds(1));

        assertThat(call(CLIENT).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Sin trust-forwarded-for la cabecera se ignora y se usa la direccion remota")
    void ignoresTheForwardedAddressByDefault() throws Exception {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            call(CLIENT);
        }

        MockHttpServletRequest spoofed = requestFrom("10.0.0.9");
        spoofed.addHeader("X-Forwarded-For", CLIENT);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(spoofed, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Con trust-forwarded-for se cuenta la direccion original de la cabecera")
    void countsTheForwardedAddressWhenTrusted() throws Exception {
        CreateTicketRateLimitFilter trusting =
                new CreateTicketRateLimitFilter(MAX_REQUESTS, WINDOW, true, new ObjectMapper(), clock);

        MockHttpServletResponse lastResponse = null;
        for (int i = 0; i < MAX_REQUESTS + 1; i++) {
            MockHttpServletRequest request = requestFrom("10.0.0.9");
            request.addHeader("X-Forwarded-For", "10.0.0.1, 172.17.0.1");
            lastResponse = new MockHttpServletResponse();
            trusting.doFilter(request, lastResponse, chain);
        }

        // The left-most entry is the real client, so both requests count towards the same window.
        assertThat(lastResponse.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            this.instant = instant.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}