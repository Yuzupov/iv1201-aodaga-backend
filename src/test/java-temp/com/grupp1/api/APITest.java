package com.grupp1.api;

import java.util.HashMap;
import org.junit.Before;
import junit.framework.TestCase;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

public class APITest extends TestCase {

  @org.junit.jupiter.api.Test
  void register() {
  }

  @Before
  public void setUp() {
  }

  private class FakeRegisterObject{

    String body(){
      return  "{"
          +"\"firstName\" : \"feta\","
          +"\"lastName\" : \"ost\","
          +"\"email\" : \"feta@ost.com\","
          +"\"personalNumber\" : \"12355\","
          +"\"userName\" : \"feta\","
          +"\"UserPassword\" : \"ost\","
          +"\"confirmUserPassword\" : \"\""
          +"}";
    }
  }

  public void testRegister() {

    FakeRegisterObject req = new FakeRegisterObject();
    API api = new API(4567);
    //assertEquals("woop woop", api.register(req));
  }
}

/*
    "{"
          +"\"firstName\" : \"feta\","
          +"\"lastName\" : \"ost\","
          +"\"email\" : \"feta@ost.com\","
          +"\"personalNumber\" : \"12355\","
          +"\"username\" : \"feta\","
          +"\"UserPassword\" : \"ost\","
          +"\"confirmUserPassword\" : \"\""
          +"}";

    */