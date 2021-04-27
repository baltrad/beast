/**
 * 
 */
package eu.baltrad.beast.admin.command;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.qc.AnomalyDetector;

/**
 * @author anders
 *
 */
public class AnomalyDetectorCommandTest extends EasyMockSupport {
  private AnomalyDetectorCommand classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new AnomalyDetectorCommand();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void validate_ADD() throws Exception {
    classUnderTest.setOperation(AnomalyDetectorCommand.ADD);
    classUnderTest.setAnomalyDetector(new AnomalyDetector("N1", "D1"));
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }

  @Test
  public void validate_ADD_missingName() throws Exception {
    classUnderTest.setOperation(AnomalyDetectorCommand.ADD);
    classUnderTest.setAnomalyDetector(new AnomalyDetector(null, "D1"));
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(false, result);
  }

  @Test
  public void validate_ADD_missingName_2() throws Exception {
    classUnderTest.setOperation(AnomalyDetectorCommand.ADD);
    classUnderTest.setAnomalyDetector(new AnomalyDetector("", "D1"));
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(false, result);
  }

  @Test
  public void validate_REMOVE() throws Exception {
    classUnderTest.setOperation(AnomalyDetectorCommand.REMOVE);
    classUnderTest.setAnomalyDetector(new AnomalyDetector("N1",null));

    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }

  @Test
  public void validate_REMOVE_missingName() throws Exception {
    classUnderTest.setOperation(AnomalyDetectorCommand.REMOVE);
    classUnderTest.setAnomalyDetector(new AnomalyDetector());
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(false, result);
  }

  @Test
  public void validate_REMOVE_missingName_2() throws Exception {
    classUnderTest.setOperation(AnomalyDetectorCommand.REMOVE);
    classUnderTest.setAnomalyDetector(new AnomalyDetector("",null));
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(false, result);
  }

  
  @Test
  public void validate_LIST() throws Exception {
    classUnderTest.setOperation(AnomalyDetectorCommand.LIST);
    
    replayAll();
    
    boolean result = classUnderTest.validate();
    
    verifyAll();
    assertEquals(true, result);
  }
}
