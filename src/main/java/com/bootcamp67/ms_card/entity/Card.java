package com.bootcamp67.ms_card.entity;

import com.bootcamp67.ms_card.enums.CardStatus;
import com.bootcamp67.ms_card.enums.CardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cards")
public class Card {

  @Id
  private String id;
  private String cardNumber;
  private String customerId;
  private CardType cardType;
  private CardStatus status;
  private LocalDate expirationDate;
  private String cvv;
  @Builder.Default
  private List<String> associatedAccounts = new ArrayList<>();
  private String mainAccountId;
  private String creditId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
