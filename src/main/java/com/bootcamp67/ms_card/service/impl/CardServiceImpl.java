package com.bootcamp67.ms_card.service.impl;

import com.bootcamp67.ms_card.dto.AssociateAccountRequest;
import com.bootcamp67.ms_card.dto.CardDTO;
import com.bootcamp67.ms_card.dto.CreditCardRequest;
import com.bootcamp67.ms_card.dto.DebitCardRequest;
import com.bootcamp67.ms_card.dto.PaymentRequest;
import com.bootcamp67.ms_card.dto.TransactionDTO;
import com.bootcamp67.ms_card.entity.Card;
import com.bootcamp67.ms_card.enums.CardStatus;
import com.bootcamp67.ms_card.enums.CardType;
import com.bootcamp67.ms_card.event.CardCreatedEvent;
import com.bootcamp67.ms_card.event.producer.CardEventProducer;
import com.bootcamp67.ms_card.exception.CardNotFoundException;
import com.bootcamp67.ms_card.exception.InsufficientBalanceException;
import com.bootcamp67.ms_card.exception.InvalidCardOperationException;
import com.bootcamp67.ms_card.repository.CardRepository;
import com.bootcamp67.ms_card.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

  private final CardRepository cardRepository;
  private final CardEventProducer cardEventProducer;

  @Override
  public Flux<CardDTO> findAll() {
    log.info("Finding all cards");
    return cardRepository.findAll()
        .map(this::mapToDTO);
  }

  @Override
  public Mono<CardDTO> findById(String id) {
    log.info("Finding card by id: {}", id);
    return cardRepository.findById(id)
        .switchIfEmpty(Mono.error(new CardNotFoundException("Card not found with id: " + id)))
        .map(this::mapToDTO);
  }

  @Override
  public Flux<CardDTO> findByCustomerId(String customerId) {
    log.info("Finding cards by customer id: {}", customerId);
    return cardRepository.findByCustomerId(customerId)
        .map(this::mapToDTO);
  }

  @Override
  public Mono<CardDTO> createDebitCard(DebitCardRequest request) {
    log.info("Creating debit card for customer: {}", request.getCustomerId());

    Card card = Card.builder()
        .cardNumber(generateCardNumber())
        .customerId(request.getCustomerId())
        .cardType(CardType.DEBIT)
        .status(CardStatus.ACTIVE)
        .expirationDate(LocalDate.now().plusYears(5))
        .cvv(generateCVV())
        .mainAccountId(request.getMainAccountId())
        .createdAt(LocalDateTime.now())
        .build();

    // Add main account to associated accounts
    card.getAssociatedAccounts().add(request.getMainAccountId());

    return cardRepository.save(card)
        .flatMap(savedCard -> {
          log.info("Debit card created with id: {} and number: {}",
              savedCard.getId(), maskCardNumber(savedCard.getCardNumber()));

          // Emit card created event
          CardCreatedEvent event = CardCreatedEvent.builder()
              .cardId(savedCard.getId())
              .customerId(savedCard.getCustomerId())
              .cardType(savedCard.getCardType())
              .maskedCardNumber(maskCardNumber(savedCard.getCardNumber()))
              .expirationDate(savedCard.getExpirationDate())
              .mainAccountId(savedCard.getMainAccountId())
              .build();

          return cardEventProducer.publishCardCreated(event)
              .thenReturn(savedCard);
        })
        .map(this::mapToDTO);
  }

  @Override
  public Mono<CardDTO> createCreditCard(CreditCardRequest request) {
    log.info("Creating credit card for customer: {}", request.getCustomerId());

    Card card = Card.builder()
        .cardNumber(generateCardNumber())
        .customerId(request.getCustomerId())
        .cardType(CardType.CREDIT)
        .status(CardStatus.ACTIVE)
        .expirationDate(LocalDate.now().plusYears(5))
        .cvv(generateCVV())
        .creditId(request.getCreditId())
        .createdAt(LocalDateTime.now())
        .build();

    return cardRepository.save(card)
        .doOnSuccess(c -> log.info("Credit card created with id: {} and number: {}",
            c.getId(), maskCardNumber(c.getCardNumber())))
        .map(this::mapToDTO);
  }

  @Override
  public Mono<CardDTO> associateAccount(String cardId, AssociateAccountRequest request) {
    log.info("Associating account {} to card {}", request.getAccountId(), cardId);

    return cardRepository.findById(cardId)
        .switchIfEmpty(Mono.error(new CardNotFoundException("Card not found with id: " + cardId)))
        .flatMap(card -> {
          if (card.getCardType() != CardType.DEBIT) {
            return Mono.error(new InvalidCardOperationException(
                "Only debit cards can have associated accounts"));
          }

          if (card.getAssociatedAccounts().contains(request.getAccountId())) {
            return Mono.error(new InvalidCardOperationException(
                "Account is already associated with this card"));
          }

          card.getAssociatedAccounts().add(request.getAccountId());
          card.setUpdatedAt(LocalDateTime.now());

          return cardRepository.save(card);
        })
        .doOnSuccess(c -> log.info("Account {} associated to card {}", request.getAccountId(), cardId))
        .map(this::mapToDTO);
  }

  @Override
  public Mono<CardDTO> setMainAccount(String cardId, String accountId) {
    log.info("Setting main account {} for card {}", accountId, cardId);

    return cardRepository.findById(cardId)
        .switchIfEmpty(Mono.error(new CardNotFoundException("Card not found with id: " + cardId)))
        .flatMap(card -> {
          if (card.getCardType() != CardType.DEBIT) {
            return Mono.error(new InvalidCardOperationException(
                "Only debit cards have main account"));
          }

          if (!card.getAssociatedAccounts().contains(accountId)) {
            return Mono.error(new InvalidCardOperationException(
                "Account must be associated first before setting as main"));
          }

          card.setMainAccountId(accountId);
          card.setUpdatedAt(LocalDateTime.now());

          return cardRepository.save(card);
        })
        .doOnSuccess(c -> log.info("Main account set to {} for card {}", accountId, cardId))
        .map(this::mapToDTO);
  }

  @Override
  public Mono<CardDTO> blockCard(String cardId) {
    log.info("Blocking card: {}", cardId);

    return cardRepository.findById(cardId)
        .switchIfEmpty(Mono.error(new CardNotFoundException("Card not found with id: " + cardId)))
        .flatMap(card -> {
          if (card.getStatus() == CardStatus.BLOCKED) {
            return Mono.error(new InvalidCardOperationException("Card is already blocked"));
          }

          card.setStatus(CardStatus.BLOCKED);
          card.setUpdatedAt(LocalDateTime.now());
          return cardRepository.save(card);
        })
        .doOnSuccess(c -> log.info("Card {} blocked successfully", cardId))
        .map(this::mapToDTO);
  }

  @Override
  public Mono<CardDTO> activateCard(String cardId) {
    log.info("Activating card: {}", cardId);

    return cardRepository.findById(cardId)
        .switchIfEmpty(Mono.error(new CardNotFoundException("Card not found with id: " + cardId)))
        .flatMap(card -> {
          if (card.getStatus() == CardStatus.EXPIRED) {
            return Mono.error(new InvalidCardOperationException(
                "Cannot activate expired card. Request new card."));
          }

          if (card.getStatus() == CardStatus.ACTIVE) {
            return Mono.error(new InvalidCardOperationException("Card is already active"));
          }

          card.setStatus(CardStatus.ACTIVE);
          card.setUpdatedAt(LocalDateTime.now());
          return cardRepository.save(card);
        })
        .doOnSuccess(c -> log.info("Card {} activated successfully", cardId))
        .map(this::mapToDTO);
  }

  @Override
  public Mono<Void> processPayment(String cardId, PaymentRequest request) {
    log.info("Processing payment of {} for card {}", request.getAmount(), cardId);

    return cardRepository.findById(cardId)
        .switchIfEmpty(Mono.error(new CardNotFoundException("Card not found with id: " + cardId)))
        .flatMap(card -> {
          // Validate card status
          if (card.getStatus() != CardStatus.ACTIVE) {
            return Mono.error(new InvalidCardOperationException(
                "Card is not active. Status: " + card.getStatus()));
          }

          // Validate expiration
          if (card.getExpirationDate().isBefore(LocalDate.now())) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card).subscribe();
            return Mono.error(new InvalidCardOperationException("Card is expired"));
          }

          // Validate amount
          if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new InvalidCardOperationException("Amount must be positive"));
          }

          // Process based on card type
          if (card.getCardType() == CardType.DEBIT) {
            return processDebitPayment(card, request);
          } else {
            return processCreditPayment(card, request);
          }
        })
        .then();
  }

  /**
   * Process debit card payment with account cascade
   * Tries main account first, then cascades through associated accounts
   */
  private Mono<Void> processDebitPayment(Card card, PaymentRequest request) {
    log.info("Processing debit payment for card: {}", card.getId());

    if (card.getMainAccountId() == null || card.getAssociatedAccounts().isEmpty()) {
      return Mono.error(new InvalidCardOperationException(
          "No accounts associated with this debit card"));
    }

    // Try main account first
    return tryDebitFromAccount(card.getMainAccountId(), request.getAmount(), card.getId())
        .doOnSuccess(v -> log.info("Payment processed from main account: {}", card.getMainAccountId()))
        .onErrorResume(error -> {
          log.warn("Main account {} has insufficient funds. Trying cascade...",
              card.getMainAccountId());

          // Try other associated accounts in order
          List<String> otherAccounts = card.getAssociatedAccounts().stream()
              .filter(accountId -> !accountId.equals(card.getMainAccountId()))
              .collect(Collectors.toList());

          if (otherAccounts.isEmpty()) {
            return Mono.error(new InsufficientBalanceException(
                "Insufficient balance in main account and no other accounts available"));
          }

          return cascadeThroughAccounts(otherAccounts, request.getAmount(), card.getId(), 0);
        })
        .doOnSuccess(v -> log.info("Debit payment processed successfully for card: {}", card.getId()))
        .then();
  }

  /**
   * Cascade through associated accounts to find one with sufficient balance
   * Implements recursive cascade logic
   */
  private Mono<Void> cascadeThroughAccounts(List<String> accountIds,
                                            BigDecimal amount,
                                            String cardId,
                                            int index) {
    if (index >= accountIds.size()) {
      log.error("All {} associated accounts have insufficient balance for card {}",
          accountIds.size(), cardId);
      return Mono.error(new InsufficientBalanceException(
          "Insufficient balance in all associated accounts"));
    }

    String accountId = accountIds.get(index);
    log.info("Trying account {} (attempt {}/{})", accountId, index + 1, accountIds.size());

    return tryDebitFromAccount(accountId, amount, cardId)
        .doOnSuccess(v -> log.info("Payment processed from account {}", accountId))
        .onErrorResume(error -> {
          log.warn("Account {} has insufficient funds. Trying next account...", accountId);
          return cascadeThroughAccounts(accountIds, amount, cardId, index + 1);
        });
  }

  /**
   * Try to debit from specific account
   * TODO: Integrate with account service
   */
  private Mono<Void> tryDebitFromAccount(String accountId, BigDecimal amount, String cardId) {
    log.info("Attempting to debit {} from account {}", amount, accountId);

    // TODO: Call account service to check balance and debit
    // For now, simulate with random success/failure
    // In production:
    // 1. Call account service to get balance
    // 2. Check if balance >= amount
    // 3. If yes, debit the amount
    // 4. Create transaction record
    // 5. Emit event to Kafka

    // Simulated logic: 30% chance of insufficient balance for demonstration
    Random random = new Random();
    if (random.nextInt(10) < 3) {
      log.warn("Simulated insufficient balance in account: {}", accountId);
      return Mono.error(new InsufficientBalanceException(
          "Insufficient balance in account: " + accountId));
    }

    log.info("Successfully debited {} from account {}", amount, accountId);
    return Mono.empty();
  }

  /**
   * Process credit card payment
   * Charges to associated credit account
   */
  private Mono<Void> processCreditPayment(Card card, PaymentRequest request) {
    log.info("Processing credit payment for card: {}", card.getId());

    if (card.getCreditId() == null) {
      return Mono.error(new InvalidCardOperationException(
          "No credit account associated with this card"));
    }

    // TODO: Integrate with credit service
    // 1. Call credit service to get available credit
    // 2. Check if available credit >= amount
    // 3. If yes, charge to credit account
    // 4. Create transaction record
    // 5. Emit event to Kafka

    log.info("Successfully charged {} to credit account {}", request.getAmount(), card.getCreditId());
    return Mono.empty();
  }

  @Override
  public Mono<BigDecimal> getMainAccountBalance(String cardId) {
    log.info("Getting main account balance for card: {}", cardId);

    return cardRepository.findById(cardId)
        .switchIfEmpty(Mono.error(new CardNotFoundException("Card not found with id: " + cardId)))
        .flatMap(card -> {
          if (card.getCardType() != CardType.DEBIT) {
            return Mono.error(new InvalidCardOperationException(
                "Only debit cards have main account"));
          }

          if (card.getMainAccountId() == null) {
            return Mono.error(new IllegalStateException("No main account set"));
          }

          // TODO: Call account service to get balance
          // For now, return simulated balance
          BigDecimal simulatedBalance = new BigDecimal("1500.00");
          log.info("Returning simulated balance {} for card {}", simulatedBalance, cardId);
          return Mono.just(simulatedBalance);
        });
  }

  @Override
  public Flux<TransactionDTO> getLastTransactions(String cardId, Integer limit) {
    log.info("Getting last {} transactions for card: {}", limit, cardId);

    return cardRepository.findById(cardId)
        .switchIfEmpty(Mono.error(new CardNotFoundException("Card not found with id: " + cardId)))
        .flatMapMany(card -> {
          // TODO: Call transaction service to get last transactions
          // For now, return empty flux
          log.info("No transaction service integration yet. Returning empty list.");
          return Flux.empty();
        });
  }

  @Override
  public Mono<Void> delete(String id) {
    log.info("Deleting card: {}", id);
    return cardRepository.findById(id)
        .switchIfEmpty(Mono.error(new CardNotFoundException("Card not found with id: " + id)))
        .flatMap(card -> {
          log.info("Card {} deleted successfully", id);
          return cardRepository.delete(card);
        });
  }

  /**
   * Map Card entity to DTO with masked card number
   */
  private CardDTO mapToDTO(Card card) {
    return CardDTO.builder()
        .id(card.getId())
        .cardNumber(maskCardNumber(card.getCardNumber()))
        .customerId(card.getCustomerId())
        .cardType(card.getCardType())
        .status(card.getStatus())
        .expirationDate(card.getExpirationDate())
        .associatedAccounts(card.getAssociatedAccounts())
        .mainAccountId(card.getMainAccountId())
        .creditId(card.getCreditId())
        .createdAt(card.getCreatedAt())
        .updatedAt(card.getUpdatedAt())
        .build();
  }

  /**
   * Generate random card number (16 digits)
   * Format: 4444-5555-6666-7777
   */
  private String generateCardNumber() {
    Random random = new Random();
    StringBuilder cardNumber = new StringBuilder();

    // Generate 16 digits in format: XXXX-XXXX-XXXX-XXXX
    for (int i = 0; i < 16; i++) {
      cardNumber.append(random.nextInt(10));
      if ((i + 1) % 4 == 0 && i < 15) {
        cardNumber.append("-");
      }
    }

    String number = cardNumber.toString();
    log.debug("Generated card number: {}", maskCardNumber(number));
    return number;
  }

  /**
   * Generate random CVV (3 digits)
   */
  private String generateCVV() {
    Random random = new Random();
    String cvv = String.format("%03d", random.nextInt(1000));
    log.debug("Generated CVV: ***");
    return cvv;
  }

  /**
   * Mask card number for security (show only last 4 digits)
   * Example: 1234-5678-9012-3456 -> ****-****-****-3456
   */
  private String maskCardNumber(String cardNumber) {
    if (cardNumber == null || cardNumber.length() < 4) {
      return cardNumber;
    }

    String lastFour = cardNumber.substring(cardNumber.length() - 4);
    return "****-****-****-" + lastFour;
  }
}
