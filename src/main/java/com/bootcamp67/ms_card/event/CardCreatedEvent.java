package com.bootcamp67.ms_card.event;

import com.bootcamp67.ms_card.enums.CardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardCreatedEvent {
  private String cardId;
  private String customerId;
  private CardType cardType;
  private String maskedCardNumber;
  private LocalDate expirationDate;
  private String mainAccountId;  // For debit cards
  private String creditId;       // For credit cards
}
