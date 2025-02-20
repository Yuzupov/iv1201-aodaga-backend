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

  private static void validateUsername(String username, String fieldName)
      throws ValidationException {
    if (!username.matches("^[a-zA-Z0-9]*$")) {
      throw new ValidationException("Invalid '" + fieldName + "' format");
    }
    if (username.length() > 80) {
      throw new ValidationException("'" + fieldName + "' too long");
    }
  }

  private static void validateEmail(String email, String fieldName) throws ValidationException {
    if (!email.matches(
        "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
    )) {
      throw new ValidationException("Invalid '" + fieldName + "' format");
    }
  }

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
        if (field.equals("personalNumber")) {
          if (!fieldVal.matches("\\d{8}-\\d{4}")) {
            throw new ValidationException("Invalid 'personalNumber' format");
          }
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
        "key"};
    for (String field : expectedFields) {
      try {
        String fieldVal = json.getString(field);
        byte[] fieldBytes = Base64.getDecoder().decode(fieldVal);
        if (field == "iv") {
          if (fieldBytes.length != 16) {
            log.info(
                "Validation not passed: iv is " + fieldBytes.length + " bytes long, must be 16.");
            throw new ValidationException("bad iv length");
          }
        }
        if (field == "key") {
          if (fieldBytes.length != 128) {
            log.info(
                "Validation not passed: encrypted key is " + fieldBytes.length
                    + " bytes long, must be 128.");
            throw new ValidationException("bad key crypt length");
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
   * @param token
   * @throws ValidationException
   */
  static void validateToken(byte[] token) throws ValidationException {
    JSONObject json = null;
    String field = "";
    try {
      String jsonString = new String(token, StandardCharsets.UTF_8);
      json = new JSONObject(jsonString);
    } catch (JSONException e) {
      e.printStackTrace();
      throw new ValidationException("Token not valid" + e);
    }
    try {
      field = "username";
      json.getString(field);
      field = "expiration";
      long expirationTime = json.getLong(field);

      if (!(expirationTime > Instant.now().getEpochSecond())) {
        throw new ValidationException("Token is Expired");
      }
    } catch (JSONException e) {
      e.printStackTrace();
      throw new ValidationException("missing " + field + "field");
    }
  }
}
