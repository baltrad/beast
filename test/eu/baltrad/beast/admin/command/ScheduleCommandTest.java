/**
 * 
 */
package eu.baltrad.beast.admin.command;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.scheduler.CronEntry;

import static org.junit.Assert.assertEquals;

/**
 * @author anders
 *
 */
public class ScheduleCommandTest extends EasyMockSupport {
  private ScheduleCommand classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = null;
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void validate_ADD() throws Exception {
    classUnderTest = new ScheduleCommand(ScheduleCommand.ADD, new CronEntry("* * * ? * *", "ROUTE"));
    
    boolean result = classUnderTest.validate();

    assertEquals(true, result);
  }

  @Test
  public void validate_ADD_2() throws Exception {
    classUnderTest = new ScheduleCommand(ScheduleCommand.ADD, new CronEntry("0 1/2 * ? * *", "ROUTE"));
    
    boolean result = classUnderTest.validate();

    assertEquals(true, result);
  }

  @Test
  public void validate_ADD_badExpression() throws Exception {
    classUnderTest = new ScheduleCommand(ScheduleCommand.ADD, new CronEntry("1 ", "ROUTE"));
    
    boolean result = classUnderTest.validate();

    assertEquals(false, result);
  }

  @Test
  public void validate_ADD_missingRoute() throws Exception {
    classUnderTest = new ScheduleCommand(ScheduleCommand.ADD, new CronEntry("* * * ? * *", ""));
    
    boolean result = classUnderTest.validate();

    assertEquals(false, result);
  }

  @Test
  public void validate_UPDATE() throws Exception {
    classUnderTest = new ScheduleCommand(ScheduleCommand.UPDATE, new CronEntry(1, "* * * ? * *", "ROUTE"));
    
    boolean result = classUnderTest.validate();

    assertEquals(true, result);
  }

  @Test
  public void validate_UPDATE_badIdentifier() throws Exception {
    classUnderTest = new ScheduleCommand(ScheduleCommand.UPDATE, new CronEntry(0, "* * * ? * *", "ROUTE"));
    
    boolean result = classUnderTest.validate();

    assertEquals(false, result);
  }

  @Test
  public void validate_REMOVE() throws Exception {
    classUnderTest = new ScheduleCommand(ScheduleCommand.REMOVE, new CronEntry(1, null, null));
    
    boolean result = classUnderTest.validate();

    assertEquals(true, result);
  }

  @Test
  public void validate_REMOVE_badIdentifier() throws Exception {
    classUnderTest = new ScheduleCommand(ScheduleCommand.REMOVE, new CronEntry(0, null, null));
    
    boolean result = classUnderTest.validate();

    assertEquals(false, result);
  }

  @Test
  public void validate_GET() throws Exception {
    classUnderTest = new ScheduleCommand(ScheduleCommand.GET, new CronEntry(1, null, null));
    
    boolean result = classUnderTest.validate();

    assertEquals(true, result);
  }

  @Test
  public void validate_GET_name() throws Exception {
    classUnderTest = new ScheduleCommand(ScheduleCommand.GET, new CronEntry(0, "", "ROUTE"));
    
    boolean result = classUnderTest.validate();

    assertEquals(true, result);
  }

  @Test
  public void validate_GET_badIdentifierAndName() throws Exception {
    classUnderTest = new ScheduleCommand(ScheduleCommand.GET);
    
    boolean result = classUnderTest.validate();

    assertEquals(false, result);
  }

  @Test
  public void validate_LIST() throws Exception {
    classUnderTest = new ScheduleCommand(ScheduleCommand.LIST);
    
    boolean result = classUnderTest.validate();

    assertEquals(true, result);
  }

}
