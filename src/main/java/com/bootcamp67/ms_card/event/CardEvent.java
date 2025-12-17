package com.bootcamp67.ms_card.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardEvent {

  private String eventId;
  private String eventType;
  private String cardId;
  private String customerId;
  private LocalDateTime timestamp;
  private Object payload;

  public static class EventType {
    public static final String CARD_CREATED = "CARD_CREATED";
    public static final String CARD_BLOCKED = "CARD_BLOCKED";
    public static final String CARD_ACTIVATED = "CARD_ACTIVATED";
    public static final String PAYMENT_PROCESSED = "PAYMENT_PROCESSED";
    public static final String ACCOUNT_ASSOCIATED = "ACCOUNT_ASSOCIATED";
    public static final String MAIN_ACCOUNT_CHANGED = "MAIN_ACCOUNT_CHANGED";
  }
}
