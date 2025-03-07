package com.grupp1;


import com.grupp1.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Hello world!
 */
public class App {

  /**
   * This is the programs main entry point If the system has a PORT enironment variable, it will
   * decide the api port.
   *
   * @param args no arguments
   */
  public static void main(String[] args) {

    String port = System.getenv("PORT");
    if (port != null) {
      new API(Integer.parseInt(port));
    } else {
      new API();
    }
  }
}
