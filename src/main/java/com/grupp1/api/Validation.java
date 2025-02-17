package com.grupp1.api;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.json.JSONException;
import org.json.JSONObject;

class Validation {

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
          //RFC 5322
          if (!fieldVal.matches(
              "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")) {
            throw new ValidationException("Invalid '" + field + "' format");
          }

        }
      } catch (JSONException e) {
        e.printStackTrace();
        throw new ValidationException("missing '" + field + "' field");
      }
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
