package com.grupp1.api;

import com.grupp1.api.Tokenizer.TokenData;
import com.grupp1.controller.ApplicantDTO;
import com.grupp1.controller.Availability;
import com.grupp1.controller.Controller;
import com.grupp1.controller.IllegalRoleException;
import com.grupp1.controller.PasswordException;
import com.grupp1.controller.UserDTO;
import com.grupp1.db.NoSuchUserException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

import static spark.Spark.before;

public class API {

  static final Logger log = LoggerFactory.getLogger(API.class);

  public API() {
    this(4567);
  }

  public API(int port) {
    if (port != 4567) {
      Spark.port(port);
    }
    setUpEndpoints();
    if (System.getenv("PORT") != null) {
      enableCORS("https://recruitment-application-fronte-593587373fd5.herokuapp.com",
          "POST, OPTIONS",
          "content-type");
    } else {
      enableCORS("http://localhost:5173", "POST, OPTIONS", "content-type");
    }
  }

  private void setUpEndpoints() {
    //Spark.get("/hello/:name", this::hello);
    Spark.post("/login", this::login);
    Spark.options("/login", this::options);
    Spark.post("/register", this::register);
    Spark.post("/applicants", this::applicants);
    Spark.options("/applicants", this::options);
    Spark.options("/register", this::options);
    Spark.post("/password-reset/validate-link", this::passwordResetValidatelink);
    Spark.post("/password-reset/create-link", this::passwordResetCreateLink);
    Spark.post("/password-reset", this::passwordReset);
    Spark.options("/password-reset", this::options);
    Spark.options("/password-reset/validate-link", this::options);
    Spark.options("/password-reset/create-link", this::options);
  }

  private static void enableCORS(final String origin, final String methods, final String headers) {
    before(new Filter() {
      @Override
      public void handle(Request request, Response response) throws Exception {
        response.header("Access-Control-Allow-Origin", origin);
        response.header("Access-Control-Request-Method", methods);
        response.header("Access-Control-Allow-Headers", headers);
      }
    });
  }

  private String options(Request req, Response res) {
    logRequest(req);
    return "";
  }

  private String login(Request req, Response res) {
    logRequest(req);
    try {
      //testcode
      /*
      JSONObject json = Json.parseJson(req.body());
      json.put("symmetricKey",
          Crypt.decryptRSA(
              "sG6xj4VkLWVOHBwJDSVyi5AWqT3ix6w2/2TQj8pU95Rc/RBqgaPVtp2WiRMMEL/FpurXpv/Y6g3jyT5mdx6KLcxI0jmqQsFkic96s9y6kaKxSoTCGrTrOwMixLjm9dkHmYEzdkGjrPh38a3XymeFOVoyLK07YuvMU3uJ8CdgRzw="));
      */
      JSONObject cryptJson = Json.parseJson(req.body());
      JSONObject json = Crypt.decryptJson(cryptJson);

      Validation.validateLogin(json);
      String username = null;
      String email = null;
      if (json.has("username")) {
        username = json.getString("username");
      }
      if (json.has("userEmail")) {
        email = json.getString("email");
      }

      UserDTO user = Controller.login(username, email, json.getString("userPassword"));
      TokenData tokenObj = Tokenizer.createToken(user.username());

      JSONObject responseJson = new JSONObject();
      responseJson.put("token", tokenObj.token());
      responseJson.put("username", user.username());
      responseJson.put("userEmail", user.email());
      responseJson.put("expirationDate", tokenObj.expirationDate());

      System.out.println("test");
      System.out.println(responseJson.toString());

      return Crypt.encryptJson(responseJson, json.getString("symmetricKey"),
          json.getString("timestamp")).toString();

    } catch (ValidationException | NoSuchUserException e) {
      res.status(400);
      return "Bad Input:\n" + e.getMessage() + "\r\n\r\n";
    } catch (BadCryptException e) {
      res.status(400);
      return "Crypt error:\n" + e.getMessage() + "\r\n\r\n"; //TODO crypt error string change
    } catch (PasswordException e) {
      res.status(403);
      return "Forbidden:\n" + e.getMessage() + "\r\n\r\n";
    } catch (ServerException e) {
      res.status(500);
      return "Internal server error:\n" + e.getMessage() + "\r\n\r\n";
    }
  }

  private String register(Request req, Response res) {
    logRequest(req);
    try {
      JSONObject cryptJson = Json.parseJson(req.body());
      JSONObject json = Crypt.decryptJson(cryptJson);
      Validation.validateRegister(json);

      String firstName = json.getString("firstName");
      String lastName = json.getString("lastName");
      String personalNumber = json.getString("personalNumber");
      String email = json.getString("email");
      String userPassword = json.getString("userPassword");
      String userName = json.getString("username");
      Controller.register(firstName, lastName, personalNumber, email, userPassword, userName);
      JSONObject responseJson = new JSONObject();
      return Crypt.encryptJson(responseJson, json.getString("symmetricKey"),
          json.getString("timestamp")).toString();

    } catch (APIException e) {
      res.status(400);
      return "Bad Input:\n" + e.getMessage() + "\r\n\r\n";
    } catch (ServerException e) {
      res.status(500);
      return "Internal server error:\n" + e.getMessage() + "\r\n\r\n";
    }
  }

  private String applicants(Request req, Response res) {
    logRequest(req);
    try {
      JSONObject cryptJson = Json.parseJson(req.body());
      JSONObject json = Crypt.decryptJson(cryptJson);

      Validation.validateApplicants(json);
      String token = json.getString("token");
      TokenData tokenData = Tokenizer.extractToken(token);
      List<ApplicantDTO> applicants = Controller.applicants(tokenData.username());

      //applicants is a list of applicantDTOs
      //applicant DTO innehåller strängar och en lista av availabilitys
      // en availability innehåller en to och en from sträng

      List<JSONObject> applicantsList = new ArrayList<>();
      for (ApplicantDTO applicant : applicants) {
        JSONObject appli = new JSONObject();
        List<JSONObject> availabilities = new ArrayList<>();
        for (Availability date : applicant.availabilities()) {
          JSONObject available = new JSONObject();
          String fromDate = date.from();
          String toDate = date.to();
          available.put("from", fromDate);
          available.put("to", toDate);
          availabilities.add(available);
        }
        appli.put("name", applicant.name());
        appli.put("surname", applicant.surname());
        appli.put("status", applicant.status());
        appli.put("availabilities", availabilities);
        applicantsList.add(appli);
      }
      json.put("applicants", applicantsList);// this does not work

      System.out.println(json);

      res.status(200);
      return Crypt.encryptJson(json, json.getString("symmetricKey"),
          json.getString("timestamp")).toString();
      // TODO Must fix catches
    } catch (ValidationException | NoSuchUserException e) {
      res.status(400);
      return "Bad Input:\n" + e.getMessage() + "\r\n\r\n";
    } catch (BadCryptException e) {
      res.status(400);
      return "Crypt error:\n" + e.getMessage() + "\r\n\r\n"; //TODO crypt error string change
    } catch (IllegalRoleException e) {
      res.status(403);
      return "Forbidden:\n" + e.getMessage() + "\r\n\r\n";
    } catch (ServerException e) {
      res.status(500);
      return "Internal server error:\n" + e.getMessage() + "\r\n\r\n";

    }


  }

  //{String link, String newPassword}
  private String passwordReset(Request req, Response res) {
    logRequest(req);
    try {
      JSONObject cryptJson = Json.parseJson(req.body());
      JSONObject json = Crypt.decryptJson(cryptJson);

      Validation.validatePasswordReset(json);

      Controller.resetPasswordWithLink(json.getString("link"), json.getString("password"));

      res.status(200);
      return "if you gaze long into an abyss, the abyss will also gaze into you.";
      // TODO Must fix catches
    } catch (ValidationException | NoSuchUserException |
             BadApiInputException e) { //| NoSuchUserException e) {
      res.status(400);
      return "Bad Input:\n" + e.getMessage() + "\r\n\r\n";
    } catch (BadCryptException e) {
      res.status(400);
      return "Crypt error:\n" + e.getMessage() + "\r\n\r\n"; //TODO crypt error string change
    /*} catch (IllegalRoleException e) {
      res.status(403);
      return "Forbidden:\n" + e.getMessage() + "\r\n\r\n";

     */
    } catch (ServerException e) {
      res.status(500);
      return "Internal server error:\n" + e.getMessage() + "\r\n\r\n";
    }
  }

  //{String username, String email}
  private String passwordResetCreateLink(Request req, Response res) {
    logRequest(req);
    try {
      JSONObject cryptJson = Json.parseJson(req.body());
      JSONObject json = Crypt.decryptJson(cryptJson);

      Validation.validatePasswordResetCreatelink(json);

      Controller.createPasswordResetLink(json.getString("email"));

      res.status(200);
      JSONObject responseJson = new JSONObject();
      return Crypt.encryptJson(responseJson, json.getString("symmetricKey"),
          json.getString("timestamp")).toString();

      // TODO Must fix catches
    } catch (ValidationException e) { //| NoSuchUserException e) {
      res.status(400);
      return "Bad Input:\n" + e.getMessage() + "\r\n\r\n";
    } catch (BadCryptException e) {
      res.status(400);
      return "Crypt error:\n" + e.getMessage() + "\r\n\r\n"; //TODO crypt error string change
    } catch (ServerException e) {
      res.status(500);
      return "Internal server error:\n" + e.getMessage() + "\r\n\r\n";
    }
  }

  //{String link}
  private String passwordResetValidatelink(Request req, Response res) {
    logRequest(req);
    try {
      JSONObject cryptJson = Json.parseJson(req.body());
      JSONObject json = Crypt.decryptJson(cryptJson);

      Validation.validatePasswordResetValidateLink(json);

      Controller.validatePasswordResetLink(json.getString("link"));

      res.status(200);
      JSONObject responseJson = new JSONObject();
      responseJson.put("valid", true);
      return Crypt.encryptJson(responseJson, json.getString("symmetricKey"),
          json.getString("timestamp")).toString();

      // TODO Must fix catches
    } catch (APIException | NoSuchUserException e) { //| NoSuchUserException e) {
      res.status(400);
      return "Bad Input:\n" + e.getMessage() + "\r\n\r\n";
    /*} catch (IllegalRoleException e) {
      res.status(403);
      return "Forbidden:\n" + e.getMessage() + "\r\n\r\n";*/
    } catch (ServerException e) {
      res.status(500);
      return "Internal server error:\n" + e.getMessage() + "\r\n\r\n";

    }
  }

  private void logRequest(Request req) {
    log.info("API call: " + req.pathInfo());
    StringBuilder s = new StringBuilder();
    for (String h : req.headers()) {
      s.append('[').append(h).append(":");
      s.append(req.headers(h));
      s.append("],");
    }
    log.debug("Headers: " + s.toString());
  }
}
