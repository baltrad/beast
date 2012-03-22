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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.manager.IBltMessageManager;
import eu.baltrad.beast.message.mo.BltAlertMessage;

/**
 *
 * @author Anders Henja
 * @date Dec 20, 2011
 */
public class AlertMessageReporterTest extends EasyMockSupport {
  interface Methods {
    public BltAlertMessage createAlert(String module, String severity, String code, String message);
    public String getMessage(String module, String code, String message, Object... args);
  };

  private ILogMessageRepository repository = null;
  private Methods methods = null;
  private IBltMessageManager manager = null;
  private AlertMessageReporter classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    repository = createMock(ILogMessageRepository.class);
    methods = createMock(Methods.class);
    manager = createMock(IBltMessageManager.class);
    
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

  @After
  public void tearDown() throws Exception {
    repository = null;
    methods = null;
    classUnderTest = null;
  }
  
  @Test
  public void testInfo_onlymessage() throws Exception {
    BltAlertMessage bltmsg = new BltAlertMessage();
    
    expect(methods.createAlert("BEAST", "INFO", "XXXXX", "We got a message")).andReturn(bltmsg);
    manager.manage(bltmsg);
    
    replayAll();
    
    classUnderTest.info("We got a message");
    
    verifyAll();
  }
  
  @Test
  public void testInfo() throws Exception {
    BltAlertMessage bltmsg = new BltAlertMessage();
    Object[] args = new Object[]{"a"};
    
    expect(methods.getMessage("BEAST", "00001", "We got a message %s", args)).andReturn("We got a message a");
    expect(methods.createAlert("BEAST", "INFO", "00001", "We got a message a")).andReturn(bltmsg);
    manager.manage(bltmsg);
    
    replayAll();
    
    classUnderTest.info("00001", "We got a message %s", args);
    
    verifyAll();
  }

  @Test
  public void testWarning() throws Exception {
    BltAlertMessage bltmsg = new BltAlertMessage();
    Object[] args = new Object[]{"a"};
    
    expect(methods.getMessage("BEAST", "00001", "We got a message %s", args)).andReturn("We got a message a");
    expect(methods.createAlert("BEAST", "WARNING", "00001", "We got a message a")).andReturn(bltmsg);
    manager.manage(bltmsg);
    
    replayAll();
    
    classUnderTest.warn("00001", "We got a message %s", args);
    
    verifyAll();
  }
 
  @Test
  public void testError() throws Exception {
    BltAlertMessage bltmsg = new BltAlertMessage();
    Object[] args = new Object[]{"a"};
    
    expect(methods.getMessage("BEAST", "00001", "We got a message %s", args)).andReturn("We got a message a");
    expect(methods.createAlert("BEAST", "ERROR", "00001", "We got a message a")).andReturn(bltmsg);
    manager.manage(bltmsg);
    
    replayAll();
    
    classUnderTest.error("00001", "We got a message %s", args);
    
    verifyAll();
  }  

  @Test
  public void testFatal() throws Exception {
    BltAlertMessage bltmsg = new BltAlertMessage();
    Object[] args = new Object[]{"a"};
    
    expect(methods.getMessage("BEAST", "00001", "We got a message %s", args)).andReturn("We got a message a");
    expect(methods.createAlert("BEAST", "FATAL", "00001", "We got a message a")).andReturn(bltmsg);
    manager.manage(bltmsg);
    
    replayAll();
    
    classUnderTest.fatal("00001", "We got a message %s", args);
    
    verifyAll();
  }
  
  @Test
  public void testCreateMessage() throws Exception {
    classUnderTest = new AlertMessageReporter();
    
    replayAll();
    
    BltAlertMessage result = classUnderTest.createAlert("NISSE",BltAlertMessage.INFO, "00001", "we got something");
    
    verifyAll();
    
    assertEquals("NISSE", result.getModule());
    assertEquals(BltAlertMessage.INFO, result.getSeverity());
    assertEquals("00001", result.getCode());
    assertEquals("we got something", result.getMessage());
  }

  @Test
  public void testGetMessage() throws Exception {
    Object[] args = new Object[]{"is"};
    
    expect(repository.getMessage("MODULE1", "00001", "This %s ok", args)).andReturn("This is ok");

    replayAll();

    classUnderTest = new AlertMessageReporter();
    classUnderTest.setMessageRepository(repository);
    
    String result = classUnderTest.getMessage("MODULE1", "00001", "This %s ok", args);
    
    verifyAll();
    
    assertEquals("This is ok", result);
  }
  
  @Test
  public void testGetMessage_nullRepository() throws Exception {
    Object[] args = new Object[]{"is"};

    replayAll();

    classUnderTest = new AlertMessageReporter();
    classUnderTest.setMessageRepository(null);
    
    String result = classUnderTest.getMessage("MODULE1", "00001", "This %s ok", args);
    
    verifyAll();
    
    assertEquals("This is ok", result);
  }
}
