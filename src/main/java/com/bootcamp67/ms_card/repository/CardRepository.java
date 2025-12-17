package com.bootcamp67.ms_card.repository;

import com.bootcamp67.ms_card.entity.Card;
import com.bootcamp67.ms_card.enums.CardStatus;
import com.bootcamp67.ms_card.enums.CardType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CardRepository extends ReactiveMongoRepository<Card, String> {

  /**
   * Find all cards by customer ID
   */
  Flux<Card> findByCustomerId(String customerId);

  /**
   * Find card by card number
   */
  Mono<Card> findByCardNumber(String cardNumber);

  /**
   * Find cards by customer ID and card type
   */
  Flux<Card> findByCustomerIdAndCardType(String customerId, CardType cardType);

  /**
   * Find cards by status
   */
  Flux<Card> findByStatus(CardStatus status);

  /**
   * Count cards by customer ID
   */
  Mono<Long> countByCustomerId(String customerId);

  /**
   * Check if card exists by card number
   */
  Mono<Boolean> existsByCardNumber(String cardNumber);
}
