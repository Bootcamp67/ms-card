package com.bootcamp67.ms_card.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {

  private String cardId;
  private String customerId;
  private String accountId;      // Account that was debited
  private BigDecimal amount;
  private String description;
  private String merchantName;
  private String transactionId;
  private Boolean wasMainAccount;  // True if main account was used, false if cascade
}
