package com.grupp1.api;

import org.json.JSONException;
import org.json.JSONObject;

class Json {

  static JSONObject parseJson(String jsonString) throws ValidationException {
    try {
      JSONObject json = new JSONObject(jsonString);
      return json;
    } catch (JSONException e) {
      API.log.info("Validation not passed, invalid json: " + e.getMessage());
      throw new ValidationException("Not valid JSON");
    }
  }

}
