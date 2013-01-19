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
import junit.framework.Assert;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.log.message.ILogMessageRepository;
import eu.baltrad.beast.log.message.LogMessage;
import eu.baltrad.beast.log.message.MessageSeverity;
import eu.baltrad.beast.manager.IBltMessageManager;
import eu.baltrad.beast.message.mo.BltAlertMessage;

/**
 *
 * @author Anders Henja
 * @date Dec 20, 2011
 */
public class AlertMessageReporterTest extends EasyMockSupport {
  interface Methods {
    public BltAlertMessage createAlert(String severity, String code, String message, Object... args);
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
      protected BltAlertMessage createAlert(String severity, String code, String message, Object... args) {
        return methods.createAlert(severity, code, message, args);
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
  public void testInfo() throws Exception {
    BltAlertMessage bltmsg = new BltAlertMessage();
    
    expect(methods.createAlert(BltAlertMessage.INFO, "00001", "We got a message for %s", "nisse")).andReturn(bltmsg);
    
    manager.manage(bltmsg);
    
    replayAll();
    
    classUnderTest.info("00001", "We got a message for %s", "nisse");
    
    verifyAll();
  }

  @Test
  public void testWarning() throws Exception {
    BltAlertMessage bltmsg = new BltAlertMessage();
    
    expect(methods.createAlert(BltAlertMessage.WARNING, "00001", "We got a message for %s", "nisse")).andReturn(bltmsg);
    
    manager.manage(bltmsg);
    
    replayAll();
    
    classUnderTest.warn("00001", "We got a message for %s", "nisse");
    
    verifyAll();
  }
 
  @Test
  public void testError() throws Exception {
    BltAlertMessage bltmsg = new BltAlertMessage();
    
    expect(methods.createAlert(BltAlertMessage.ERROR, "00001", "We got a message for %s", "nisse")).andReturn(bltmsg);
    
    manager.manage(bltmsg);
    
    replayAll();
    
    classUnderTest.error("00001", "We got a message for %s", "nisse");
    
    verifyAll();    
  }  

  @Test
  public void testFatal() throws Exception {
    BltAlertMessage bltmsg = new BltAlertMessage();
    
    expect(methods.createAlert(BltAlertMessage.FATAL, "00001", "We got a message for %s", "nisse")).andReturn(bltmsg);
    
    manager.manage(bltmsg);
    
    replayAll();
    
    classUnderTest.fatal("00001", "We got a message for %s", "nisse");
    
    verifyAll();     
  }
  
  @Test
  public void testCreateAlert() throws Exception {
    LogMessage lmsg = new LogMessage("MYMODULE", "00001", "another message for %s", MessageSeverity.ERROR);
    classUnderTest = new AlertMessageReporter();
    classUnderTest.setMessageRepository(repository);
    classUnderTest.setMessageManager(manager);
    
    expect(repository.getMessage("00001")).andReturn(lmsg);
    
    replayAll();
    
    BltAlertMessage result = classUnderTest.createAlert(BltAlertMessage.WARNING, "00001", "a message for %s", "nisse");
    
    verifyAll();
    
    Assert.assertEquals(BltAlertMessage.ERROR, result.getSeverity());
    Assert.assertEquals("00001", result.getCode());
    Assert.assertEquals("MYMODULE", result.getModule());
    Assert.assertEquals("another message for nisse", result.getMessage());
  }
  
  @Test
  public void testCreateAlert_noRepository() throws Exception {
    classUnderTest = new AlertMessageReporter();
    classUnderTest.setMessageManager(manager);
    
    replayAll();
    
    BltAlertMessage result = classUnderTest.createAlert(BltAlertMessage.ERROR, "00001", "a message for %s", "nisse");
    
    verifyAll();
    
    Assert.assertEquals(BltAlertMessage.ERROR, result.getSeverity());
    Assert.assertEquals("00001", result.getCode());
    Assert.assertEquals("BEAST", result.getModule());
    Assert.assertEquals("a message for nisse", result.getMessage());
  }
  
  @Test
  public void testCreateAlert_noSuchCode() throws Exception {
    
    classUnderTest = new AlertMessageReporter();
    classUnderTest.setMessageRepository(repository);
    classUnderTest.setMessageManager(manager);
    
    expect(repository.getMessage("00001")).andReturn(null);

    replayAll();
    
    BltAlertMessage result = classUnderTest.createAlert(BltAlertMessage.ERROR, "00001", "a message for %s", "nisse");
    
    verifyAll();
    
    Assert.assertEquals(BltAlertMessage.ERROR, result.getSeverity());
    Assert.assertEquals("00001", result.getCode());
    Assert.assertEquals("BEAST", result.getModule());
    Assert.assertEquals("a message for nisse", result.getMessage());
  }
  
  @Test
  public void testCreateAlert_noSeverityInMessage() throws Exception {
    LogMessage lmsg = new LogMessage("MYMODULE", "00001", "another message for %s");
    classUnderTest = new AlertMessageReporter();
    classUnderTest.setMessageRepository(repository);
    classUnderTest.setMessageManager(manager);
    
    expect(repository.getMessage("00001")).andReturn(lmsg);
    
    replayAll();
    
    BltAlertMessage result = classUnderTest.createAlert(BltAlertMessage.WARNING, "00001", "a message for %s", "nisse");
    
    verifyAll();
    
    Assert.assertEquals(BltAlertMessage.WARNING, result.getSeverity());
    Assert.assertEquals("00001", result.getCode());
    Assert.assertEquals("MYMODULE", result.getModule());
    Assert.assertEquals("another message for nisse", result.getMessage());
  }
}
