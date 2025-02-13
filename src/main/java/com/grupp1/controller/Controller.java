package com.grupp1.controller;

import com.grupp1.db.DB;
import com.grupp1.api.ServerException;
import java.sql.SQLException;

public class Controller {

  public static boolean register(String firstName,
      String lastName,
      String personalNumber,
      String email,
      String userPassword,
      String userName)
      throws ServerException {

    try {
      DB.createUser(firstName, lastName, personalNumber, email, userPassword, userName);
    } catch (SQLException e) {
      throw new ServerException(e.toString());
    }

    return true;

  }

}
