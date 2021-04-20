/**
 * 
 */
package eu.baltrad.beast.admin.objects;

import static org.junit.Assert.assertEquals;

import org.codehaus.jackson.map.ObjectMapper;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.admin.objects.routes.DistributionRoute;
import eu.baltrad.beast.db.AlwaysMatchFilter;

/**
 * @author anders
 *
 */
public class DistributionRouteTest extends EasyMockSupport {
  private DistributionRoute classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    classUnderTest = new DistributionRoute();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  @Test
  public void isValid() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.setDestination("copy:///somewhere");
    classUnderTest.setFilter(new AlwaysMatchFilter());
    classUnderTest.setNameTemplate("x.h5");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(true, result);
  }
  
  @Test
  public void isValid_missingName() throws Exception {
    //classUnderTest.setName("A");
    classUnderTest.setDestination("copy:///somewhere");
    classUnderTest.setFilter(new AlwaysMatchFilter());
    classUnderTest.setNameTemplate("x.h5");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

  @Test
  public void isValid_missingDestination() throws Exception {
    classUnderTest.setName("A");
    //classUnderTest.setDestination("copy:///somewhere");
    classUnderTest.setFilter(new AlwaysMatchFilter());
    classUnderTest.setNameTemplate("x.h5");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

  @Test
  public void isValid_invalidDestination() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.setDestination("this is not an uri");
    classUnderTest.setFilter(new AlwaysMatchFilter());
    classUnderTest.setNameTemplate("x.h5");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

  @Test
  public void isValid_missingNameTemplate() throws Exception {
    classUnderTest.setName("A");
    classUnderTest.setDestination("copy:///somewhere");
    classUnderTest.setFilter(new AlwaysMatchFilter());
    //classUnderTest.setNameTemplate("x.h5");
    
    boolean result = classUnderTest.isValid();
    
    assertEquals(true, result);
  }

}
