package com.bootcamp67.ms_card.exception;

public class CardNotFoundException extends RuntimeException{
  public CardNotFoundException(String message) {
    super(message);
  }
}
