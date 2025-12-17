package com.bootcamp67.ms_card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitCardRequest {
  @NotBlank(message = "Customer ID is required")
  private String customerId;

  @NotBlank(message = "Main account ID is required")
  private String mainAccountId;
}
