package com.bootcamp67.ms_card.event;

import com.bootcamp67.ms_card.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardStatusChangedEvent {

  private String cardId;
  private String customerId;
  private CardStatus previousStatus;
  private CardStatus newStatus;
  private String reason;
}
