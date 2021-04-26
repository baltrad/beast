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
public class SettingCommandTest extends EasyMockSupport {
  private SettingCommand classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new SettingCommand();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void validate_UPDATE_SETTING() throws Exception {
    classUnderTest = new SettingCommand(SettingCommand.UPDATE_SETTINGS);
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }

  @Test
  public void validate_UPDATE_SETTING_noMap() throws Exception {
    classUnderTest = new SettingCommand(SettingCommand.UPDATE_SETTINGS, null);
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }
}
