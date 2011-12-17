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

import org.apache.log4j.Logger;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import junit.framework.TestCase;

/**
 *
 * @author Anders Henja
 * @date Dec 17, 2011
 */
public class BeastReporterTest extends TestCase {
  interface Methods {
    public String getMessage(String code, String message, Object... args);
  }
  
  private BeastReporter classUnderTest = null;
  private MockControl loggerControl = null;
  private Logger logger = null;
  private MockControl repositoryControl = null;
  private ILogMessageRepository repository = null;
  private MockControl methodsControl = null;
  private Methods methods = null;
  
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
    
    classUnderTest = new BeastReporter() {
      protected String getMessage(String code, String message, Object... args) {
        return methods.getMessage(code, message, args);
      }
    };
    classUnderTest.logger = logger;
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
  
  public void testInfo_onlyMessage() throws Exception {
    logger.info("IXXXXX: My message");
    
    replay();
    
    classUnderTest.info("My message");
    
    verify();
  }
  
  public void testInfo() throws Exception {
    Object[] args = new Object[0];
    methods.getMessage("00032", "my message", args);
    methodsControl.setReturnValue("MY message");
    logger.info("I00032: MY message");
    
    replay();
    
    classUnderTest.info("00032", "my message", args);
    
    verify();
  }

  public void testWarning() throws Exception {
    Object[] args = new Object[0];
    methods.getMessage("00032", "my message", args);
    methodsControl.setReturnValue("MY message");
    logger.warn("W00032: MY message");
    
    replay();
    
    classUnderTest.warn("00032", "my message", args);
    
    verify();
  }

  public void testError() throws Exception {
    Object[] args = new Object[0];
    methods.getMessage("00032", "my message", args);
    methodsControl.setReturnValue("MY message");
    logger.error("E00032: MY message");
    
    replay();
    
    classUnderTest.error("00032", "my message", args);
    
    verify();
  }

  public void testFatal() throws Exception {
    Object[] args = new Object[0];
    methods.getMessage("00032", "my message", args);
    methodsControl.setReturnValue("MY message");
    logger.fatal("F00032: MY message");
    
    replay();
    
    classUnderTest.fatal("00032", "my message", args);
    
    verify();
  }
  
  public void testGetMessage() throws Exception {
    LogMessage msg = new LogMessage("00032", "Message");
    
    classUnderTest = new BeastReporter();
    classUnderTest.logger = logger;
    classUnderTest.setRepository(repository);    
    
    repository.getMessage("BEAST", "00032");
    repositoryControl.setReturnValue(msg);
    
    replay();
    
    String result = classUnderTest.getMessage("00032", "My message");
    
    verify();
    assertEquals("Message", result);
  }

  public void testGetMessage_notFound() throws Exception {
    classUnderTest = new BeastReporter();
    classUnderTest.logger = logger;
    classUnderTest.setRepository(repository);
    
    repository.getMessage("BEAST", "00032");
    repositoryControl.setReturnValue(null);
    
    replay();
    
    String result = classUnderTest.getMessage("00032", "My message");
    
    verify();
    assertEquals("My message", result);
  }

  public void testGetMessage_noRepository() throws Exception {
    classUnderTest = new BeastReporter();
    classUnderTest.logger = logger;
    
    replay();
    
    String result = classUnderTest.getMessage("00032", "My message");
    
    verify();
    assertEquals("My message", result);
  }  
}
