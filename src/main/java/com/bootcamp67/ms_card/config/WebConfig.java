package com.bootcamp67.ms_card.config;

import com.bootcamp67.ms_card.interceptor.HeaderInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class WebConfig {
  private final HeaderInterceptor headerInterceptor;

  /**
   * Register security interceptor as WebFilter
   */
  @Bean
  public WebFilter securityFilter() {
    return headerInterceptor;
  }

  @Bean
  public CorsWebFilter corsWebFilter() {
    CorsConfiguration corsConfig = new CorsConfiguration();
    corsConfig.setAllowedOrigins(Arrays.asList("*"));
    corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    corsConfig.setAllowedHeaders(Arrays.asList("*"));
    corsConfig.setExposedHeaders(Arrays.asList(
        "X-Auth-Username",
        "X-Auth-Customer-Id",
        "X-Auth-Role",
        "Content-Type",
        "Authorization"
    ));
    corsConfig.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);

    return new CorsWebFilter(source);
  }
}
