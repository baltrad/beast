/**
 * 
 */
package eu.baltrad.beast.admin.command;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author anders
 *
 */
public class HelpCommandTest  extends EasyMockSupport {
  private HelpCommand classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new HelpCommand();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  @Test
  public void validate_HELP() throws Exception {
    classUnderTest.setOperation(HelpCommand.HELP);
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }

  @Test
  public void validate_badOperation() throws Exception {
    classUnderTest.setOperation("SOMEOP");
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(false, result);
  }

  @Test
  public void validate_HELP_with_command() throws Exception {
    classUnderTest.setOperation(HelpCommand.HELP);
    classUnderTest.setCommand("a_command");
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }

}
