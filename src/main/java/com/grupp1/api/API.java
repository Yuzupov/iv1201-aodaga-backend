package com.grupp1.api;

import com.grupp1.controller.Controller;

import org.json.JSONObject;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

import static spark.Spark.before;

public class API {

  public API() {
    this(4567);
  }

  public API(int port) {
    if (port != 4567) {
      Spark.port(port);
    }
    setUpEndpoints();
    enableCORS("http://localhost:5173", "*", "content-type");
  }

  private void setUpEndpoints() {
    //Spark.get("/hello/:name", this::hello);
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
      return "woop woop";

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
