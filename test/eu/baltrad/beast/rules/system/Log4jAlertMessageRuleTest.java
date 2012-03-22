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

package eu.baltrad.beast.rules.system;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.log4j.Logger;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltAlertMessage;

/**
 * @author Anders Henja
 * @date Dec 19, 2011
 */
public class Log4jAlertMessageRuleTest extends EasyMockSupport {
  interface Methods {
    public Logger getLogger(String logname);
  };
  
  private Logger logger = null;
  private Methods methods = null;
  private Log4jAlertMessageRule classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    logger = createMock(Logger.class);
    methods = createMock(Methods.class);
    classUnderTest = new Log4jAlertMessageRule() {
      protected Logger getLogger(String logname) {
        return methods.getLogger(logname);
      }
    };
  }

  @After
  public void tearDown() throws Exception {
    logger = null;
    methods = null;
    classUnderTest = null;
  }

  @Test
  public void testGetType() throws Exception {
    String result = classUnderTest.getType();
    assertEquals("log4j_system_alert", result);
  }
  
  @Test
  public void testIsValid() throws Exception {
    boolean result = classUnderTest.isValid();
    assertEquals(true, result);
  }

  @Test
  public void testHandle_notAlertMessage() throws Exception {
    IBltMessage msg = new IBltMessage() { };
    
    replayAll();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verifyAll();
    assertNull(result);
  }
  
  @Test
  public void testHandle_noSeverity() throws Exception {
    BltAlertMessage msg = new BltAlertMessage();
    msg.setSeverity(null);
    msg.setCode("00001");
    msg.setModule("MYMODULE");
    msg.setMessage("We are having problems");
    
    
    expect(methods.getLogger("MYMODULE")).andReturn(logger);
    logger.info("I00001: We are having problems");
    
    replayAll();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verifyAll();
    assertNull(result);
  }

  @Test
  public void testHandle_info() throws Exception {
    BltAlertMessage msg = new BltAlertMessage();
    msg.setSeverity(BltAlertMessage.INFO);
    msg.setCode("00001");
    msg.setMessage("We are having problems");
    msg.setModule("MYMODULE");
    
    expect(methods.getLogger("MYMODULE")).andReturn(logger);
    logger.info("I00001: We are having problems");
    
    replayAll();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verifyAll();
    assertNull(result);
  }

  @Test
  public void testHandle_warning() throws Exception {
    BltAlertMessage msg = new BltAlertMessage();
    msg.setSeverity(BltAlertMessage.WARNING);
    msg.setCode("00001");
    msg.setMessage("We are having problems");
    msg.setModule("MYMODULE");
    
    expect(methods.getLogger("MYMODULE")).andReturn(logger);
    logger.warn("W00001: We are having problems");
    
    replayAll();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verifyAll();
    assertNull(result);
  }

  @Test
  public void testHandle_error() throws Exception {
    BltAlertMessage msg = new BltAlertMessage();
    msg.setSeverity(BltAlertMessage.ERROR);
    msg.setCode("00001");
    msg.setMessage("We are having problems");
    msg.setModule("MYMODULE");
    
    expect(methods.getLogger("MYMODULE")).andReturn(logger);
    logger.error("E00001: We are having problems");
    
    replayAll();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verifyAll();
    assertNull(result);
  }

  @Test
  public void testHandle_fatal() throws Exception {
    BltAlertMessage msg = new BltAlertMessage();
    msg.setSeverity(BltAlertMessage.FATAL);
    msg.setCode("00001");
    msg.setMessage("We are having problems");
    msg.setModule("MYMODULE");
    
    expect(methods.getLogger("MYMODULE")).andReturn(logger);
    logger.fatal("F00001: We are having problems");
    
    replayAll();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verifyAll();
    assertNull(result);
  }

  @Test
  public void testHandle_unknownSeverity() throws Exception {
    BltAlertMessage msg = new BltAlertMessage();
    msg.setSeverity("NISSE");
    msg.setCode("00001");
    msg.setMessage("We are having problems");
    msg.setModule("MYMODULE");
    
    expect(methods.getLogger("MYMODULE")).andReturn(logger);
    logger.fatal("X00001: We are having problems");
    
    replayAll();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verifyAll();
    assertNull(result);
  }
}
