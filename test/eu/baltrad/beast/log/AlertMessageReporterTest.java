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

import org.easymock.MockControl;

import eu.baltrad.beast.manager.IBltMessageManager;
import eu.baltrad.beast.message.mo.BltAlertMessage;

/**
 *
 * @author Anders Henja
 * @date Dec 20, 2011
 */
public class AlertMessageReporterTest extends TestCase {
  interface Methods {
    public BltAlertMessage createAlert(String module, String severity, String code, String message);
    public String getMessage(String module, String code, String message, Object... args);
  };

  private MockControl repositoryControl = null;
  private ILogMessageRepository repository = null;
  private MockControl methodsControl = null;
  private Methods methods = null;
  private MockControl managerControl = null;
  private IBltMessageManager manager = null;
  private AlertMessageReporter classUnderTest = null;
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    repositoryControl = MockControl.createControl(ILogMessageRepository.class);
    repository = (ILogMessageRepository)repositoryControl.getMock();
    methodsControl = MockControl.createControl(Methods.class);
    methods = (Methods)methodsControl.getMock();
    managerControl = MockControl.createControl(IBltMessageManager.class);
    manager = (IBltMessageManager)managerControl.getMock();
    
    classUnderTest = new AlertMessageReporter() {
      protected String getMessage(String module, String code, String message, Object... args) {
        return methods.getMessage(module, code, message, args);
      }
      protected BltAlertMessage createAlert(String module, String severity, String code, String message) {
        return methods.createAlert(module, severity, code, message);
      }
    };
    classUnderTest.setMessageRepository(repository);
    classUnderTest.setMessageManager(manager);
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryControl = null;
    repository = null;
    methodsControl = null;
    methods = null;
    classUnderTest = null;
  }
  
  protected void replay() {
    repositoryControl.replay();
    methodsControl.replay();
    managerControl.replay();
  }
  
  protected void verify() {
    repositoryControl.verify();
    methodsControl.verify();
    managerControl.verify();
  }

  public void testInfo_onlymessage() throws Exception {
    BltAlertMessage bltmsg = new BltAlertMessage();
    
    methods.createAlert("BEAST", "INFO", "XXXXX", "We got a message");
    methodsControl.setReturnValue(bltmsg);
    manager.manage(bltmsg);
    
    replay();
    
    classUnderTest.info("We got a message");
    
    verify();
  }
  
  public void testInfo() throws Exception {
    BltAlertMessage bltmsg = new BltAlertMessage();
    Object[] args = new Object[]{"a"};
    
    methods.getMessage("BEAST", "00001", "We got a message %s", args);
    methodsControl.setReturnValue("We got a message a");
    methods.createAlert("BEAST", "INFO", "00001", "We got a message a");
    methodsControl.setReturnValue(bltmsg);
    manager.manage(bltmsg);
    
    replay();
    
    classUnderTest.info("00001", "We got a message %s", args);
    
    verify();
  }

  public void testWarning() throws Exception {
    BltAlertMessage bltmsg = new BltAlertMessage();
    Object[] args = new Object[]{"a"};
    
    methods.getMessage("BEAST", "00001", "We got a message %s", args);
    methodsControl.setReturnValue("We got a message a");
    methods.createAlert("BEAST", "WARNING", "00001", "We got a message a");
    methodsControl.setReturnValue(bltmsg);
    manager.manage(bltmsg);
    
    replay();
    
    classUnderTest.warn("00001", "We got a message %s", args);
    
    verify();
  }
 
  public void testError() throws Exception {
    BltAlertMessage bltmsg = new BltAlertMessage();
    Object[] args = new Object[]{"a"};
    
    methods.getMessage("BEAST", "00001", "We got a message %s", args);
    methodsControl.setReturnValue("We got a message a");
    methods.createAlert("BEAST", "ERROR", "00001", "We got a message a");
    methodsControl.setReturnValue(bltmsg);
    manager.manage(bltmsg);
    
    replay();
    
    classUnderTest.error("00001", "We got a message %s", args);
    
    verify();
  }  

  public void testFatal() throws Exception {
    BltAlertMessage bltmsg = new BltAlertMessage();
    Object[] args = new Object[]{"a"};
    
    methods.getMessage("BEAST", "00001", "We got a message %s", args);
    methodsControl.setReturnValue("We got a message a");
    methods.createAlert("BEAST", "FATAL", "00001", "We got a message a");
    methodsControl.setReturnValue(bltmsg);
    manager.manage(bltmsg);
    
    replay();
    
    classUnderTest.fatal("00001", "We got a message %s", args);
    
    verify();
  }
  
  public void testCreateMessage() throws Exception {
    classUnderTest = new AlertMessageReporter();
    
    replay();
    
    BltAlertMessage result = classUnderTest.createAlert("NISSE",BltAlertMessage.INFO, "00001", "we got something");
    
    verify();
    
    assertEquals("NISSE", result.getModule());
    assertEquals(BltAlertMessage.INFO, result.getSeverity());
    assertEquals("00001", result.getCode());
    assertEquals("we got something", result.getMessage());
  }
   
  
  public void testGetMessage() throws Exception {
    Object[] args = new Object[]{"is"};
    repository.getMessage("MODULE1", "00001", "This %s ok", args);
    repositoryControl.setReturnValue("This is ok");

    replay();

    classUnderTest = new AlertMessageReporter();
    classUnderTest.setMessageRepository(repository);
    
    String result = classUnderTest.getMessage("MODULE1", "00001", "This %s ok", args);
    
    verify();
    
    assertEquals("This is ok", result);
  }
  
  public void testGetMessage_nullRepository() throws Exception {
    Object[] args = new Object[]{"is"};

    replay();

    classUnderTest = new AlertMessageReporter();
    classUnderTest.setMessageRepository(null);
    
    String result = classUnderTest.getMessage("MODULE1", "00001", "This %s ok", args);
    
    verify();
    
    assertEquals("This is ok", result);
  }
}
