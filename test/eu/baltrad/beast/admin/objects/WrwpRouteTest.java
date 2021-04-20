/**
 * 
 */
package eu.baltrad.beast.admin.objects;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.admin.objects.routes.VolumeRoute;
import eu.baltrad.beast.admin.objects.routes.WrwpRoute;

/**
 * @author anders
 *
 */
public class WrwpRouteTest extends EasyMockSupport {
  private WrwpRoute classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    classUnderTest = new WrwpRoute();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  @Test
  public void isValid() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.getSources().add("S1");
    classUnderTest.getRecipients().add("R1");
    classUnderTest.getFields().add("FF_DEV");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(true, result);
  }
  
  @Test
  public void isValid_missingName() throws Exception {
    //classUnderTest.setName("A");
    classUnderTest.getSources().add("S1");
    classUnderTest.getRecipients().add("R1");
    classUnderTest.getFields().add("FF_DEV");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

  @Test
  public void isValid_missingSources() throws Exception {
    classUnderTest.setName("A");
    //classUnderTest.getSources().add("S1");
    classUnderTest.getRecipients().add("R1");
    classUnderTest.getFields().add("FF_DEV");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

  @Test
  public void isValid_missingRecipients() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.getSources().add("S1");
    //classUnderTest.getRecipients().add("R1");
    classUnderTest.getFields().add("FF_DEV");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

  @Test
  public void isValid_missingFields() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.getSources().add("S1");
    classUnderTest.getRecipients().add("R1");
    //classUnderTest.getFields().add("FF_DEV");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

}
