package com.grupp1.db;

import com.grupp1.controller.Controller;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Validation {

  static final Logger log = LoggerFactory.getLogger(Controller.class);

  /**
   * Validation for input to the getUserByUsenameOrEmail function in DB throws error if validation
   * fails
   *
   * @param username
   * @param email
   * @throws DBValidationException if validation fails
   */
  static void validateGetUserByUsernameOrEmail(String username, String email)
      throws DBValidationException {
    if (username == null || username.isEmpty()) {
      validateEmail(email);
    } else {
      validateUsername(username);
    }
  }

  /**
   * Validation for input to the createUser function in DB throws error if validation fails
   *
   * @param name
   * @param surname
   * @param pnr
   * @param email
   * @param passwordHash
   * @param username
   * @throws DBValidationException if validation fails
   */
  static void validateCreateUser(String name, String surname, String pnr, String email,
      String passwordHash,
      String username) throws DBValidationException {
    validateEmail(email);
    validateUsername(username);
    validatePersonalNumber(pnr);
    validatePasswordHash(passwordHash);
  }

  /**
   * Validation for input to the applicants function in DB. throws error if validation fails
   */
  static void validateApplicants() {
  }

  /**
   * Validation for input to the passwordResetlinkExpirationdate function in DB. throws error if
   * validation fails
   *
   * @param resetLink
   */
  static void validateGetPasswordResetlinkExpirationdate(String resetLink) {
    validateResetLink(resetLink);
  }

  /**
   * Validation for input to the createPasswordResetLink function in DB. throws error if validation
   * fails
   *
   * @param email
   * @param resetlink
   * @param timestamp
   */
  static void validateCreatePasswordResetlink(String email, String resetlink,
      Long timestamp) {
    validateEmail(email);
    validateResetLink(resetlink);
    if (Instant.now().isAfter(Instant.ofEpochMilli(timestamp))) {
      log.error("Timestamp is already outdated");
      throw new DBValidationException("Timestamp is already outdated");
    }

  }

  /**
   * Validation for input to the setUserPasswordByResetLink function in DB. throws error if
   * validation fails
   *
   * @param resetLink
   * @param passwordHash
   */
  static void validateSetUserPasswordByResetlink(String resetLink, String passwordHash) {
    validateResetLink(resetLink);
    validatePasswordHash(passwordHash);
  }

//private functions

  private static void validatePasswordHash(String passwordHash) throws DBValidationException {
    if (!passwordHash.matches("^\\$[\\p{L}\\d\\/+]{22}==\\$[\\p{L}\\d\\/+]{43}=$")) {
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

  private static void validateResetLink(String resetLink)
      throws DBValidationException {
    try {
      com.grupp1.utils.Validation.validateResetLink(resetLink);
    } catch (IllegalArgumentException e) {
      log.error(
          "Validation not passed: Invalid resetLink format.");
      throw new DBValidationException("Invalid resetLink format");
    }
  }
}
