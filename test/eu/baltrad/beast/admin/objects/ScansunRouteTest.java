/**
 * 
 */
package eu.baltrad.beast.admin.objects;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.admin.objects.routes.ScansunRoute;

/**
 * @author anders
 *
 */
public class ScansunRouteTest extends EasyMockSupport {
  private ScansunRoute classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    classUnderTest = new ScansunRoute();
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
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(true, result);
  }
  
  @Test
  public void isValid_missingName() throws Exception {
    //classUnderTest.setName("A");
    classUnderTest.getSources().add("S1");
    classUnderTest.getRecipients().add("R1");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }
  
  @Test
  public void isValid_missingSources() throws Exception {
    classUnderTest.setName("A");
    //classUnderTest.getSources().add("S1");
    classUnderTest.getRecipients().add("R1");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }
  
  @Test
  public void isValid_missingRecipients() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.getSources().add("S1");
    //classUnderTest.getRecipients().add("R1");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }


}


