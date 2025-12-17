package com.bootcamp67.ms_card.event.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventConsumer {

  @KafkaListener(
      topics = "customer-events",
      groupId = "card-service-group",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void handleCustomerEvent(String message) {
    log.info("Received customer event: {}", message);

    try {
      // TODO: Parse event and handle according to event type
      // Example events:
      // - CUSTOMER_BLOCKED -> Block all customer cards
      // - CUSTOMER_DELETED -> Delete or archive all cards
      // - CUSTOMER_UPGRADED -> Update card tier/benefits

      log.info("Processing customer event: {}", message);

      // For now, just log. In production:
      // 1. Parse JSON to CustomerEvent object
      // 2. Check event type
      // 3. Find all cards for this customer
      // 4. Execute corresponding action (block, delete, update)

    } catch (Exception e) {
      log.error("Error processing customer event: {}", e.getMessage(), e);
      // In production: send to DLQ (Dead Letter Queue)
    }
  }

  /**
   * Listen to customer status change events
   */
  @KafkaListener(
      topics = "customer-status-events",
      groupId = "card-service-group"
  )
  public void handleCustomerStatusChange(String message) {
    log.info("Received customer status change: {}", message);

    try {
      // TODO: Implement logic
      // If customer is blocked -> Block all their cards
      // If customer is VIP -> Upgrade card benefits

    } catch (Exception e) {
      log.error("Error processing customer status change: {}", e.getMessage(), e);
    }
  }
}
