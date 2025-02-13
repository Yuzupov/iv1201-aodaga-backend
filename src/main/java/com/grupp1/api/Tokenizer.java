package com.grupp1.api;

import java.io.Serializable;
import java.time.Instant;
import org.apache.commons.lang3.SerializationUtils;


public class Tokenizer {
  public class TokenDTO implements Serializable {
    String username;
    Long expirationDate;
    TokenDTO(String username, Long expirationDate){
      this.username = username;
      this.expirationDate = expirationDate;
    }
  }
  byte[] createToken(String key, String username){
    TokenDTO tokenDTO = new TokenDTO(username, Instant.now().getEpochSecond()+86500);
    byte[] token = SerializationUtils.serialize(tokenDTO);

    TokenDTO deser = SerializationUtils.deserialize(token);
    System.out.println(deser.username);
    return token;
  }
}
