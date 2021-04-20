/**
 * 
 */
package eu.baltrad.beast.admin.command;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.admin.objects.User;

/**
 * @author anders
 *
 */
public class UserCommandTest extends EasyMockSupport {
  private UserCommand classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = null;
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void validate_CHANGE_PASSWORD() throws Exception {
    User user = new User("A", User.ROLE_ADMIN);
    user.setPassword("P1");
    user.setNewpassword("P2");

    classUnderTest = new UserCommand(UserCommand.CHANGE_PASSWORD, user);
    
    boolean result = classUnderTest.validate();

    assertEquals(true, result);
  }

  @Test
  public void validate_CHANGE_PASSWORD_noUser() throws Exception {

    classUnderTest = new UserCommand(UserCommand.CHANGE_PASSWORD);
    
    boolean result = classUnderTest.validate();

    assertEquals(false, result);
  }

  
  @Test
  public void validate_CHANGE_PASSWORD_noNewPassword() throws Exception {
    User user = new User("A", User.ROLE_ADMIN);
    user.setPassword("P1");
    //user.setNewpassword("P2");

    classUnderTest = new UserCommand(UserCommand.CHANGE_PASSWORD, user);
    
    boolean result = classUnderTest.validate();

    assertEquals(false, result);
  }

  @Test
  public void validate_LIST() throws Exception {
    classUnderTest = new UserCommand(UserCommand.LIST);
    
    boolean result = classUnderTest.validate();

    assertEquals(true, result);
  }

}
