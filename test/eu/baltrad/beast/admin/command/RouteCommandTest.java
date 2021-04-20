/**
 * 
 */
package eu.baltrad.beast.admin.command;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.admin.objects.routes.Route;
import static org.easymock.EasyMock.expect;

/**
 * @author anders
 *
 */
public class RouteCommandTest extends EasyMockSupport {
  private RouteCommand classUnderTest = null;
  private Route route = null;
  
  @Before
  public void setUp() throws Exception {
    route = createMock(Route.class);
    classUnderTest = new RouteCommand();
    classUnderTest.setRoute(route);
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  
  @Test
  public void test_ADD() throws Exception {
    classUnderTest.setOperation(RouteCommand.ADD);
    
    expect(route.validate()).andReturn(true);
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }

  @Test
  public void test_UPDATE() throws Exception {
    classUnderTest.setOperation(RouteCommand.UPDATE);
    
    expect(route.validate()).andReturn(true);
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }

  @Test
  public void test_REMOVE() throws Exception {
    classUnderTest.setOperation(RouteCommand.REMOVE);
    
    expect(route.getName()).andReturn("A").anyTimes();
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }
  
  @Test
  public void test_REMOVE_missingName() throws Exception {
    classUnderTest.setOperation(RouteCommand.REMOVE);
    
    expect(route.getName()).andReturn("").anyTimes();
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(false, result);
  }

  @Test
  public void test_GET() throws Exception {
    classUnderTest.setOperation(RouteCommand.GET);
    
    expect(route.getName()).andReturn("A").anyTimes();
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }
  
  @Test
  public void test_GET_missingName() throws Exception {
    classUnderTest.setOperation(RouteCommand.GET);
    
    expect(route.getName()).andReturn("").anyTimes();
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(false, result);
  }
  
  @Test
  public void test_LIST() throws Exception {
    classUnderTest.setOperation(RouteCommand.LIST);
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }

  @Test
  public void test_LIST_TYPES() throws Exception {
    classUnderTest.setOperation(RouteCommand.LIST_TYPES);
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  } 
  
  @Test
  public void test_CREATE_ROUTE_TEMPLATE() throws Exception {
    classUnderTest.setOperation(RouteCommand.CREATE_ROUTE_TEMPLATE);
    classUnderTest.setTemplateRouteType("acrr-route");
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }  
  
  @Test
  public void test_CREATE_ROUTE_TEMPLATE_missingType() throws Exception {
    classUnderTest.setOperation(RouteCommand.CREATE_ROUTE_TEMPLATE);
    //classUnderTest.setTemplateRouteType("acrr-route");
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(false, result);
  } 
}
