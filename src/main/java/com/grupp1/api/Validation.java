package com.grupp1.api;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;

class Validation {

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
   * Checks wether a json o
   *
   * @param json
   * @throws ValidationException
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

  static void validateEncrypted(JSONObject json) throws ValidationException {

    String[] expectedFields = {
        "cipher",
        "iv",
        "key"};
    for (String field : expectedFields) {
      try {
        String fieldVal = json.getString(field);
      } catch (JSONException e) {
        e.printStackTrace();
        throw new ValidationException("missing '" + field + "' field");
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
  static void validateToken(String token) throws ValidationException {
    JSONObject json;
    String field = "";
    try {
      byte[] tokenBytes = Base64.getDecoder().decode(token);
      String jsonString = new String(tokenBytes, StandardCharsets.UTF_8);
      json = new JSONObject(jsonString);
    } catch (JSONException e) {
      e.printStackTrace();
      throw new ValidationException("Token not valid" + e);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      throw new ValidationException("token is wrong of wrong type");
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
