package com.grupp1.controller;

import static com.grupp1.controller.PasswordHash.hashPassword;
import static com.grupp1.controller.PasswordHash.testPassword;

import com.grupp1.api.BadApiInputException;
import com.grupp1.db.DB;
import com.grupp1.api.ServerException;
import com.grupp1.db.NoSuchUserException;
import com.grupp1.db.UserExistsException;
import java.sql.SQLException;

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

}
