package com.bootcamp67.ms_card.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CardNotFoundException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleCardNotFound(CardNotFoundException ex) {
    log.error("Card not found: {}", ex.getMessage());
    return Mono.just(createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND));
  }

  @ExceptionHandler(InsufficientBalanceException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleInsufficientBalance(InsufficientBalanceException ex) {
    log.error("Insufficient balance: {}", ex.getMessage());
    return Mono.just(createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST));
  }

  @ExceptionHandler(InvalidCardOperationException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleInvalidOperation(InvalidCardOperationException ex) {
    log.error("Invalid card operation: {}", ex.getMessage());
    return Mono.just(createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgument(IllegalArgumentException ex) {
    log.error("Illegal argument: {}", ex.getMessage());
    return Mono.just(createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST));
  }

  @ExceptionHandler(IllegalStateException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleIllegalState(IllegalStateException ex) {
    log.error("Illegal state: {}", ex.getMessage());
    return Mono.just(createErrorResponse(ex.getMessage(), HttpStatus.CONFLICT));
  }

  @ExceptionHandler(WebExchangeBindException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleValidationErrors(WebExchangeBindException ex) {
    log.error("Validation error: {}", ex.getMessage());

    Map<String, String> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .collect(Collectors.toMap(
            FieldError::getField,
            error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
            (existing, replacement) -> existing
        ));

    Map<String, Object> response = new HashMap<>();
    response.put("error", "Validation Failed");
    response.put("message", "Invalid request data");
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("timestamp", LocalDateTime.now());
    response.put("validationErrors", errors);

    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
  }

  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleGeneral(Exception ex) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);
    return Mono.just(createErrorResponse("Internal server error: " + ex.getMessage(),
        HttpStatus.INTERNAL_SERVER_ERROR));
  }

  /**
   * Create standardized error response
   */
  private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
    Map<String, Object> error = new HashMap<>();
    error.put("error", status.getReasonPhrase());
    error.put("message", message);
    error.put("status", status.value());
    error.put("timestamp", LocalDateTime.now());

    return ResponseEntity.status(status).body(error);
  }
}
