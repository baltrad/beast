package eu.baltrad.beast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class InitializationExceptionTest {
  @Test
  public void testConstructor() {
    InitializationException classUnderTest = new InitializationException();
    assertTrue(classUnderTest instanceof InitializationException);
  }
  
  @Test
  public void testStringConstructor() {
    InitializationException classUnderTest = new InitializationException("something");
    assertEquals("something", classUnderTest.getMessage());
  }
  
  @Test
  public void testThrowableConstructor() {
    RuntimeException x = new RuntimeException("something");
    InitializationException classUnderTest = new InitializationException(x);
    assertEquals("something", classUnderTest.getCause().getMessage());
  }
  
  @Test
  public void testStringThrowableConstructor() {
    RuntimeException x = new RuntimeException("something");
    InitializationException classUnderTest = new InitializationException("else", x);
    assertEquals("else", classUnderTest.getMessage());
    assertEquals("something", classUnderTest.getCause().getMessage());
  }
}
