package com.bootcamp67.ms_card.event.producer;

import com.bootcamp67.ms_card.event.CardCreatedEvent;
import com.bootcamp67.ms_card.event.CardEvent;
import com.bootcamp67.ms_card.event.CardStatusChangedEvent;
import com.bootcamp67.ms_card.event.MainAccountChange;
import com.bootcamp67.ms_card.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardEventProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  // Kafka Topics
  private static final String CARD_EVENTS_TOPIC = "card-events";
  private static final String PAYMENT_EVENTS_TOPIC = "payment-events";
  private static final String CARD_STATUS_TOPIC = "card-status-events";

  public Mono<Void> publishCardCreated(CardCreatedEvent event) {
    log.info("Publishing card created event for card: {}", event.getCardId());

    CardEvent cardEvent = CardEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(CardEvent.EventType.CARD_CREATED)
        .cardId(event.getCardId())
        .customerId(event.getCustomerId())
        .timestamp(LocalDateTime.now())
        .payload(event)
        .build();

    return sendEvent(CARD_EVENTS_TOPIC, cardEvent.getCardId(), cardEvent);
  }

  public Mono<Void> publishPaymentProcessed(PaymentProcessedEvent event) {
    log.info("Publishing payment processed event for card: {} amount: {}",
        event.getCardId(), event.getAmount());

    CardEvent cardEvent = CardEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(CardEvent.EventType.PAYMENT_PROCESSED)
        .cardId(event.getCardId())
        .customerId(event.getCustomerId())
        .timestamp(LocalDateTime.now())
        .payload(event)
        .build();

    return sendEvent(PAYMENT_EVENTS_TOPIC, cardEvent.getCardId(), cardEvent);
  }

  public Mono<Void> publishCardBlocked(CardStatusChangedEvent event) {
    log.info("Publishing card blocked event for card: {}", event.getCardId());

    CardEvent cardEvent = CardEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(CardEvent.EventType.CARD_BLOCKED)
        .cardId(event.getCardId())
        .customerId(event.getCustomerId())
        .timestamp(LocalDateTime.now())
        .payload(event)
        .build();

    return sendEvent(CARD_STATUS_TOPIC, cardEvent.getCardId(), cardEvent);
  }

  public Mono<Void> publishCardActivated(CardStatusChangedEvent event) {
    log.info("Publishing card activated event for card: {}", event.getCardId());

    CardEvent cardEvent = CardEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(CardEvent.EventType.CARD_ACTIVATED)
        .cardId(event.getCardId())
        .customerId(event.getCustomerId())
        .timestamp(LocalDateTime.now())
        .payload(event)
        .build();

    return sendEvent(CARD_STATUS_TOPIC, cardEvent.getCardId(), cardEvent);
  }

  public Mono<Void> publishAccountAssociated(String cardId, String customerId, String accountId) {
    log.info("Publishing account associated event for card: {} account: {}", cardId, accountId);

    CardEvent cardEvent = CardEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(CardEvent.EventType.ACCOUNT_ASSOCIATED)
        .cardId(cardId)
        .customerId(customerId)
        .timestamp(LocalDateTime.now())
        .payload(accountId)
        .build();

    return sendEvent(CARD_EVENTS_TOPIC, cardEvent.getCardId(), cardEvent);
  }

  public Mono<Void> publishMainAccountChanged(String cardId, String customerId,
                                              String oldAccountId, String newAccountId) {
    log.info("Publishing main account changed event for card: {} from: {} to: {}",
        cardId, oldAccountId, newAccountId);

    CardEvent cardEvent = CardEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(CardEvent.EventType.MAIN_ACCOUNT_CHANGED)
        .cardId(cardId)
        .customerId(customerId)
        .timestamp(LocalDateTime.now())
        .payload(new MainAccountChange(oldAccountId, newAccountId))
        .build();

    return sendEvent(CARD_EVENTS_TOPIC, cardEvent.getCardId(), cardEvent);
  }

  private Mono<Void> sendEvent(String topic, String key, Object event) {
    return Mono.create(sink -> {
      try {
        ListenableFuture<SendResult<String, Object>> future =
            kafkaTemplate.send(topic, key, event);

        future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
          @Override
          public void onSuccess(SendResult<String, Object> result) {
            log.info("Event sent successfully to topic: {} partition: {} offset: {}",
                topic,
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
            sink.success();
          }

          @Override
          public void onFailure(Throwable ex) {
            log.error("Error sending event to topic: {} error: {}", topic, ex.getMessage(), ex);
            sink.error(ex);
          }
        });
      } catch (Exception e) {
        log.error("Exception sending event to topic: {}", topic, e);
        sink.error(e);
      }
    });
  }

}
