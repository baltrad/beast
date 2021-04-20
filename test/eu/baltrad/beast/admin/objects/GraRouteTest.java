/**
 * 
 */
package eu.baltrad.beast.admin.objects;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.admin.objects.routes.GraRoute;

/**
 * @author anders
 *
 */
public class GraRouteTest extends EasyMockSupport {
  private GraRoute classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    classUnderTest = new GraRoute();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  @Test
  public void isValid() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.setArea("area");
    classUnderTest.getRecipients().add("R");
    classUnderTest.setAcceptableLoss(0);
    classUnderTest.setFilesPerHour(4);
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(true, result);
  }
  
  @Test
  public void isValid_missingName() throws Exception {
    //classUnderTest.setName("A");
    classUnderTest.setArea("area");
    classUnderTest.getRecipients().add("R");
    classUnderTest.setAcceptableLoss(0);
    classUnderTest.setFilesPerHour(4);
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

  @Test
  public void isValid_missingArea() throws Exception {
    classUnderTest.setName("A");
    //classUnderTest.setArea("area");
    classUnderTest.getRecipients().add("R");
    classUnderTest.setAcceptableLoss(0);
    classUnderTest.setFilesPerHour(4);
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

  @Test
  public void isValid_missingRecipients() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.setArea("area");
    //classUnderTest.getRecipients().add("R");
    classUnderTest.setAcceptableLoss(0);
    classUnderTest.setFilesPerHour(4);
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

  @Test
  public void isValid_invalidAcceptableLoss() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.setArea("area");
    classUnderTest.getRecipients().add("R");
    classUnderTest.setAcceptableLoss(-1);
    classUnderTest.setFilesPerHour(4);
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

  @Test
  public void isValid_invalidFilesPerHour() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.setArea("area");
    classUnderTest.getRecipients().add("R");
    classUnderTest.setAcceptableLoss(0);
    classUnderTest.setFilesPerHour(-1);
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }
}
