/* --------------------------------------------------------------------
Copyright (C) 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

This file is part of the Beast library.

Beast library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Beast library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with the Beast library library.  If not, see <http://www.gnu.org/licenses/>.
------------------------------------------------------------------------*/

package eu.baltrad.beast.log;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import eu.baltrad.beast.log.AlertSystemMessageRule;
import eu.baltrad.beast.log.ILogMessageRepository;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltAlertMessage;

/**
 *
 * @author Anders Henja
 * @date Dec 19, 2011
 */
public class AlertSystemMessageRuleTest extends TestCase {
  interface Methods {
    public Logger getLogger(String logname);
    public String getMessage(String module, String code, String message, Object... args);
  };
  
  private MockControl loggerControl = null;
  private Logger logger = null;
  private MockControl repositoryControl = null;
  private ILogMessageRepository repository = null;
  private MockControl methodsControl = null;
  private Methods methods = null;
  private AlertSystemMessageRule classUnderTest = null;
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    loggerControl = MockClassControl.createControl(Logger.class);
    logger = (Logger)loggerControl.getMock();
    repositoryControl = MockControl.createControl(ILogMessageRepository.class);
    repository = (ILogMessageRepository)repositoryControl.getMock();
    methodsControl = MockControl.createControl(Methods.class);
    methods = (Methods)methodsControl.getMock();
    classUnderTest = new AlertSystemMessageRule() {
      protected Logger getLogger(String logname) {
        return methods.getLogger(logname);
      }
      protected String getMessage(String module, String code, String message, Object... args) {
        return methods.getMessage(module, code, message, args);
      }
    };
    classUnderTest.setRepository(repository);
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
    loggerControl = null;
    logger = null;
    repositoryControl = null;
    repository = null;
    methodsControl = null;
    methods = null;
    classUnderTest = null;
  }

  protected void replay() throws Exception {
    loggerControl.replay();
    repositoryControl.replay();
    methodsControl.replay();
  }
  
  protected void verify() throws Exception {
    loggerControl.verify();
    repositoryControl.verify();
    methodsControl.verify();
  }  
  
  public void testGetType() throws Exception {
    String result = classUnderTest.getType();
    assertEquals("log4j_system_alert", result);
  }
  
  public void testIsValid() throws Exception {
    boolean result = classUnderTest.isValid();
    assertEquals(true, result);
  }

  public void testHandle_notAlertMessage() throws Exception {
    IBltMessage msg = new IBltMessage() { };
    
    replay();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verify();
    assertNull(result);
  }
  
  public void testHandle_noSeverity() throws Exception {
    BltAlertMessage msg = new BltAlertMessage();
    msg.setSeverity(null);
    msg.setCode("00001");
    msg.setModule("MYMODULE");
    msg.setMessage("We are having problems");
    
    methods.getMessage("MYMODULE", "00001", "We are having problems");
    methodsControl.setMatcher(MockControl.ARRAY_MATCHER);
    methodsControl.setReturnValue("We have huge problems");
    methods.getLogger("MYMODULE");
    methodsControl.setReturnValue(logger);
    logger.info("I00001: We have huge problems");
    
    replay();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verify();
    assertNull(result);
  }

  public void testHandle_info() throws Exception {
    BltAlertMessage msg = new BltAlertMessage();
    msg.setSeverity(BltAlertMessage.INFO);
    msg.setCode("00001");
    msg.setMessage("We are having problems");
    msg.setModule("MYMODULE");
    
    methods.getMessage("MYMODULE", "00001", "We are having problems");
    methodsControl.setMatcher(MockControl.ARRAY_MATCHER);
    methodsControl.setReturnValue("We have huge problems");
    methods.getLogger("MYMODULE");
    methodsControl.setReturnValue(logger);
    logger.info("I00001: We have huge problems");
    
    replay();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verify();
    assertNull(result);
  }

  public void testHandle_warning() throws Exception {
    BltAlertMessage msg = new BltAlertMessage();
    msg.setSeverity(BltAlertMessage.WARNING);
    msg.setCode("00001");
    msg.setMessage("We are having problems");
    msg.setModule("MYMODULE");
    
    methods.getMessage("MYMODULE", "00001", "We are having problems");
    methodsControl.setMatcher(MockControl.ARRAY_MATCHER);
    methodsControl.setReturnValue("We have huge problems");
    methods.getLogger("MYMODULE");
    methodsControl.setReturnValue(logger);
    logger.warn("W00001: We have huge problems");
    
    replay();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verify();
    assertNull(result);
  }

  public void testHandle_error() throws Exception {
    BltAlertMessage msg = new BltAlertMessage();
    msg.setSeverity(BltAlertMessage.ERROR);
    msg.setCode("00001");
    msg.setMessage("We are having problems");
    msg.setModule("MYMODULE");
    
    methods.getMessage("MYMODULE", "00001", "We are having problems");
    methodsControl.setMatcher(MockControl.ARRAY_MATCHER);
    methodsControl.setReturnValue("We have huge problems");
    methods.getLogger("MYMODULE");
    methodsControl.setReturnValue(logger);
    logger.error("E00001: We have huge problems");
    
    replay();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verify();
    assertNull(result);
  }
  
  public void testHandle_fatal() throws Exception {
    BltAlertMessage msg = new BltAlertMessage();
    msg.setSeverity(BltAlertMessage.FATAL);
    msg.setCode("00001");
    msg.setMessage("We are having problems");
    msg.setModule("MYMODULE");
    
    methods.getMessage("MYMODULE", "00001", "We are having problems");
    methodsControl.setMatcher(MockControl.ARRAY_MATCHER);
    methodsControl.setReturnValue("We have huge problems");
    methods.getLogger("MYMODULE");
    methodsControl.setReturnValue(logger);
    logger.fatal("F00001: We have huge problems");
    
    replay();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verify();
    assertNull(result);
  }

  public void testHandle_unknownSeverity() throws Exception {
    BltAlertMessage msg = new BltAlertMessage();
    msg.setSeverity("NISSE");
    msg.setCode("00001");
    msg.setMessage("We are having problems");
    msg.setModule("MYMODULE");
    
    methods.getMessage("MYMODULE", "00001", "We are having problems");
    methodsControl.setMatcher(MockControl.ARRAY_MATCHER);
    methodsControl.setReturnValue("We have huge problems");
    methods.getLogger("MYMODULE");
    methodsControl.setReturnValue(logger);
    logger.fatal("X00001: We have huge problems");
    
    replay();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verify();
    assertNull(result);
  }
  
  public void testGetMessage() throws Exception {
    Object[] args = new Object[]{"is"};
    repository.getMessage("MODULE1", "00001", "This %s ok", args);
    repositoryControl.setReturnValue("This is ok");

    replay();

    classUnderTest = new AlertSystemMessageRule();
    classUnderTest.setRepository(repository);
    
    String result = classUnderTest.getMessage("MODULE1", "00001", "This %s ok", args);
    
    verify();
    
    assertEquals("This is ok", result);
  }
  
  public void testGetMessage_nullRepository() throws Exception {
    Object[] args = new Object[]{"is"};

    replay();

    classUnderTest = new AlertSystemMessageRule();
    classUnderTest.setRepository(null);
    
    String result = classUnderTest.getMessage("MODULE1", "00001", "This %s ok", args);
    
    verify();
    
    assertEquals("This is ok", result);
  }
}
