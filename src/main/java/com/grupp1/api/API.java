package com.grupp1.api;

import com.grupp1.api.Tokenizer.TokenData;
import com.grupp1.controller.Controller;

import com.grupp1.controller.PasswordException;
import com.grupp1.controller.UserDTO;
import com.grupp1.db.NoSuchUserException;
import org.json.JSONObject;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

import static spark.Spark.before;
import static spark.Spark.ipAddress;

public class API {

  public API() {
    this(4567);
  }

  public API(int port) {
    if (port != 4567) {
      Spark.port(port);
    }
    setUpEndpoints();
    if (System.getenv("PORT") != null) {
      enableCORS("https://recruitment-application-fronte-593587373fd5.herokuapp.com", "*",
          "content-type");
    } else {
      enableCORS("http://localhost:5173", "*", "content-type");
    }
  }

  private void setUpEndpoints() {
    //Spark.get("/hello/:name", this::hello);
    Spark.post("/login", this::login);
    Spark.options("/login", this::test);
    Spark.post("/register", this::register);
    Spark.options("/register", this::test);
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

  String test(Request req, Response res) {
    return "ble";
  }

  String login(Request req, Response res) {
    try {
      JSONObject cryptJson = Json.parseJson(req.body());
      Validation.validateEncrypted(cryptJson);
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
      TokenData tokenObj = new Tokenizer().createToken(user.username());

      JSONObject responseJson = new JSONObject();
      responseJson.put("token", tokenObj.token);
      responseJson.put("username", user.username());
      responseJson.put("userEmail", user.email());
      responseJson.put("expirationDate", tokenObj.expirationDate);

      System.out.println(responseJson.toString());
      return responseJson.toString();


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

  String register(Request req, Response res) {
    try {
      JSONObject cryptJson = Json.parseJson(req.body());
      Validation.validateEncrypted(cryptJson);
      JSONObject json = Crypt.decryptJson(cryptJson);
      Validation.validateRegister(json);

      String firstName = json.getString("firstName");
      String lastName = json.getString("lastName");
      String personalNumber = json.getString("personalNumber");
      String email = json.getString("email");
      String userPassword = json.getString("userPassword");
      String userName = json.getString("username");
      Controller.register(firstName, lastName, personalNumber, email, userPassword, userName);
      return "";

    } catch (APIException e) {
      res.status(400);
      return "Bad Input:\n" + e.getMessage() + "\r\n\r\n";
    } catch (ServerException e) {
      res.status(500);
      return "Internal server error:\n" + e.getMessage() + "\r\n\r\n";
    } catch (Throwable e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
  }
}
