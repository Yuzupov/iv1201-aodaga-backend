package com.grupp1.db;

public class NoSuchUserException extends Exception {

  public NoSuchUserException(String message) {
    super(message);
  }
}
