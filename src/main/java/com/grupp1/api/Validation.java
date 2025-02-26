package com.grupp1.api;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Validation {

  private static final Logger log = LoggerFactory.getLogger(Validation.class);


  /**
   * Checks wether a json object conforms to the requirements for the login endpoint
   *
   * @param json the Json object
   * @throws ValidationException if the requirements are not met
   */
  static void validateLogin(JSONObject json) throws ValidationException {
    String field = "";
    String username;
    String email;
    try {
      boolean hasEmail = false;
      boolean hasUsername = false;
      field = "username";
      if (json.has(field)) {
        username = json.getString(field);
        validateUsername(username, field);
        hasUsername = true;
      }
      field = "userEmail";
      if (json.has(field)) {
        email = json.getString(field);
        validateEmail(email, field);
        hasUsername = true;
      }
      if (!hasUsername && !hasEmail) {
        throw new ValidationException("missing 'username' or 'userEmail' field");
      }
      field = "userPassword";
      json.getString(field);

    } catch (JSONException e) {
      e.printStackTrace();
      throw new ValidationException("bad or missing '" + field + "' field");
    }
  }

  /**
   * Checks wether a json object conforms to the requirements for the register endpoint
   *
   * @param json the Json object
   * @throws ValidationException if the requirements are not met
   */
  static void validateRegister(JSONObject json) throws ValidationException {

    String[] expectedFields = {
        "firstName",
        "lastName",
        "personalNumber",
        "email",
        "userPassword",
        "username"};

    for (String field : expectedFields) {
      try {
        if (json.get(field).getClass() != String.class) {
          throw new ValidationException("bad '" + field + "' parameter (should be string)");
        }
        String fieldVal = json.getString(field);
        if (fieldVal.length() > 255) {
          throw new ValidationException("'" + field + "' too long");
        }
        //TODO
        if (field.equals("personalNumber")) {
          validatePersonalNumber(fieldVal, field);
        }
        if (field.equals("email")) {
          validateEmail(fieldVal, field);
        }
        if (field.equals("userName")) {
          validateUsername(fieldVal, field);
        }
      } catch (JSONException e) {
        e.printStackTrace();
        throw new ValidationException("missing '" + field + "' field");
      }
    }
  }

  /**
   * Takes a Json object and validates for existing token-field.
   *
   * @param json
   * @throws ValidationException
   */
  static void validateApplicants(JSONObject json) throws ValidationException {
    try {
      json.getString("token");
    } catch (JSONException e) {
      e.printStackTrace();
      throw new ValidationException("missing 'token' field");
    }
  }


  /**
   * Checks wether a supplied key is a valid AES key for our purposes
   *
   * @param key Base64 encoded AES key
   * @throws ValidationException if the key is not valid
   */
  static void validateAESKey(String key) throws ValidationException {
    try {
      byte[] keyBytes = Base64.getDecoder().decode(key);

      if (keyBytes.length != 32) {
        log.info("Validation not passed: key is " + keyBytes.length + " bytes long, must be 32.");
        throw new ValidationException("invalid key");
      }
    } catch (IllegalArgumentException e) {
      log.info("Validation not passed: decoded key invalid Base64: " + e.getMessage());
      throw new ValidationException("'" + key + "' not valid");
    }

  }

  /**
   * Checks wether a json object conforms to the requirements for an encrypted endpoint input json
   *
   * @param json
   * @throws ValidationException if the Json object does not conform
   */
  static void validateEncrypted(JSONObject json) throws ValidationException {
    String[] expectedFields = {
        "cipher",
        "iv",
        "key",
        "timestamp"};
    for (String field : expectedFields) {
      try {
        String fieldVal = json.getString(field);
        if (fieldVal.length() == 0) {
          log.info(
              "Validation not passed: '" + field + "' is empty");
          throw new ValidationException("Bad field: '" + field + "'");
        }
        if (!field.equals("timestamp")) {
          byte[] fieldBytes = Base64.getDecoder().decode(fieldVal);
          if (field.equals("iv")) {
            if (fieldBytes.length != 16) {
              log.info(
                  "Validation not passed: iv is " + fieldBytes.length + " bytes long, must be 16.");
              throw new ValidationException("bad iv length");
            }
          }
          if (field.equals("key")) {
            if (fieldBytes.length != 128) {
              log.info(
                  "Validation not passed: encrypted key is " + fieldBytes.length
                      + " bytes long, must be 128.");
              throw new ValidationException("bad key crypt length");
            }
          }
        }
      } catch (JSONException e) {
        log.info("Validation not passed: " + e.getMessage());
        throw new ValidationException("bad or missing '" + field + "' field");
      } catch (IllegalArgumentException e) {
        log.info("Validation not passed: '" + field + "' invalid Base64: " + e.getMessage());
        throw new ValidationException("'" + field + "' not valid Base64");
      }
    }
  }

  /**
   * Takes in byte[] Token Checks that token can parse into a JSON object Checks that said object
   * contains the correct fields Check that token is not expired throws exceptions in case something
   * is wrong.
   *
   * @param token Base64 encoded token
   * @throws ValidationException
   */
  static void validateToken(String token) throws ValidationException {
    JSONObject json;
    String field = "";
    try {
      byte[] tokenBytes = Base64.getDecoder().decode(token);
      String jsonString = new String(tokenBytes, StandardCharsets.UTF_8);
      json = new JSONObject(jsonString);
    } catch (JSONException | IllegalArgumentException e) {
      log.info("Not a valid token");
      throw new ValidationException("Token not valid" + e);
    }

    try {
      field = "username";
      json.getString(field);
      field = "expiration";
      long expirationTime = json.getLong(field);

      if (!(expirationTime > Instant.now().getEpochSecond())) {
        log.info("Token is expired");
        throw new ValidationException("Token is Expired");
      }
    } catch (JSONException e) {
      log.info("missing " + field + " field");
      throw new ValidationException("missing " + field + " field");
    }
  }


  /**
   * Checks wether a json object conforms to the requirements for the paswordReset/validatelink
   * endpoint
   *
   * @param json the Json object
   * @throws ValidationException if the requirements are not met
   */
  public static void validatePasswordResetValidateLink(JSONObject json) throws ValidationException {
    String field = "link";
    try {
      String fieldVal = json.getString(field);
      validateResetLink(fieldVal, field);
    } catch (JSONException e) {
      log.info("Validation not passed: Missing '" + field + "'");
      throw new ValidationException("missing " + field + "field");
    }
  }


  /**
   * Checks wether a json object conforms to the requirements for the resetPassword/createlink
   * endpoint
   *
   * @param json the Json object
   * @throws ValidationException if the requirements are not met
   */
  public static void validatePasswordResetCreatelink(JSONObject json) throws ValidationException {
    String field = "email";
    try {
      String fieldVal = json.getString(field);
      validateEmail(fieldVal, field);
    } catch (JSONException e) {
      log.info("Validation not passed: Missing '" + field + "'");
      throw new ValidationException("missing " + field + "field");
    }
  }


  /**
   * Checks wether a json object conforms to the requirements for the resetPassword endpoint
   *
   * @param json the Json object
   * @throws ValidationException if the requirements are not met
   */
  public static void validatePasswordReset(JSONObject json) throws ValidationException {
    String[] expectedFields = {"link", "password"};
    String field = "";
    try {
      for (String f : expectedFields) {
        field = f;
        String fieldVal = json.getString(field);

        if (field.equals("link")) {
          validateResetLink(fieldVal, field);
        } else if (field.equals("password")) {
          validateString(fieldVal, field);
        }
      }
    } catch (JSONException e) {
      log.info("Validation not passed: Missing '" + field + "'");
      throw new ValidationException("missing " + field + "field");
    }
  }

  public static void validateUpdate(JSONObject json) throws ValidationException {
    String field = "";
    try {
      field = "token";
      json.getString(field);
      field = "password";
      String password = json.getString(field);
      validateString(password, field);
    } catch (JSONException e) {
      log.info("missing field: " + field);
      throw new ValidationException("missing field: " + field);
    }
  }

  private static void validateString(String string, String fieldName)
      throws ValidationException {
    if (string == null || string.isEmpty()) {
      log.info("Validation not passed: Invalid '" + fieldName + "' format");
      throw new ValidationException("Invalid '" + fieldName + "' format");
    }
  }

  private static void validateResetLink(String resetLink, String fieldName)
      throws ValidationException {
    try {
      com.grupp1.utils.Validation.validateResetLink(resetLink);
    } catch (IllegalArgumentException e) {
      log.info("Validation not passed: Invalid '" + fieldName + "' format");
      throw new ValidationException("Invalid '" + fieldName + "' format");
    }
  }

  private static void validateEmail(String username, String fieldName) throws ValidationException {
    try {
      com.grupp1.utils.Validation.validateEmail(username);
    } catch (IllegalArgumentException e) {
      log.info("Validation not passed: Invalid '" + fieldName + "' format");
      throw new ValidationException("Invalid '" + fieldName + "' format");
    }
  }

  private static void validateUsername(String username, String fieldName)
      throws ValidationException {
    try {
      com.grupp1.utils.Validation.validateUsername(username);
    } catch (IllegalArgumentException e) {
      log.info("Validation not passed: Invalid '" + fieldName + "' format");
      throw new ValidationException("Invalid '" + fieldName + "' format");
    }
  }

  private static void validatePersonalNumber(String personalNumber, String fieldName)
      throws ValidationException {
    try {
      com.grupp1.utils.Validation.validatePersonalNumber(personalNumber);
    } catch (IllegalArgumentException e) {
      log.info("Validation not passed: Invalid '" + fieldName + "' format");
      throw new ValidationException("Invalid '" + fieldName + "' format");
    }
  }
}
