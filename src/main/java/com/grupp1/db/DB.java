package com.grupp1.db;

import com.grupp1.controller.ApplicantDTO;
import com.grupp1.controller.Availability;
import com.grupp1.controller.UserDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DB {

  static final Logger log = LoggerFactory.getLogger(DB.class);

  static String user = "aodaga";
  static String password = "";
  static String db = "aodaga";
  static String host = "jdbc:postgresql://localhost";
  static String port = "5432";

  private static Connection getConn() throws DBException {
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    try {
      if (System.getenv("DATABASE_URL") != null) {

        return DriverManager.getConnection(System.getenv("JDBC_DATABASE_URL"));

      } else {
        String url = "" + host + ":" + port + "/" + db;
        return DriverManager.getConnection(url, user, password);
      }
    } catch (SQLException e) {
      log.error("SQLException: " + e.getMessage());
      throw new DBException(e);
    }
  }

  /**
   * Fetches a 'person' from the database using username or email. if both are supplied, username
   * takes precedence.
   *
   * @param username
   * @param email
   * @return a UserDTO with the information from the DB
   * @throws NoSuchUserException if no such user is found in the DB
   * @throws DBException         if DB throws an error
   */
  public static UserDTO getUserByUsernameOrEmail(String username, String email)
      throws NoSuchUserException, DBException {
    Validation.validateGetUserByUsernameOrEmail(username, email);
    if (username == null || username.isEmpty()) {
      username = "";
    } else {
      email = "";
    }
    if (username.length() + email.length() == 0) {
      log.info("No such user, no username or email was supplied");
      throw new NoSuchUserException("No such user");
    }
    String query = "SELECT p.name, p.surname, p.email, p.username, p.password, r.name AS role FROM person p JOIN role r ON p.role_id = r.role_id WHERE p.username = ? OR p.email = ?";
    Connection conn = getConn();
    try {
      conn.setAutoCommit(false);
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, username);
      stmt.setString(2, email);

      ResultSet res = stmt.executeQuery();

      if (!res.next()) {
        conn.rollback();
        log.info("User: '" + username + email + "' not found in DB");
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
      try {
        conn.rollback();
      } catch (SQLException ex) {
        log.error("SQLException: " + ex.getMessage());
        throw new DBException(ex);
      }
      log.error("SQLException: " + e.getMessage());
      throw new DBException(e);
    }

  }

  /**
   * Creates a person in the database
   *
   * @param name
   * @param surname
   * @param pnr
   * @param email
   * @param passwordHash
   * @param username
   * @throws UserExistsException if the person already exists (username or email)
   * @throws DBException         if an error in the database occurs
   */
  public static void createUser(String name, String surname, String pnr, String email,
      String passwordHash,
      String username)
      throws UserExistsException, DBException {
    Validation.validateCreateUser(name, surname, pnr, email, passwordHash, username);

    Connection conn = getConn();
    try {
      conn.setAutoCommit(false);

      PreparedStatement checkExists = conn.prepareStatement(
          "SELECT person_id FROM person WHERE username = ? OR email = ? OR pnr = ?");
      checkExists.setString(1, username);
      checkExists.setString(2, email);
      checkExists.setString(3, pnr);
      ResultSet rrs = checkExists.executeQuery();
      if (rrs.next() && rrs.getInt(1) > 0) {
        conn.rollback();
        log.info("no user created, username ('" + username + "'), personal number ('" + pnr
            + "') or email ('" + email
            + "') already exists");
        throw new UserExistsException("user already exists");
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
      stmt.setString(5, passwordHash);
      stmt.setString(6, username);
      stmt.setInt(7, role_id);
      //System.out.println(stmt);
      stmt.execute();

      conn.commit();
      conn.close();
    } catch (SQLException e) {
      try {
        conn.rollback();
        conn.close();
      } catch (SQLException ex) {
        log.error("SQLException" + ex.getMessage());
        throw new DBException(ex);
      }
      log.error("SQLException" + e.getMessage());
      throw new DBException(e);
    }


  }

  /**
   * list all applicants
   *
   * @return list of applicantDTO objects
   * @throws DBException
   */
  public static List<ApplicantDTO> applicants() throws DBException {
    Validation.validateApplicants();
    String query_availability = "SELECT * FROM availability ORDER BY person_id";

    String query_person = "SELECT p.person_id, p.name, p.surname, a.status from person p JOIN application a ON p.person_id = a.person_id";
    Connection conn = getConn();
    try {
      conn.setAutoCommit(false);
      PreparedStatement stmt_availability = conn.prepareStatement(query_availability);

      ResultSet res_a = stmt_availability.executeQuery();

      Map<Integer, List<Availability>> availabilities = new HashMap<>();
      while (res_a.next()) {
        int person_id = res_a.getInt("person_id");
        String from_date = res_a.getDate("from_date").toString();
        String to_date = res_a.getDate("to_date").toString();
        if (!availabilities.containsKey(person_id)) {
          availabilities.put(person_id, new ArrayList<>());
        }
        availabilities.get(person_id).add(new Availability(from_date, to_date));
      }

      PreparedStatement stmt_person = conn.prepareStatement(query_person);

      ResultSet res_p = stmt_person.executeQuery();
      List<ApplicantDTO> applicants = new ArrayList<>();
      while (res_p.next()) {
        int personID = res_p.getInt("person_id");
        String name = res_p.getString("name");
        String surname = res_p.getString("surname");
        String status = res_p.getString("status");
        List<Availability> applicantAvailabilities = availabilities.get(personID);
        if (applicantAvailabilities == null) {
          applicantAvailabilities = new ArrayList<>();
        }
        applicants.add(new ApplicantDTO(name, surname, status, applicantAvailabilities));
      }

      conn.commit();

      return applicants;

    } catch (SQLException e) {
      try {
        conn.rollback();
      } catch (SQLException ex) {
        log.error("SQLException: " + ex.getMessage());
        throw new DBException(ex);
      }
      log.error("SQLException: " + e.getMessage());
      throw new DBException(e);
    }
  }

  /**
   * Gets the expiration timestamp of the given reset link
   *
   * @param resetLink
   * @return expiration date/time in unix epoch millisecond
   * @throws DBException
   * @throws NoSuchUserException
   */
  public static long getPasswordResetLinkExpiratonTime(String resetLink)
      throws DBException, NoSuchUserException {
    Validation.validateGetPasswordResetlinkExpirationdate(resetLink);
    String query = "SELECT expiration_time FROM reset_link WHERE reset_link = ?";

    Connection conn = getConn();
    try {
      conn.setAutoCommit(false);
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, resetLink);

      ResultSet res = stmt.executeQuery();
      if (!res.next()) {
        conn.rollback();
        log.info("Requested password reset link not found in DB");
        throw new NoSuchUserException("No such reset link");
      }

      Timestamp time = res.getTimestamp("expiration_time");

      conn.commit();
      conn.close();

      return time.getTime();

    } catch (SQLException e) {
      try {
        conn.rollback();
        conn.close();
      } catch (SQLException ex) {
        log.error("SQLException" + ex.getMessage());
        throw new DBException(ex);
      }
      log.error("SQLException" + e.getMessage());
      throw new DBException(e);
    }
  }

  /**
   * create a new password reset link for a given user(by email) in database If one alreay exists
   * for given user it is overwritten.
   *
   * @param email     the user email
   * @param resetlink the reset link
   * @param timestamp expiration time (unix epoch millisecond)
   * @throws DBException
   */
  public static void createPasswordResetLink(String email, String resetlink, Long timestamp)
      throws DBException {
    Validation.validateCreatePasswordResetlink(email, resetlink, timestamp);
    String deleteQuery = "DELETE FROM reset_link WHERE person_id = (SELECT person_id FROM person WHERE email = ?)";
    String query =
        "INSERT INTO reset_link (person_id, reset_link, expiration_time) VALUES ((SELECT person_id FROM person WHERE email = ?), ?, ?)";

    Connection conn = getConn();
    try {
      conn.setAutoCommit(false);
      PreparedStatement delStmt = conn.prepareStatement(deleteQuery);
      delStmt.setString(1, email);
      boolean b = delStmt.execute();

      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, email);
      stmt.setString(2, resetlink);
      stmt.setTimestamp(3, new Timestamp(timestamp));
      boolean k = stmt.execute();
      conn.commit();
      conn.close();

    } catch (SQLException e) {
      try {
        conn.rollback();
        conn.close();
      } catch (SQLException ex) {
        log.error("SQLException" + ex.getMessage());
        throw new DBException(ex);
      }
      log.error("SQLException" + e.getMessage());
      throw new DBException(e);
    }
  }

  /**
   * Set a users password using a reset link.
   *
   * @param resetLink
   * @param passwordHash
   * @throws DBException
   */
  public static void setUserPasswordByResetLink(String resetLink, String passwordHash)
      throws DBException {
    log.debug("changing password Hash: " + passwordHash);
    Validation.validateSetUserPasswordByResetlink(resetLink, passwordHash);
    String query = "UPDATE person SET password = ? WHERE person_id = (SELECT person_id FROM reset_link WHERE reset_link = ?)";
    String deleteQuery = "DELETE FROM reset_link WHERE reset_link = ?";

    Connection conn = getConn();
    try {
      conn.setAutoCommit(false);
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, passwordHash);
      stmt.setString(2, resetLink);
      stmt.execute();

      PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
      deleteStmt.setString(1, resetLink);
      deleteStmt.execute();

      conn.commit();
      conn.close();

    } catch (SQLException e) {
      try {
        conn.rollback();
        conn.close();
      } catch (SQLException ex) {
        log.error("SQLException" + ex.getMessage());
        throw new DBException(ex);
      }
      log.error("SQLException" + e.getMessage());
      throw new DBException(e);

    }
  }
}
