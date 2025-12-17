package com.bootcamp67.ms_card.util;

import org.springframework.web.server.ServerWebExchange;

public class SecurityContextUtil {
  private static final String USERNAME_ATTR = "username";
  private static final String CUSTOMER_ID_ATTR = "customerId";
  private static final String ROLE_ATTR = "role";

  public static String getUsername(ServerWebExchange exchange) {
    return (String) exchange.getAttribute(USERNAME_ATTR);
  }

  public static String getCustomerId(ServerWebExchange exchange) {
    return (String) exchange.getAttribute(CUSTOMER_ID_ATTR);
  }

  public static String getRole(ServerWebExchange exchange) {
    return (String) exchange.getAttribute(ROLE_ATTR);
  }

  public static boolean hasRole(ServerWebExchange exchange, String role) {
    String userRole = getRole(exchange);
    return userRole != null && userRole.equalsIgnoreCase(role);
  }

  public static boolean isAdmin(ServerWebExchange exchange) {
    return hasRole(exchange, "ADMIN");
  }

  public static boolean isOwnCustomer(ServerWebExchange exchange, String customerId) {
    String authCustomerId = getCustomerId(exchange);
    return authCustomerId != null && authCustomerId.equals(customerId);
  }
}
