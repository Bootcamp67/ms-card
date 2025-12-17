package com.bootcamp67.ms_card.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MainAccountChange {
  private String oldAccountId;
  private String newAccountId;
}
