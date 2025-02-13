package com.grupp1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.grupp1.api.BadApiInputException;
import com.grupp1.api.ServerException;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import com.fasterxml.jackson.databind.ObjectMapper;
public class ControllerTest extends TestCase {

//TODO I need to implement a teardown where the user is removed from the database.

  /**
   * public void testRegisterMissingField()
   * Test what happens if a field is missing
   * @throws JsonProcessingException
   */
  public void testRegisterMissingField() throws JsonProcessingException{
    String missingFieldJson = "{"
        +"\"firstName\" : \"feta\","
        +"\"lastName\" : \"ost\","
        +"\"email\" : \"feta@ost.com\","
        +"\"personalNumber\" : \"12345588-9876\","
        +"\"username\" : \"feta\","
        +"\"confirmUserPassword\" : \"\""
        +"}";

    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, Object> map = objectMapper.readValue(missingFieldJson, HashMap.class);

    try{
      Controller.register(map);
      fail("Expected exception was not thrown");
    }
    catch (Exception e){
      assertEquals(BadApiInputException.class, e.getClass());
      String message = e.getMessage();
      assertEquals("missing 'userPassword' parameter", message);
    }
  }

  /**
   * public void testRegisterNullField()
   * Test what happens if i field value is null
   * @throws JsonProcessingException
   */
  public void testRegisterNullField() throws JsonProcessingException {
    String nullFielsJson = "{"
        + "\"firstName\" : \"feta\","
        + "\"lastName\" : \"ost\","
        + "\"email\" : \"feta@ost.com\","
        + "\"personalNumber\" : \"12345588-9876\","
        + "\"username\" : \"feta\","
        + "\"userPassword\" : null,"
        + "\"confirmUserPassword\" : \"\""
        + "}";

    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, Object> map = objectMapper.readValue(nullFielsJson, HashMap.class);

    try{
      Controller.register(map);
      fail("Expected exception was not thrown");
    }
    catch (Exception e){
      assertEquals(BadApiInputException.class, e.getClass());
      String message = e.getMessage();
      assertEquals("missing 'userPassword' parameter", message);
    }
  }

  /**
   * public void testRegisterEmptyStringField()
   * test what happens if a field value is an empty string
   * @throws JsonProcessingException
   */
  public void testRegisterEmptyStringField() throws JsonProcessingException {
    String emptyFirstNameStringJson = "{"
        +"\"firstName\" : \"\","
        +"\"lastName\" : \"ost\","
        +"\"email\" : \"feta@ost.com\","
        +"\"personalNumber\" : \"12345588-9876\","
        +"\"username\" : \"feta\","
        +"\"userPassword\" : \"ost\","
        +"\"confirmUserPassword\" : \"\""
        +"}";

    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, Object> map = objectMapper.readValue(emptyFirstNameStringJson, HashMap.class);

    try{
      Controller.register(map);
      fail("Expected exception was not thrown");
    }
    catch (Exception e){
      assertEquals(BadApiInputException.class, e.getClass());
      String message = e.getMessage();
      assertEquals("'firstName' too short", message);
    }
  }

  /**
   * public void testRegisterTooLongField()
   * test what happens if a field value is longer than 255 chars
   * @throws JsonProcessingException
   */
  public void testRegisterTooLongField() throws JsonProcessingException {
    String tooLongLastNameJson = "{"
        +"\"firstName\" : \"feta\","
        +"\"lastName\" : \"ostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostostost\","
        +"\"email\" : \"feta@ost.com\","
        +"\"personalNumber\" : \"12345588-9876\","
        +"\"username\" : \"feta\","
        +"\"userPassword\" : \"ost\","
        +"\"confirmUserPassword\" : \"\""
        +"}";

    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, Object> map = objectMapper.readValue(tooLongLastNameJson, HashMap.class);

    try{
      Controller.register(map);
      fail("Expected exception was not thrown");
    }
    catch (Exception e){
      assertEquals(BadApiInputException.class, e.getClass());
      String message = e.getMessage();
      assertEquals("'lastName' too long", message);
    }
  }

  /**
   * public void testRegisterWrongTypeField()
   * test what happens if field value is not of String type
   * @throws JsonProcessingException
   */
  public void testRegisterWrongTypeField() throws JsonProcessingException {
    String lastNameNotStringJson = "{"
        +"\"firstName\" : \"feta\","
        +"\"lastName\" : 18274,"
        +"\"email\" : \"feta@ost.com\","
        +"\"personalNumber\" : \"12345588-9876\","
        +"\"username\" : \"feta\","
        +"\"userPassword\" : \"ost\","
        +"\"confirmUserPassword\" : \"\""
        +"}";

    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, Object> map = objectMapper.readValue(lastNameNotStringJson, HashMap.class);

    try{
      Controller.register(map);
      fail("Expected exception was not thrown");
    }
    catch (Exception e){
      assertEquals(BadApiInputException.class, e.getClass());
      String message = e.getMessage();
      assertEquals("bad 'lastName' parameter (should be string)", message);
    }
  }

  /**
   * public void testRegisterWrongPersonalNumberFormatField()
   * test what happens if personalNumber is on the wrong format
   * @throws JsonProcessingException
   */
  public void testRegisterWrongPersonalNumberFormatField() throws JsonProcessingException {
    String wrongPersonalNumberFormatJson = "{"
        +"\"firstName\" : \"feta\","
        +"\"lastName\" : \"ost\","
        +"\"email\" : \"feta@ost.com\","
        +"\"personalNumber\" : \"123455\","
        +"\"username\" : \"feta\","
        +"\"userPassword\" : \"ost\","
        +"\"confirmUserPassword\" : \"\""
        +"}";

    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, Object> map = objectMapper.readValue(wrongPersonalNumberFormatJson, HashMap.class);

    try{
      Controller.register(map);
      fail("Expected exception was not thrown");
    }
    catch (Exception e){
      assertEquals(BadApiInputException.class, e.getClass());
      //String message = e.getMessage();
      //assertEquals("invalid 'lastName' too long", message);
    }
  }

  /**
   * public void testRegisterWrongEmailFormatField()
   * test what happens if email is on the wrong format
   * @throws JsonProcessingException
   */
  public void testRegisterWrongEmailFormatField() throws JsonProcessingException {
    String badEmailJson = "{"
        +"\"firstName\" : \"feta\","
        +"\"lastName\" : \"ost\","
        +"\"email\" : \"feta@ost\","
        +"\"personalNumber\" : \"12345588-9876\","
        +"\"username\" : \"feta\","
        +"\"userPassword\" : \"ost\","
        +"\"confirmUserPassword\" : \"\""
        +"}";

    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, Object> map = objectMapper.readValue(badEmailJson, HashMap.class);

    try{
      Controller.register(map);
      fail("Expected exception was not thrown");
    }
    catch (Exception e){
      assertEquals(BadApiInputException.class, e.getClass());
      //String message = e.getMessage();
      //assertEquals("invalid 'lastName' too long", message);
    }
  }

  /**
   * public void testRegister()
   * test what happens if nothing is wtong with the input
   * @throws JsonProcessingException
   * @throws BadApiInputException
   * @throws ServerException
   */
  public void testRegister() throws JsonProcessingException, BadApiInputException, ServerException {
    String okJson = "{"
        +"\"firstName\" : \"feta\","
        +"\"lastName\" : \"ost\","
        +"\"email\" : \"feta@ost.com\","
        +"\"personalNumber\" : \"12345588-9876\","
        +"\"username\" : \"feta\","
        +"\"userPassword\" : \"ost\","
        +"\"confirmUserPassword\" : \"\""
        +"}";

    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, Object> map = objectMapper.readValue(okJson, HashMap.class);

    try {
      assertTrue(Controller.register(map));
    }
    catch (Exception e){
      fail("Exception " + e +" was thrown");
    }
  }
}