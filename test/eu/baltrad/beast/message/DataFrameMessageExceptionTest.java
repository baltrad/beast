package eu.baltrad.beast.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DataFrameMessageExceptionTest {
  @Test
  public void testConstructor() {
    DataFrameMessageException classUnderTest = new DataFrameMessageException();
    assertTrue(classUnderTest instanceof DataFrameMessageException);
  }
  
  @Test
  public void testStringConstructor() {
    DataFrameMessageException classUnderTest = new DataFrameMessageException("something");
    assertEquals("something", classUnderTest.getMessage());
  }
  
  @Test
  public void testThrowableConstructor() {
    RuntimeException x = new RuntimeException("something");
    DataFrameMessageException classUnderTest = new DataFrameMessageException(x);
    assertEquals("something", classUnderTest.getCause().getMessage());
  }
  
  @Test
  public void testStringThrowableConstructor() {
    RuntimeException x = new RuntimeException("something");
    DataFrameMessageException classUnderTest = new DataFrameMessageException("else", x);
    assertEquals("else", classUnderTest.getMessage());
    assertEquals("something", classUnderTest.getCause().getMessage());
  }

}
