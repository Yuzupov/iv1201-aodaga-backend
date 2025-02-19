package com.grupp1.db;

import com.grupp1.controller.ApplicantsDTO;
import com.grupp1.controller.UserDTO;
import java.sql.*;

public class DB {

  static String user = "aodaga";
  static String password = "";
  static String db = "aodaga";
  static String host = "jdbc:postgresql://localhost";
  static String port = "5432";

  private static Connection getConn() throws SQLException {
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    if (System.getenv("DATABASE_URL") != null) {

      return DriverManager.getConnection(System.getenv("JDBC_DATABASE_URL"));

    } else {
      String url = "" + host + ":" + port + "/" + db;
      return DriverManager.getConnection(url, user, password);
    }
  }

  public static UserDTO getUserByUsernameOrEmail(String username, String email)
      throws SQLException, NoSuchUserException {
    if (username == null) {
      username = "";
    }
    if (email == null) {
      email = "";
    }
    String query = "SELECT p.name, p.surname, p.email, p.username, p.password, r.name AS role FROM person p JOIN role r ON p.role_id = r.role_id WHERE p.username = ? OR p.email = ?";
    Connection conn = getConn();
    try {
      conn.setAutoCommit(false);
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, username);
      stmt.setString(2, email);

      ResultSet res = stmt.executeQuery();

      System.out.println(res.getFetchSize());

      if (!res.next()) {
        conn.rollback();
        throw new NoSuchUserException("No such user");
      }
      UserDTO user = new UserDTO(
          res.getString("name"),
          res.getString("surname"),
          res.getString("email"),
          res.getString("username"),
          res.getString("password"),
          res.getString("role"));

      conn.commit();

      return user;

    } catch (SQLException e) {
      conn.rollback();
      e.printStackTrace();
      throw e;
    }

  }

  public static void createUser(String name, String surname, String pnr, String email,
      String password,
      String username)
      throws SQLException, UserExistsException {

    Connection conn = getConn();

    try {
      conn.setAutoCommit(false);

      PreparedStatement checkExists = conn.prepareStatement(
          "SELECT person_id FROM person WHERE username = ? OR email = ?");
      checkExists.setString(1, username);
      checkExists.setString(2, email);
      ResultSet rrs = checkExists.executeQuery();
      if (rrs.next() && rrs.getInt(1) > 0) {
        conn.rollback();
        throw new UserExistsException("username or email already exists");
      }
      Statement lol = conn.createStatement();
      ResultSet rs = lol.executeQuery("SELECT role_id FROM role WHERE name = 'applicant'");
      rs.next();
      int role_id = rs.getInt("role_id");

      String query =
          "INSERT INTO person (name, surname, pnr, email, password, username, role_id) " +
              "VALUES (?, ?, ?, ?, ?, ?, ?)";
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, name);
      stmt.setString(2, surname);
      stmt.setString(3, pnr);
      stmt.setString(4, email);
      stmt.setString(5, password);
      stmt.setString(6, username);
      stmt.setInt(7, role_id);
      //System.out.println(stmt);
      stmt.execute();

      conn.commit();
      conn.close();
    } catch (SQLException e) {
      conn.rollback();
      e.printStackTrace();
      throw e;
    }


  }

  public static ApplicantsDTO listAll() {
    return null;
  }
}
