/**
 * 
 */
package eu.baltrad.beast.admin.command;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.admin.objects.Adaptor;

/**
 * @author anders
 *
 */
public class AdaptorCommandTest extends EasyMockSupport {
  private AdaptorCommand classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new AdaptorCommand();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void validate_ADD() throws Exception {
    classUnderTest.setOperation(AdaptorCommand.ADD);
    classUnderTest.setAdaptor(new Adaptor("R1", "XMLRPC", "http://localhost:1234/RAVE", 5000));
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }

  @Test
  public void validate_ADD_missingName() throws Exception {
    classUnderTest.setOperation(AdaptorCommand.ADD);
    classUnderTest.setAdaptor(new Adaptor("", "XMLRPC", "http://localhost:1234/RAVE", 5000));
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(false, result);
  }

  @Test
  public void validate_ADD_missingName_2() throws Exception {
    classUnderTest.setOperation(AdaptorCommand.ADD);
    classUnderTest.setAdaptor(new Adaptor(null, "XMLRPC", "http://localhost:1234/RAVE", 5000));
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(false, result);
  }

  @Test
  public void validate_REMOVE() throws Exception {
    classUnderTest.setOperation(AdaptorCommand.REMOVE);
    classUnderTest.setAdaptor(new Adaptor("R1"));
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }
  
  @Test
  public void validate_REMOVE_missingName() throws Exception {
    classUnderTest.setOperation(AdaptorCommand.REMOVE);
    classUnderTest.setAdaptor(new Adaptor(""));
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(false, result);
  }
  
  @Test
  public void validate_LIST() throws Exception {
    classUnderTest.setOperation(AdaptorCommand.LIST);
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }

}
