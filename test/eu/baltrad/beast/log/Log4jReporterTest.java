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

import org.apache.log4j.Logger;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Anders Henja
 * @date Dec 17, 2011
 */
public class Log4jReporterTest extends EasyMockSupport {
  interface Methods {
    public String getMessage(String code, String message, Object... args);
  }
  
  private Log4jReporter classUnderTest = null;
  private Logger logger = null;
  private ILogMessageRepository repository = null;
  private Methods methods = null;
  
  @Before
  public void setUp() throws Exception {
    logger = createMock(Logger.class);
    repository = createMock(ILogMessageRepository.class);
    methods = createMock(Methods.class);
    
    classUnderTest = new Log4jReporter() {
      protected String getMessage(String code, String message, Object... args) {
        return methods.getMessage(code, message, args);
      }
    };
    classUnderTest.logger = logger;
    classUnderTest.setRepository(repository);
  }

  @After
  public void tearDown() throws Exception {
    logger = null;
    repository = null;
    methods = null;
    classUnderTest = null;
  }

  @Test
  public void testInfo_onlyMessage() throws Exception {
    logger.info("IXXXXX: My message");
    
    replayAll();
    
    classUnderTest.info("My message");
    
    verifyAll();
  }

  @Test
  public void testInfo() throws Exception {
    Object[] args = new Object[0];
    expect(methods.getMessage("00032", "my message", args)).andReturn("MY message");
    logger.info("I00032: MY message");
    
    replayAll();
    
    classUnderTest.info("00032", "my message", args);
    
    verifyAll();
  }

  @Test
  public void testWarning() throws Exception {
    Object[] args = new Object[0];
    expect(methods.getMessage("00032", "my message", args)).andReturn("MY message");
    logger.warn("W00032: MY message");
    
    replayAll();
    
    classUnderTest.warn("00032", "my message", args);
    
    verifyAll();
  }

  @Test
  public void testError() throws Exception {
    Object[] args = new Object[0];
    expect(methods.getMessage("00032", "my message", args)).andReturn("MY message");
    logger.error("E00032: MY message");
    
    replayAll();
    
    classUnderTest.error("00032", "my message", args);
    
    verifyAll();
  }

  @Test
  public void testFatal() throws Exception {
    Object[] args = new Object[0];
    expect(methods.getMessage("00032", "my message", args)).andReturn("MY message");
    logger.fatal("F00032: MY message");
    
    replayAll();
    
    classUnderTest.fatal("00032", "my message", args);
    
    verifyAll();
  }
  
  @Test
  public void testGetMessage() throws Exception {
    LogMessage msg = new LogMessage("00032", "Message");
    
    classUnderTest = new Log4jReporter();
    classUnderTest.logger = logger;
    classUnderTest.setRepository(repository);    
    
    expect(repository.getMessage("BEAST", "00032")).andReturn(msg);
    
    replayAll();
    
    String result = classUnderTest.getMessage("00032", "My message");
    
    verifyAll();
    assertEquals("Message", result);
  }

  @Test
  public void testGetMessage_notFound() throws Exception {
    classUnderTest = new Log4jReporter();
    classUnderTest.logger = logger;
    classUnderTest.setRepository(repository);
    
    expect(repository.getMessage("BEAST", "00032")).andReturn(null);
    
    replayAll();
    
    String result = classUnderTest.getMessage("00032", "My message");
    
    verifyAll();
    assertEquals("My message", result);
  }

  @Test
  public void testGetMessage_noRepository() throws Exception {
    classUnderTest = new Log4jReporter();
    classUnderTest.logger = logger;
    
    replayAll();
    
    String result = classUnderTest.getMessage("00032", "My message");
    
    verifyAll();
    assertEquals("My message", result);
  }  
}
