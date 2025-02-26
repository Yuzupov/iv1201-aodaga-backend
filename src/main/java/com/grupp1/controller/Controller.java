package com.grupp1.controller;

import static com.grupp1.controller.PasswordHash.hashPassword;
import static com.grupp1.controller.PasswordHash.testPassword;

import com.grupp1.api.BadApiInputException;
import com.grupp1.db.DB;
import com.grupp1.api.ServerException;
import com.grupp1.db.DBException;
import com.grupp1.db.NoSuchUserException;
import com.grupp1.db.UserExistsException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Controller {

  static final Logger log = LoggerFactory.getLogger(Controller.class);

  /**
   * Log in user, checks wether supplied credentials are correct and returns a user object if they
   * are
   *
   * @param username
   * @param email
   * @param password
   * @return
   * @throws PasswordException   if incorrect password was supplied
   * @throws NoSuchUserException if no such user exists
   * @throws ServerException     if something unexpected went wrong
   */
  public static UserDTO login(String username, String email, String password)
      throws PasswordException, ServerException, NoSuchUserException {

    try {

      UserDTO user = DB.getUserByUsernameOrEmail(username, email);

      testPassword(password, user.password());
      return user;

    } catch (DBException e) {
      throw new ServerException("database error");
    }
  }

  public static void resetPasswordWithLink(String resetLink, String password)
      throws BadApiInputException, ServerException, NoSuchUserException {
    String passwordHash = PasswordHash.hashPassword(password);
    validatePasswordResetLink(resetLink);
    try {
      DB.setUserPasswordByResetLink(resetLink, passwordHash);
    } catch (DBException e) {
      throw new ServerException("database error");
    }

  }

  public static void validatePasswordResetLink(String resetLink)
      throws NoSuchUserException, ServerException, BadApiInputException {
    long timestamp;
    try {
      timestamp = DB.getPasswordResetLinkExpiratonTime(resetLink);
    } catch (DBException e) {
      throw new ServerException("database error");
    }

    Instant linkExpirationTime = Instant.ofEpochMilli(timestamp);
    if (Instant.now().isAfter(linkExpirationTime)) {
      log.info("Provided password reset link is expired");
      throw new BadApiInputException("Link has expired");
    }
    return;
  }

  /**
   * create a password reset link and email the user {dummy, prints to out}
   *
   * @param email
   * @throws ServerException
   */

  public static void createPasswordResetLink(String email) throws ServerException {
    byte[] rndBytes = new byte[32];
    try {
      SecureRandom.getInstanceStrong().nextBytes(rndBytes);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    StringBuilder rndLink = new StringBuilder();
    String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    for (byte b : rndBytes) {
      rndLink.append(charset.charAt((b + 128) % charset.length()));
    }
    String resetlink = rndLink.toString();
    Long timestamp = Instant.now().plusSeconds(600).getEpochSecond() * 1000;

    try {
      DB.createPasswordResetLink(email, resetlink, timestamp);

      System.out.println("This is an email");
      System.out.println(
          "https://recruitment-application-fronte-593587373fd5.herokuapp.com/reset/" + resetlink);
      System.out.println("Sincerely, aodagaAdmin");

    } catch (DBException e) {
      throw new ServerException("database error");
    }
  }


  /**
   * Create user using supplied Data
   *
   * @param firstName
   * @param lastName
   * @param personalNumber
   * @param email
   * @param userPassword
   * @param userName
   * @return
   * @throws BadApiInputException if supplied arguments are invalid
   * @throws ServerException      if something unexpected went wrong
   */
  public static boolean register(String firstName,
      String lastName,
      String personalNumber,
      String email,
      String userPassword,
      String userName)
      throws ServerException, BadApiInputException {

    String passwordHash = hashPassword(userPassword);

    try {
      DB.createUser(firstName, lastName, personalNumber, email, passwordHash, userName);
    } catch (UserExistsException e) {
      throw new BadApiInputException(e.getMessage());
    } catch (DBException e) {
      throw new ServerException("Database Error");
    }

    return true;

  }

  /**
   * Will request User data and check if user is in fact allowed to make the intended request.
   *
   * @param username
   * @return ApplicantsDTO, a read only record of all applicants.
   * @throws IllegalRoleException
   */
  public static List<ApplicantDTO> applicants(String username)
      throws IllegalRoleException, ServerException, NoSuchUserException {
    try {
      UserDTO user = DB.getUserByUsernameOrEmail(username, "");
      if (!user.role().equals("recruiter")) {
        log.info("Invalid Role '" + user.role() + " for the requested Action");
        throw new IllegalRoleException(
            "Invalid Role '" + user.role() + " for the requested Action");
      }
      return DB.applicants();

    } catch (DBException e) {
      throw new ServerException("Database Error");
    }
  }

  /**
   * Dummy endpoint method
   *
   * @param username
   * @param password
   * @throws PasswordException
   * @throws NoSuchUserException
   * @throws ServerException
   */
  public static void update(String username, String password)
      throws PasswordException, NoSuchUserException, ServerException {
    try {
      UserDTO user = DB.getUserByUsernameOrEmail(username, "");
      PasswordHash.testPassword(password, user.password());
    } catch (DBException e) {
      log.error("Database Error: " + e);
      throw new ServerException("Database Error");
    }
  }
}
