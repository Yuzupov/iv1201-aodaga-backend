package com.grupp1.api;

import java.nio.charset.StandardCharsets;
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

class Crypt {

  private static final String rsaPrivKey =
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

  static JSONObject decryptJson(JSONObject json) throws BadCryptException, ValidationException {
    String encryptedKey = json.getString("key");
    String crypt = json.getString("cipher");
    String iv = json.getString("iv");
    String key = decryptRSA(encryptedKey);
    String decryptedJsonString = decryptAES(crypt, iv, key);
    JSONObject decryptedJson = Json.parseJson(decryptedJsonString);
    decryptedJson.put("symmetricKey", key);
    return decryptedJson;
  }

  static JSONObject encryptJson(JSONObject json, String symmetricKey) throws BadCryptException {
    String cipher = encryptAES(json.toString(), symmetricKey);


  }

  public static String decryptRSA(String cipherText) {
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

      System.out.println("decrypted: " + plainText.length + new String(plainText));
      return new String(plainText);

    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch (NoSuchPaddingException e) {
      throw new RuntimeException(e);
    } catch (InvalidKeySpecException e) {
      throw new RuntimeException(e);
    } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Takes a byte[] and encrypts with a public key.
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

    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch (InvalidKeyException e) {
      throw new RuntimeException(e);
    } catch (InvalidKeySpecException e) {
      throw new RuntimeException(e);
    } catch (NoSuchPaddingException e) {
      throw new RuntimeException(e);
    } catch (IllegalBlockSizeException e) {
      throw new RuntimeException(e);
    } catch (BadPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  private static String encryptAES(String message, String keyString) throws BadCryptException {
    byte[] key = hexStringToByteArray(keyString);
    SecretKey sKey = new SecretKeySpec(key, "AES");
    byte[] ivBytes = new byte[16];
    new SecureRandom().nextBytes(ivBytes);
    IvParameterSpec iv = new IvParameterSpec(ivBytes);

    try {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, sKey, iv);
      byte[] cipherBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
      String cipherText = Base64.getEncoder().encodeToString(cipherBytes);
      return new String(cipherText);

    } catch (Exception e) {
      e.printStackTrace();
      throw new BadCryptException("Bad Crypt");
    }
  }

  //private record AESCrypt() {
//
  //}


  private static String decryptAES(String cipherText,
      String ivstring, String keyString) throws BadCryptException {

    //byte[] lol = hexStringToByteArray("000102030405060708090a0b0c0d0e0f");
    byte[] key = hexStringToByteArray(keyString);
    SecretKey sKey = new SecretKeySpec(key, "AES");

    byte[] ivbytes = hexStringToByteArray(ivstring);
    IvParameterSpec iv = new IvParameterSpec(ivbytes);
    //cipherText = "lol";

    try {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, sKey, iv);
      byte[] plainText = cipher.doFinal(Base64.getDecoder()
          .decode(cipherText));
      System.out.println("decrypted: " + new String(plainText));
      return new String(plainText);
    } catch (Exception e) {
      e.printStackTrace();
      throw new BadCryptException("Bad Crypt");
    }
  }

  private static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
          + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }
}
