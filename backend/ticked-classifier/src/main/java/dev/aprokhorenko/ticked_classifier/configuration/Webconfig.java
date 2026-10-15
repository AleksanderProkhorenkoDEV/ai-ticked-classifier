package dev.aprokhorenko.ticked_classifier.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration;

import dev.aprokhorenko.ticked_classifier.filter.CreateTicketRateLimitFilter;
import tools.jackson.databind.ObjectMapper;


@Configuration 
public class Webconfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PATCH" )
                .allowedHeaders("*");
    }

    // Only the creation endpoint is limited: it is the only one that calls the model.
    @Bean
    public FilterRegistrationBean<CreateTicketRateLimitFilter> createTicketRateLimitFilter(
            ObjectMapper objectMapper,
            @Value("${app.rate-limit.create-ticket.max-requests:5}") int maxRequests,
            @Value("${app.rate-limit.create-ticket.window:1m}") Duration window,
            @Value("${app.rate-limit.trust-forwarded-for:false}") boolean trustForwardedFor) {
        // Narrowing by URL only: /tickets/create has a single mapping and it is a POST.
        FilterRegistrationBean<CreateTicketRateLimitFilter> registration = new FilterRegistrationBean<>(
                new CreateTicketRateLimitFilter(maxRequests, window, trustForwardedFor, objectMapper));
        registration.addUrlPatterns("/tickets/create");
        return registration;
    }
}
