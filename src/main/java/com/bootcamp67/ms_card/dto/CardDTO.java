package com.bootcamp67.ms_card.dto;

import com.bootcamp67.ms_card.enums.CardStatus;
import com.bootcamp67.ms_card.enums.CardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {
  private String id;
  private String cardNumber;
  private String customerId;
  private CardType cardType;
  private CardStatus status;
  private LocalDate expirationDate;
  private List<String> associatedAccounts;
  private String mainAccountId;
  private String creditId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
