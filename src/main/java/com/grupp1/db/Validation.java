package com.grupp1.db;

import com.grupp1.controller.Controller;
import com.grupp1.controller.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Validation {

  static final Logger log = LoggerFactory.getLogger(Controller.class);

  static void validateGetUserByUsernameOrEmail(String username, String email) {
    validateUsername(username);
    validateEmail(email);
  }

  static void validateCreateUser(String name, String surname, String pnr, String email,
      String passwordHash,
      String username) {
    validateEmail(email);
    validateUsername(username);
    validatePersonalNumber(pnr);
    validatePasswordHash(passwordHash);
  }

  private static void validatePasswordHash(String passwordHash) {
    if (!passwordHash.matches("^\\$[\\p{L}\\d\\/]{22}==\\$[\\p{L}\\d\\/]{43}=$")) {
      log.error("Validation not passed: Invalid password hash format.");
      throw new DBValidationException(
          "Validation not passed: Invalid password hash format");

    }
  }

  private static void validateEmail(String email)
      throws DBValidationException {
    try {
      com.grupp1.utils.Validation.validateEmail(email);
    } catch (IllegalArgumentException e) {
      log.error("Validation not passed: Invalid Email format. this should be caught earlier");
      throw new DBValidationException(
          "Validation not passed: Invalid Email format");
    }
  }

  private static void validateUsername(String username)
      throws DBValidationException {
    try {
      com.grupp1.utils.Validation.validateUsername(username);
    } catch (IllegalArgumentException e) {
      log.error("Validation not passed: Invalid username format. this should be caught earlier");
      throw new DBValidationException("Invalid username format");
    }
  }

  private static void validatePersonalNumber(String personalNumber)
      throws DBValidationException {
    try {
      com.grupp1.utils.Validation.validatePersonalNumber(personalNumber);
    } catch (IllegalArgumentException e) {
      log.error(
          "Validation not passed: Invalid personalNumber format. this should be caught earlier");
      throw new DBValidationException("Invalid personalNumber format");
    }
  }


}
