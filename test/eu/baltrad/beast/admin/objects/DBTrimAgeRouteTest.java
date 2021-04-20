/**
 * 
 */
package eu.baltrad.beast.admin.objects;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.admin.objects.routes.DBTrimAgeRoute;

import static org.junit.Assert.assertEquals;

/**
 * @author anders
 *
 */
public class DBTrimAgeRouteTest extends EasyMockSupport {
  private DBTrimAgeRoute classUnderTest;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new DBTrimAgeRoute();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void isValid() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.setAge(1);
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(true, result);
  }
  
  @Test
  public void isValid_missingName() throws Exception {
    //classUnderTest.setName("A");
    classUnderTest.setAge(1);
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

  @Test
  public void isValid_missingAge() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.setAge(0);
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

}
