package com.bootcamp67.ms_card.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class HeaderInterceptor implements WebFilter {
  private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
      "/actuator/health",
      "/actuator/info"
  );
  private static final String HEADER_USERNAME = "X-Auth-Username";
  private static final String HEADER_CUSTOMER_ID = "X-Auth-Customer-Id";
  private static final String HEADER_ROLE = "X-Auth-Role";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getPath().toString();

    if (isPublicEndpoint(path)) {
      log.debug("Public endpoint accessed: {}", path);
      return chain.filter(exchange);
    }

    String username = exchange.getRequest().getHeaders().getFirst(HEADER_USERNAME);
    String customerId = exchange.getRequest().getHeaders().getFirst(HEADER_CUSTOMER_ID);
    String role = exchange.getRequest().getHeaders().getFirst(HEADER_ROLE);

    if (username == null || username.isEmpty()) {
      log.warn("Missing or empty {} header for path: {}", HEADER_USERNAME, path);
      return unauthorized(exchange, "Missing authentication header: " + HEADER_USERNAME);
    }

    if (role == null || role.isEmpty()) {
      log.warn("Missing or empty {} header for path: {}", HEADER_ROLE, path);
      return unauthorized(exchange, "Missing authentication header: " + HEADER_ROLE);
    }

    log.info("Authenticated request - Username: {}, CustomerId: {}, Role: {}, Path: {}",
        username, customerId, role, path);
    exchange.getAttributes().put("username", username);
    exchange.getAttributes().put("customerId", customerId);
    exchange.getAttributes().put("role", role);

    return chain.filter(exchange);
  }
  private boolean isPublicEndpoint(String path) {
    return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    exchange.getResponse().getHeaders().add("Content-Type", "application/json");

    String errorBody = String.format(
        "{\"error\":\"Unauthorized\",\"message\":\"%s\",\"status\":401}",
        message
    );

    return exchange.getResponse().writeWith(
        Mono.just(exchange.getResponse().bufferFactory().wrap(errorBody.getBytes()))
    );
  }
}
