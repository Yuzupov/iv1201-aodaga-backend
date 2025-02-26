package com.grupp1.api;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.json.JSONObject;


class Tokenizer {

  record TokenData(String token, String username, Long expirationDate) {

  }

  /**
   * Takes in a username and output an object, TokenData. TokenData contains a String username, long
   * expiration date and byte[] token which is a byte representation of a stringified JSON object
   * containing the username and expiration date.
   *
   * @param username
   * @return TokenData object
   */
  static TokenData createToken(String username) {
    long expirationTime = Instant.now().getEpochSecond() + 86500;
    JSONObject json = new JSONObject();
    json.put("username", username);
    json.put("expiration", expirationTime);
    String token = Crypt.encryptRsaPubKey(json.toString().getBytes(StandardCharsets.UTF_8));
    return new TokenData(token, username, expirationTime);
  }

  /**
   * Takes in a token and recreates the TokenData object from the information it contains.
   *
   * @param token
   * @return TokenData based on a valid token
   * @throws ValidationException
   */
  static TokenData extractToken(String token) throws ValidationException, BadCryptException {
    String decryptedToken = Crypt.decryptRSA(token);
    Validation.validateToken(decryptedToken);
    byte[] tokenBytes = Base64.getDecoder().decode(decryptedToken);
    String jsonString = new String(tokenBytes, StandardCharsets.UTF_8);
    JSONObject json = new JSONObject(jsonString);
    return new TokenData(token, json.getString("username"), json.getLong("expiration"));
  }
}
