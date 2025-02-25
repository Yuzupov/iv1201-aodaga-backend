package com.grupp1.utils;

import com.grupp1.api.ValidationException;

public class Validation {

  /**
   * Validate username. silent if username conforms to the format, otherwise throws an error.
   *
   * @param username the username to validate
   * @throws IllegalArgumentException if validation fails
   */
  public static void validateUsername(String username)
      throws IllegalArgumentException {
    //if (!username.matches("^[a-zA-Z0-9åäöÅÄÖ]*$")) {
    if (!username.matches("^[\\p{L}\\d]*$")) {
      throw new IllegalArgumentException("invalid username");
    }
    if (username.length() > 80) {
      throw new IllegalArgumentException("invalid username");
    }
  }


  /**
   * Validate email. silent if email conforms to the format, otherwise throws an error.
   *
   * @param email the username to validate
   * @throws IllegalArgumentException if validation fails
   */
  public static void validateEmail(String email) throws IllegalArgumentException {
    if (!email.matches(
        "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
    )) {
      throw new IllegalArgumentException("invalid email");
    }
    if (email.length() > 255) {
      throw new IllegalArgumentException("invalid email");
    }
  }


  /**
   * Validate personalNumber. silent if email conforms to the format, otherwise throws an error.
   *
   * @param personalNumber the username to validate
   * @throws IllegalArgumentException if validation fails
   */
  public static void validatePersonalNumber(String personalNumber) {
    if (!personalNumber.matches("\\d{8}-\\d{4}")) {
      throw new IllegalArgumentException("Invalid personalNumber");
    }
  }
}
