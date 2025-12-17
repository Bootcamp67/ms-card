package com.bootcamp67.ms_card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
  private String id;
  private String cardId;
  private String type;
  private BigDecimal amount;
  private String description;
  private String merchantName;
  private String accountId;
  private LocalDateTime transactionDate;
  private String status;
}
