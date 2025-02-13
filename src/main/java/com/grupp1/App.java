package com.grupp1;


import com.grupp1.api.API;
import org.apache.commons.lang3.ObjectUtils.Null;


/**
 * Hello world!
 */
public class App {

  public static void main(String[] args) {
    String port = System.getenv("PORT");
    if (port != null) {
      new API(Integer.parseInt(port));
    } else {
      new API();
    }
  }
}
