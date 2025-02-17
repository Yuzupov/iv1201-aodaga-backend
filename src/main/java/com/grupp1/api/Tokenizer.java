package com.grupp1.api;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.json.JSONObject;


class Tokenizer {

  static class TokenData {

    String token;
    String username;
    Long expirationDate;

    TokenData(String token, String uName, long time) {
      this.token = token;
      this.username = uName;
      this.expirationDate = time;
    }
  }

  /**
   * Takes in a username and output an object, TokenData. TokenData contains a String username, long
   * expiration date and byte[] token which is a byte representation of a stringified JSON object
   * containing the username and expiration date.
   *
   * @param username
   * @return TokenData object
   */
  TokenData createToken(String username) {
    long expirationTime = Instant.now().getEpochSecond() + 86500;
    JSONObject json = new JSONObject();
    json.put("username", username);
    json.put("expiration", expirationTime);
    String token = Crypt.encryptRsaPubKey(json.toString().getBytes(StandardCharsets.UTF_8));
    return new TokenData(token, username, expirationTime);
  }

  /**
   * Extracts the username from the byte[] token and returns a string
   *
   * @param token
   * @return username
   */
  String extractUsername(byte[] token) {
    String jsonString = new String(token, StandardCharsets.UTF_8);
    JSONObject json = new JSONObject(jsonString);
    return json.getString("username");
  }
}
