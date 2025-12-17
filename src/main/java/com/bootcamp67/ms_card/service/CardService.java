package com.bootcamp67.ms_card.service;

import com.bootcamp67.ms_card.dto.AssociateAccountRequest;
import com.bootcamp67.ms_card.dto.CardDTO;
import com.bootcamp67.ms_card.dto.CreditCardRequest;
import com.bootcamp67.ms_card.dto.DebitCardRequest;
import com.bootcamp67.ms_card.dto.PaymentRequest;
import com.bootcamp67.ms_card.dto.TransactionDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface CardService {

  /**
   * Find all cards
   */
  Flux<CardDTO> findAll();

  /**
   * Find card by ID
   */
  Mono<CardDTO> findById(String id);

  /**
   * Find cards by customer ID
   */
  Flux<CardDTO> findByCustomerId(String customerId);

  /**
   * Create debit card
   */
  Mono<CardDTO> createDebitCard(DebitCardRequest request);

  /**
   * Create credit card
   */
  Mono<CardDTO> createCreditCard(CreditCardRequest request);

  /**
   * Associate account to debit card
   */
  Mono<CardDTO> associateAccount(String cardId, AssociateAccountRequest request);

  /**
   * Set main account for debit card
   */
  Mono<CardDTO> setMainAccount(String cardId, String accountId);

  /**
   * Block card
   */
  Mono<CardDTO> blockCard(String cardId);

  /**
   * Activate card
   */
  Mono<CardDTO> activateCard(String cardId);

  /**
   * Process payment with card
   * For debit cards: tries main account first, then cascades through associated accounts
   * For credit cards: charges to credit account
   */
  Mono<Void> processPayment(String cardId, PaymentRequest request);

  /**
   * Get main account balance for debit card
   */
  Mono<BigDecimal> getMainAccountBalance(String cardId);

  /**
   * Get last transactions for card
   */
  Flux<TransactionDTO> getLastTransactions(String cardId, Integer limit);

  /**
   * Delete card
   */
  Mono<Void> delete(String id);
}
