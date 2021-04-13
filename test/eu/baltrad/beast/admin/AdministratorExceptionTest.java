/**
 * 
 */
package eu.baltrad.beast.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author anders
 *
 */
public class AdministratorExceptionTest {
  @Test
  public void testConstructor() {
    AdministratorException classUnderTest = new AdministratorException();
    assertTrue(classUnderTest instanceof AdministratorException);
  }
  
  @Test
  public void testStringConstructor() {
    AdministratorException classUnderTest = new AdministratorException("something");
    assertEquals("something", classUnderTest.getMessage());
  }
  
  @Test
  public void testThrowableConstructor() {
    RuntimeException x = new RuntimeException("something");
    AdministratorException classUnderTest = new AdministratorException(x);
    assertEquals("something", classUnderTest.getCause().getMessage());
  }
  
  @Test
  public void testStringThrowableConstructor() {
    RuntimeException x = new RuntimeException("something");
    AdministratorException classUnderTest = new AdministratorException("else", x);
    assertEquals("else", classUnderTest.getMessage());
    assertEquals("something", classUnderTest.getCause().getMessage());
  }

}
