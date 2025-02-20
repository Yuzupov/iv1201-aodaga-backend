package com.grupp1.api;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Crypt {

  private static final Logger log = LoggerFactory.getLogger(Crypt.class);

  private static final String rsaPrivKey;

  static {
    String key = System.getenv("RSA_PRIV_KEY");
    if (key.length() > 0) {
      rsaPrivKey = key;
    } else {
      rsaPrivKey =
          "-----BEGIN PRIVATE KEY-----\n"
              + "MIICeQIBADANBgkqhkiG9w0BAQEFAASCAmMwggJfAgEAAoGBALVtRzmA5aSxe1QR\n"
              + "Ico3IVpolewxmYehk0uFfET/YD4YZZz/XoQIO+twbJqOC6RzSIHbahcWKwqFhl3z\n"
              + "owp6+vgX0QeRyA4yVef0LhzsZoNsXTJ1p6mVMR432YboA3Ln5vw6TgdlpUl2uRhH\n"
              + "nKtAxlpfIh4ZN0vUeaJZUS3eRdwfAgMBAAECgYEAl99TiSqCkjxUPgpdW9aMoA7+\n"
              + "uYrHt8ck80pZvbR9j12C6krHhwURi8Q/1Z1k15P9tV7ET3EqNJOT6GAEMsjB/rWv\n"
              + "OmzRi2qBfcZF3qv9GWpDVONk8SFqoqLAspgxKJvl9dIj4gVH2yBjI2YtKUDFQ9m9\n"
              + "fDWPahuwc6cpkGaEBEkCQQDamIChPvXrT/kw3HXRuRj9zBOxkVtA6cVxInH3SQXY\n"
              + "zU0F6K4SceB34q24UccfoR7b/MTrReqAzSU4buhFd+IDAkEA1HicAzcLpmwXbTGq\n"
              + "TlSGWCCsugI11Eqc6+/emMAPKHH6PSpS21BW6+2mpFz1CmUF6Pfvj7RWMUaND6Zr\n"
              + "NQuwtQJBAKvK6GRQ59HsAwoMaKfO0T48kUme09mbHyl/iZNvFyJAjoTTTWJ/joqJ\n"
              + "Yj+WPWi1Jlx7NYM1akuZbeQA/ZgC1GMCQQCxpG18WqeI61Li3uVvPEheomMH2hU7\n"
              + "e26b7R+FQv7pZ/I69Yn1B8TE2Ru8zGOr3y8Dy1gmJDb0V/JUpWV5Il8JAkEAl2jy\n"
              + "doxaf4lymJq7N6e9wOWJCeafa/rppq7vtoddz+RvfFPyiH36Vpi5k5PBJdAYBsJe\n"
              + "e2+cS/dHkYPwTgZbKw==\n"
              + "-----END PRIVATE KEY-----";
    }
    //test key
    String message = "test";
    try {
      String crypt = encryptRsaPubKey(message.getBytes(StandardCharsets.UTF_8));
      String decryptedMessage = decryptRSA(crypt);
      if (!decryptedMessage.equals(message)) {
        throw new RuntimeException("Bad RSA key");
      }
    } catch (BadCryptException | RuntimeException e) {
      log.error("RSA_PRIV_KEY failed test.");
      throw new RuntimeException("Bad RSA key");
    }

  }

  /**
   * Decrypts an encrypted json object on the form {key:"..", cipher:"...", iv:"..."} where key is a
   * symmetric key encrypted using the public key, cipher and iv are encrypted with that symmetric
   * key. All encoded in base64.
   *
   * @param json the json object with key, cipher and iv
   * @return The json object that was encrypted and contained in the carrier json object
   * @throws BadCryptException
   * @throws ValidationException
   */
  static JSONObject decryptJson(JSONObject json) throws BadCryptException, ValidationException {
    Validation.validateEncrypted(json);
    String encryptedKey = json.getString("key");
    String crypt = json.getString("cipher");
    String iv = json.getString("iv");
    String key = decryptRSA(encryptedKey);
    String decryptedJsonString = decryptAES(crypt, iv, key);
    JSONObject decryptedJson = Json.parseJson(decryptedJsonString);
    decryptedJson.put("symmetricKey", key);
    return decryptedJson;
  }

  /**
   * Encrypts a json object into another json object conaining the data needed for decryption once
   * delivered.
   *
   * @param json         generic json object on any form
   * @param symmetricKey AES key for encryption
   * @return Json object {cipher: ****, iv: ****}, where cipher is the input json object stringified
   * and encrypted
   * @throws BadCryptException
   */
  static JSONObject encryptJson(JSONObject json, String symmetricKey) throws BadCryptException {

    System.out.println("testt");
    AESCrypt crypt = encryptAES(json.toString(), symmetricKey);
    JSONObject cryptJson = new JSONObject();
    cryptJson.put("cipher", crypt.cipher());
    cryptJson.put("iv", crypt.iv());
    return cryptJson;
  }

  /**
   * Decrypts an RSA cipher encrypted with the public key, using the global private key.
   *
   * @param cipherText a string containing a Base64 encoded cipher byte array. The encoded data must
   *                   be a valid string/.
   * @return
   * @throws BadCryptException
   */
  public static String decryptRSA(String cipherText) throws BadCryptException {
    try {
      String privateKeyPEM = rsaPrivKey
          .replace("-----BEGIN PRIVATE KEY-----", "")
          .replaceAll(System.lineSeparator(), "")
          .replace("-----END PRIVATE KEY-----", "");

      byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
      RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.DECRYPT_MODE, privateKey);

      byte[] plainText = cipher.doFinal(Base64.getDecoder()
          .decode(cipherText));

      return new String(plainText);

    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new RuntimeException(e);
    } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException |
             InvalidKeySpecException e) {
      log.info("RSA decryption failed: " + e.getMessage());
      throw new BadCryptException("Bad crypt: " + e.getMessage());
    }
  }

  /**
   * Takes a byte[] and encrypts with the public key.
   *
   * @param message
   * @return Base64 encrypted String
   */
  public static String encryptRsaPubKey(byte[] message) {
    try {
      String privateKeyPEM = rsaPrivKey
          .replace("-----BEGIN PRIVATE KEY-----", "")
          .replaceAll(System.lineSeparator(), "")
          .replace("-----END PRIVATE KEY-----", "");
      byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);

      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
      PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

      RSAPrivateCrtKey rsaPrivateKey = (RSAPrivateCrtKey) privateKey;
      RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
          rsaPrivateKey.getModulus(),
          rsaPrivateKey.getPublicExponent()
      );
      PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.ENCRYPT_MODE, publicKey);

      byte[] encrypted = cipher.doFinal(Base64.getEncoder().encode(message));
      return Base64.getEncoder().encodeToString(encrypted);

    } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException |
             NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * AES encrypts provided message with the provided key and a randomized IV
   *
   * @param message   the message to be encrypted
   * @param keyString the key, a base64 encoded byte array
   * @return an AESCrypt object with the cipher and iv
   * @throws BadCryptException
   */
  private static AESCrypt encryptAES(String message, String keyString) throws BadCryptException {
    byte[] key = Base64.getDecoder().decode(keyString);
    SecretKey sKey = new SecretKeySpec(key, "AES");
    byte[] ivBytes = new byte[16];
    new SecureRandom().nextBytes(ivBytes);
    IvParameterSpec iv = new IvParameterSpec(ivBytes);

    try {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, sKey, iv);
      byte[] cipherBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
      String cipherText = Base64.getEncoder().encodeToString(cipherBytes);
      String ivText = Base64.getEncoder().encodeToString(ivBytes);
      return new AESCrypt(cipherText, ivText);

    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new RuntimeException(e);
    } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException |
             BadPaddingException e) {
      log.info("AES encryption failed: " + e.getMessage());
      throw new BadCryptException("Bad crypt: " + e.getMessage());
    }
  }

  /**
   * data encrypted using the AES algorithm encoded in Base64;
   *
   * @param cipher Base64 encoded cipher
   * @param iv     Base64 encoded iv
   */
  private record AESCrypt(String cipher, String iv) {

  }


  /**
   * AES decrypts provided message with the provided key and iv
   *
   * @param cipherText Base64 encoded cipher to be decrypted, plaintext needs to be valid String
   * @param ivstring   Base64 encoded initalization vector
   * @param keyString  Base64 encoded Key
   * @return The decrypted plaintext
   * @throws BadCryptException if the supplied parameters are invalid
   */
  private static String decryptAES(String cipherText,
      String ivstring, String keyString) throws BadCryptException {

    byte[] key = Base64.getDecoder().decode(keyString);
    SecretKey sKey = new SecretKeySpec(key, "AES");

    byte[] ivbytes = Base64.getDecoder().decode(ivstring);
    IvParameterSpec iv = new IvParameterSpec(ivbytes);

    try {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, sKey, iv);
      byte[] plainText = cipher.doFinal(Base64.getDecoder()
          .decode(cipherText));
      //System.out.println("decrypted: " + new String(plainText));
      return new String(plainText);

    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new RuntimeException(e);
    } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException |
             InvalidAlgorithmParameterException e) {
      log.info("AES decryption failed: " + e.getMessage());
      throw new BadCryptException("Bad crypt: " + e.getMessage());
    }
  }
}
