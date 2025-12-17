package com.bootcamp67.ms_card.event.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventConsumer {

  @KafkaListener(
      topics = "account-events",
      groupId = "card-service-group",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void handleAccountEvent(String message) {
    log.info("Received account event: {}", message);

    try {
      log.info("Processing account event: {}", message);

    } catch (Exception e) {
      log.error("Error processing account event: {}", e.getMessage(), e);
    }
  }

  @KafkaListener(
      topics = "account-status-events",
      groupId = "card-service-group"
  )
  public void handleAccountStatusChange(String message) {
    log.info("Received account status change: {}", message);

    try {
    } catch (Exception e) {
      log.error("Error processing account status change: {}", e.getMessage(), e);
    }
  }
}
