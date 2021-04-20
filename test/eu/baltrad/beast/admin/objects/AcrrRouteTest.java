/**
 * 
 */
package eu.baltrad.beast.admin.objects;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.admin.objects.routes.AcrrRoute;

import static org.junit.Assert.assertEquals;

/**
 * @author anders
 *
 */
public class AcrrRouteTest extends EasyMockSupport {
  private AcrrRoute classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new AcrrRoute();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  @Test
  public void isValid() throws Exception {
    classUnderTest.setName("N");
    classUnderTest.getRecipients().add("X");
    classUnderTest.setArea("anarea");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(true, result);
  }
  
  @Test
  public void isValid_noArea() throws Exception {
    classUnderTest.setName("N");
    classUnderTest.getRecipients().add("X");
    
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
