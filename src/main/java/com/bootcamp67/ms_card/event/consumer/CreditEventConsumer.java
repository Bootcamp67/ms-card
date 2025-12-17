package com.bootcamp67.ms_card.event.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditEventConsumer {

  @KafkaListener(
      topics = "credit-events",
      groupId = "card-service-group",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void handleCreditEvent(String message) {
    log.info("Received credit event: {}", message);

    try {

      log.info("Processing credit event: {}", message);


    } catch (Exception e) {
      log.error("Error processing credit event: {}", e.getMessage(), e);
      // In production: send to DLQ (Dead Letter Queue)
    }
  }

  /**
   * Listen to credit status change events
   */
  @KafkaListener(
      topics = "credit-status-events",
      groupId = "card-service-group"
  )
  public void handleCreditStatusChange(String message) {
    log.info("Received credit status change: {}", message);

    try {

    } catch (Exception e) {
      log.error("Error processing credit status change: {}", e.getMessage(), e);
    }
  }

  @KafkaListener(
      topics = "credit-payment-events",
      groupId = "card-service-group"
  )
  public void handleCreditPayment(String message) {
    log.info("Received credit payment event: {}", message);

    try {
    } catch (Exception e) {
      log.error("Error processing credit payment: {}", e.getMessage(), e);
    }
  }
}
