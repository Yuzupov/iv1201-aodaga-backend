package com.grupp1.db;

import java.sql.*;

public class DB {

  static String user = "aodaga";
  static String password = "";
  static String db = "aodaga";
  static String host = "localhost";
  static String port = "5432";

  private static Connection getConn() throws SQLException {
    String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;
    return DriverManager.getConnection(url, user, password);
  }

  public static void createUser(String name, String surname, String pnr, String email,
      String password,
      String username)
      throws SQLException {
    String query = "INSERT INTO person (name, surname, pnr, email, password, username, role_id) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    try {
      Connection conn = getConn();
      Statement lol = conn.createStatement();
      ResultSet rs = lol.executeQuery("SELECT role_id FROM role WHERE name = 'applicant'");
      rs.next();
      int role_id = rs.getInt("role_id");
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, name);
      stmt.setString(2, surname);
      stmt.setString(3, pnr);
      stmt.setString(4, email);
      stmt.setString(5, password);
      stmt.setString(6, username);
      stmt.setInt(7, role_id);
      System.out.println(query);
      stmt.execute();
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    }


  }
}
