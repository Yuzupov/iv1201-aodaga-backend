package com.grupp1.controller;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

public class PasswordHash {

  private static final int t = 2;
  private static final int m = 19456;
  private static final int h = 32;
  private static final int p = 1;

  /**
   * Creates a password hash from a supplied password. Uses the Argon2id algorithm.
   *
   * @param password in plaintext
   * @return password hash on $salt$password form, each base64 encoded
   */
  public static String hashPassword(String password) {
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);
    return hashPassword(password, salt);
  }

  /**
   * Creates a password hash from a supplied password. Uses the Argon2id algorithm.
   *
   * @param password in plaintext
   * @param salt     the salt as a byte array
   * @return password hash on $salt$password form, each base64 encoded
   */
  public static String hashPassword(String password, byte[] salt) {
    Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
        .withVersion(Argon2Parameters.ARGON2_VERSION_13)
        .withIterations(t)
        .withMemoryAsKB(m)
        .withParallelism(p)
        .withSalt(salt);
    Argon2BytesGenerator generate = new Argon2BytesGenerator();
    generate.init(builder.build());

    byte[] hashBytes = new byte[h];
    generate.generateBytes(password.getBytes(StandardCharsets.UTF_8), hashBytes, 0, h);

    String hash = Base64.getEncoder().encodeToString(hashBytes);
    String saltString = Base64.getEncoder().encodeToString(salt);
    String storedHash = "$" + saltString + "$" + hash;
    return storedHash;
  }

  /**
   * Validates a password to a given hash.
   *
   * @param password
   * @param storedSaltedHash
   * @return true if the password corresonds to the hash, otherwise false.
   */
  public static void testPassword(String password, String storedSaltedHash)
      throws PasswordException {
    String[] splitSaltedHash = storedSaltedHash.split("\\$");
    byte[] salt = Base64.getDecoder().decode(splitSaltedHash[1]);
    String storedHash = splitSaltedHash[2];
    String passwordHash = hashPassword(password, salt);
    if (!passwordHash.equals(storedSaltedHash)) {
      throw new PasswordException("Wrong Password");
    }
  }
}
