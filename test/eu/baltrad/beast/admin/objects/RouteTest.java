/**
 * 
 */
package eu.baltrad.beast.admin.objects;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.admin.objects.routes.Route;

/**
 * @author anders
 */
public class RouteTest extends EasyMockSupport {
  private Route classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = createMockBuilder(Route.class)
        .addMockedMethod("isValid")
        .createMock();

  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void validate() {
    classUnderTest.setAuthor("A");
    classUnderTest.setDescription("D");
    classUnderTest.setName("N");
    
    expect(classUnderTest.isValid()).andReturn(true);

    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }
  
  @Test
  public void validate_missingAuthor() {
    classUnderTest.setDescription("D");
    classUnderTest.setName("N");
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(false, result);
  }

  @Test
  public void validate_missingDescription() {
    classUnderTest.setAuthor("A");
    classUnderTest.setName("N");
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(false, result);
  }

  @Test
  public void validate_missingName() {
    classUnderTest.setAuthor("A");
    classUnderTest.setDescription("D");
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(false, result);
  }
  
  @Test
  public void validate_invalidSubclass() {
    classUnderTest.setAuthor("A");
    classUnderTest.setDescription("D");
    classUnderTest.setName("N");
    
    expect(classUnderTest.isValid()).andReturn(false);

    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(false, result);
  }


}
