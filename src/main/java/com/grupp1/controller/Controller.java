package com.grupp1.controller;

import static com.grupp1.controller.PasswordHash.hashPassword;
import static com.grupp1.controller.PasswordHash.testPassword;

import com.grupp1.api.BadApiInputException;
import com.grupp1.db.DB;
import com.grupp1.api.ServerException;
import com.grupp1.db.NoSuchUserException;
import com.grupp1.db.UserExistsException;
import java.sql.SQLException;
import java.util.Objects;

public class Controller {

  public static UserDTO login(String username, String email, String password)
      throws PasswordException, ServerException, NoSuchUserException {

    try {

      UserDTO user = DB.getUserByUsernameOrEmail(username, email);

      testPassword(password, user.password());
      return user;

    } catch (SQLException e) {
      throw new ServerException(e.toString());
    }
  }

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
    } catch (SQLException e) {
      throw new ServerException(e.toString());
    } catch (UserExistsException e) {
      throw new BadApiInputException(e.getMessage());
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
  public static ApplicantsDTO applicants(String username) throws IllegalRoleException {
    try {
      UserDTO user = DB.getUserByUsernameOrEmail(username, "");
      if (!Objects.equals(user.role(), "recruiter")) {
        throw new IllegalRoleException(
            "Invalid Role '" + user.role() + " for the requested Action");
      }
      return DB.applicants();

    } catch (SQLException e) {
      throw new RuntimeException(e);
    } catch (NoSuchUserException e) {
      throw new RuntimeException(e);
    }
  }
}
