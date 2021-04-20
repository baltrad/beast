/**
 * 
 */
package eu.baltrad.beast.admin.objects;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.admin.objects.routes.GmapRoute;

/**
 * @author anders
 *
 */
public class GmapRouteTest extends EasyMockSupport {
  private GmapRoute classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    classUnderTest = new GmapRoute();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  @Test
  public void isValid() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.setArea("area");
    classUnderTest.setPath("/p");
    classUnderTest.getRecipients().add("R");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(true, result);
  }

  @Test
  public void isValid_missingName() throws Exception {
    //classUnderTest.setName("A");
    classUnderTest.setArea("area");
    classUnderTest.setPath("/p");
    classUnderTest.getRecipients().add("R");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }
  
  @Test
  public void isValid_missingArea() throws Exception {
    classUnderTest.setName("A");
    //classUnderTest.setArea("area");
    classUnderTest.setPath("/p");
    classUnderTest.getRecipients().add("R");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }
  
  @Test
  public void isValid_missingPath() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.setArea("area");
    //classUnderTest.setPath("/p");
    classUnderTest.getRecipients().add("R");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

  @Test
  public void isValid_missingRecipients() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.setArea("area");
    classUnderTest.setPath("/p");
    //classUnderTest.getRecipients().add("R");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

}
