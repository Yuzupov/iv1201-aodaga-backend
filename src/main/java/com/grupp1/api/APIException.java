package com.grupp1.api;

abstract class APIException extends Exception {

  public APIException(String message) {
    super(message);
  }
}
