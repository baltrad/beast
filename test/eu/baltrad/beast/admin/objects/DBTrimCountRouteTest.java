/**
 * 
 */
package eu.baltrad.beast.admin.objects;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.admin.objects.routes.DBTrimCountRoute;

import static org.junit.Assert.assertEquals;

/**
 * @author anders
 *
 */
public class DBTrimCountRouteTest extends EasyMockSupport {
  private DBTrimCountRoute classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new DBTrimCountRoute();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  @Test
  public void isValid() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.setCount(1);
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(true, result);
  }
  
  @Test
  public void isValid_missingName() throws Exception {
    //classUnderTest.setName("A");
    classUnderTest.setCount(1);
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

  @Test
  public void isValid_missingCount() throws Exception {
    classUnderTest.setName("A");
    //classUnderTest.setCount(1);
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

}
