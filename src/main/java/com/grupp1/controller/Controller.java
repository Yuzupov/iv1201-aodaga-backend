package com.grupp1.controller;

import static com.grupp1.controller.PasswordHash.hashPassword;
import static com.grupp1.controller.PasswordHash.testPassword;

import com.grupp1.api.BadApiInputException;
import com.grupp1.db.DB;
import com.grupp1.api.ServerException;
import com.grupp1.db.DBException;
import com.grupp1.db.NoSuchUserException;
import com.grupp1.db.UserExistsException;
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
}
