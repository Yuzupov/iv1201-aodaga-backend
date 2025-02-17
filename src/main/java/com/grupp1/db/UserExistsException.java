package com.grupp1.db;

public class UserExistsException extends Exception {

  public UserExistsException(String message) {
    super(message);
  }
}
