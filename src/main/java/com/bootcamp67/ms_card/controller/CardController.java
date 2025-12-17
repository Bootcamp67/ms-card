package com.bootcamp67.ms_card.controller;

import com.bootcamp67.ms_card.dto.AssociateAccountRequest;
import com.bootcamp67.ms_card.dto.BalanceResponse;
import com.bootcamp67.ms_card.dto.CardDTO;
import com.bootcamp67.ms_card.dto.CardResponse;
import com.bootcamp67.ms_card.dto.CreditCardRequest;
import com.bootcamp67.ms_card.dto.DebitCardRequest;
import com.bootcamp67.ms_card.dto.PaymentRequest;
import com.bootcamp67.ms_card.dto.TransactionDTO;
import com.bootcamp67.ms_card.service.CardService;
import com.bootcamp67.ms_card.util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

  private final CardService cardService;

  @GetMapping
  public Mono<ResponseEntity<Flux<CardDTO>>> findAll(ServerWebExchange exchange) {
    String username = SecurityContextUtil.getUsername(exchange);
    String role = SecurityContextUtil.getRole(exchange);

    log.info("REST request to get all cards by user: {} with role: {}", username, role);

    if (!SecurityContextUtil.isAdmin(exchange)) {
      log.warn("User {} attempted to access all cards without ADMIN role", username);
      return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    return Mono.just(ResponseEntity.ok(cardService.findAll()));
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<CardDTO>> findById(
      @PathVariable String id,
      ServerWebExchange exchange) {
    String username = SecurityContextUtil.getUsername(exchange);
    log.info("REST request to get card by id: {} by user: {}", id, username);

    return cardService.findById(id)
        .flatMap(card -> validateCardOwnership(exchange, card))
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @GetMapping("/customer/{customerId}")
  public Mono<ResponseEntity<Flux<CardDTO>>> findByCustomerId(
      @PathVariable String customerId,
      ServerWebExchange exchange) {
    String username = SecurityContextUtil.getUsername(exchange);
    String authCustomerId = SecurityContextUtil.getCustomerId(exchange);

    log.info("REST request to get cards by customer id: {} by user: {}", customerId, username);

    if (!SecurityContextUtil.isAdmin(exchange) && !customerId.equals(authCustomerId)) {
      log.warn("User {} attempted to access cards of customer {} without permission",
          username, customerId);
      return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    return Mono.just(ResponseEntity.ok(cardService.findByCustomerId(customerId)));
  }

  @PostMapping("/debit")
  public Mono<ResponseEntity<CardResponse>> createDebitCard(
      @RequestBody @Valid DebitCardRequest request,
      ServerWebExchange exchange) {
    String username = SecurityContextUtil.getUsername(exchange);
    String authCustomerId = SecurityContextUtil.getCustomerId(exchange);

    log.info("REST request to create debit card for customer: {} by user: {}",
        request.getCustomerId(), username);

    if (!SecurityContextUtil.isAdmin(exchange) &&
        !request.getCustomerId().equals(authCustomerId)) {
      log.warn("User {} attempted to create card for customer {} without permission",
          username, request.getCustomerId());
      return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(CardResponse.builder()
              .success(false)
              .message("You can only create cards for yourself")
              .build()));
    }

    return cardService.createDebitCard(request)
        .map(cardDTO -> ResponseEntity.status(HttpStatus.CREATED)
            .body(CardResponse.builder()
                .success(true)
                .message("Debit card created successfully")
                .data(cardDTO)
                .build()));
  }

  @PostMapping("/credit")
  public Mono<ResponseEntity<CardResponse>> createCreditCard(
      @RequestBody @Valid CreditCardRequest request,
      ServerWebExchange exchange) {
    String username = SecurityContextUtil.getUsername(exchange);
    String authCustomerId = SecurityContextUtil.getCustomerId(exchange);

    log.info("REST request to create credit card for customer: {} by user: {}",
        request.getCustomerId(), username);

    if (!SecurityContextUtil.isAdmin(exchange) &&
        !request.getCustomerId().equals(authCustomerId)) {
      log.warn("User {} attempted to create card for customer {} without permission",
          username, request.getCustomerId());
      return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(CardResponse.builder()
              .success(false)
              .message("You can only create cards for yourself")
              .build()));
    }

    return cardService.createCreditCard(request)
        .map(cardDTO -> ResponseEntity.status(HttpStatus.CREATED)
            .body(CardResponse.builder()
                .success(true)
                .message("Credit card created successfully")
                .data(cardDTO)
                .build()));
  }

  @PostMapping("/{id}/associate-account")
  public Mono<ResponseEntity<CardResponse>> associateAccount(
      @PathVariable String id,
      @RequestBody @Valid AssociateAccountRequest request,
      ServerWebExchange exchange) {
    String username = SecurityContextUtil.getUsername(exchange);
    log.info("REST request to associate account {} to card {} by user: {}",
        request.getAccountId(), id, username);

    return cardService.findById(id)
        .flatMap(card -> validateCardOwnership(exchange, card))
        .flatMap(card -> cardService.associateAccount(id, request))
        .map(cardDTO -> ResponseEntity.ok(CardResponse.builder()
            .success(true)
            .message("Account associated successfully")
            .data(cardDTO)
            .build()))
        .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(CardResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build())));
  }

  @PutMapping("/{id}/main-account/{accountId}")
  public Mono<ResponseEntity<CardResponse>> setMainAccount(
      @PathVariable String id,
      @PathVariable String accountId,
      ServerWebExchange exchange) {
    String username = SecurityContextUtil.getUsername(exchange);
    log.info("REST request to set main account {} for card {} by user: {}",
        accountId, id, username);

    return cardService.findById(id)
        .flatMap(card -> validateCardOwnership(exchange, card))
        .flatMap(card -> cardService.setMainAccount(id, accountId))
        .map(cardDTO -> ResponseEntity.ok(CardResponse.builder()
            .success(true)
            .message("Main account set successfully")
            .data(cardDTO)
            .build()))
        .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(CardResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build())));
  }

  @PutMapping("/{id}/block")
  public Mono<ResponseEntity<CardResponse>> blockCard(
      @PathVariable String id,
      ServerWebExchange exchange) {
    String username = SecurityContextUtil.getUsername(exchange);
    log.info("REST request to block card: {} by user: {}", id, username);

    return cardService.findById(id)
        .flatMap(card -> validateCardOwnership(exchange, card))
        .flatMap(card -> cardService.blockCard(id))
        .map(cardDTO -> ResponseEntity.ok(CardResponse.builder()
            .success(true)
            .message("Card blocked successfully")
            .data(cardDTO)
            .build()))
        .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(CardResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build())));
  }

  @PutMapping("/{id}/activate")
  public Mono<ResponseEntity<CardResponse>> activateCard(
      @PathVariable String id,
      ServerWebExchange exchange) {
    String username = SecurityContextUtil.getUsername(exchange);
    log.info("REST request to activate card: {} by user: {}", id, username);

    return cardService.findById(id)
        .flatMap(card -> validateCardOwnership(exchange, card))
        .flatMap(card -> cardService.activateCard(id))
        .map(cardDTO -> ResponseEntity.ok(CardResponse.builder()
            .success(true)
            .message("Card activated successfully")
            .data(cardDTO)
            .build()))
        .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(CardResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build())));
  }

  @PostMapping("/{id}/payment")
  public Mono<ResponseEntity<CardResponse>> processPayment(
      @PathVariable String id,
      @RequestBody @Valid PaymentRequest request,
      ServerWebExchange exchange) {
    String username = SecurityContextUtil.getUsername(exchange);
    log.info("REST request to process payment of {} for card {} by user: {}",
        request.getAmount(), id, username);

    return cardService.findById(id)
        .flatMap(card -> validateCardOwnership(exchange, card))
        .flatMap(card -> cardService.processPayment(id, request))
        .then(Mono.just(ResponseEntity.ok(CardResponse.builder()
            .success(true)
            .message("Payment processed successfully")
            .data(null)
            .build())))
        .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(CardResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build())));
  }

  @GetMapping("/{id}/balance")
  public Mono<ResponseEntity<BalanceResponse>> getBalance(
      @PathVariable String id,
      ServerWebExchange exchange) {
    String username = SecurityContextUtil.getUsername(exchange);
    log.info("REST request to get balance for card: {} by user: {}", id, username);

    return cardService.findById(id)
        .flatMap(card -> validateCardOwnership(exchange, card))
        .flatMap(card -> cardService.getMainAccountBalance(id))
        .map(balance -> ResponseEntity.ok(BalanceResponse.builder()
            .cardId(id)
            .balance(balance)
            .build()))
        .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build()));
  }

  @GetMapping("/{id}/transactions")
  public Mono<ResponseEntity<Flux<TransactionDTO>>> getTransactions(
      @PathVariable String id,
      @RequestParam(defaultValue = "10") Integer limit,
      ServerWebExchange exchange) {
    String username = SecurityContextUtil.getUsername(exchange);
    log.info("REST request to get last {} transactions for card: {} by user: {}",
        limit, id, username);

    return cardService.findById(id)
        .flatMap(card -> validateCardOwnership(exchange, card))
        .map(card -> ResponseEntity.ok(cardService.getLastTransactions(id, limit)))
        .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build()));
  }

  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<Void>> delete(
      @PathVariable String id,
      ServerWebExchange exchange) {
    String username = SecurityContextUtil.getUsername(exchange);
    log.info("REST request to delete card: {} by user: {}", id, username);

    return cardService.findById(id)
        .flatMap(card -> validateCardOwnership(exchange, card))
        .flatMap(card -> cardService.delete(id))
        .then(Mono.just(ResponseEntity.noContent().<Void>build()))
        .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build()));
  }

  private Mono<CardDTO> validateCardOwnership(ServerWebExchange exchange, CardDTO card) {
    String authCustomerId = SecurityContextUtil.getCustomerId(exchange);
    String username = SecurityContextUtil.getUsername(exchange);

    if (SecurityContextUtil.isAdmin(exchange)) {
      return Mono.just(card);
    }

    if (authCustomerId == null || !authCustomerId.equals(card.getCustomerId())) {
      log.warn("User {} attempted to access card {} owned by customer {} without permission",
          username, card.getId(), card.getCustomerId());
      return Mono.error(new SecurityException("You don't have permission to access this card"));
    }

    return Mono.just(card);
  }
}
