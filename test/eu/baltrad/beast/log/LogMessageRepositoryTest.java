/* --------------------------------------------------------------------
Copyright (C) 2009-2010 Swedish Meteorological and Hydrological Institute, SMHI,

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

import java.io.File;
import java.util.Map;

import org.easymock.MockControl;

import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class LogMessageRepositoryTest extends TestCase {
  interface Methods {
    public void load(String[] filenames);
  }
  
  private static String XML_LOAD_TEST_FIXTURE = "load-test-log-messages.xml";
  private static String XML_LOAD_TEST_FIXTURE2 = "load-test-log-messages2.xml";
  private LogMessageRepository classUnderTest = null;
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    classUnderTest = new LogMessageRepository();
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
    classUnderTest = null;    
  }
  
  public void testFilenames_init() throws Exception {
    String[] result = classUnderTest.getFilenames();
    assertEquals(0, result.length);
  }

  public void testFilenames_setArray() throws Exception {
    String[] arr = new String[]{"A", "B", "C"};
    classUnderTest.setFilenames(arr);
    assertSame(arr, classUnderTest.getFilenames());
  }

  public void testFilenames_setNull() throws Exception {
    classUnderTest.setFilenames(null);
    String[] result = classUnderTest.getFilenames();
    assertEquals(0, result.length);
  }
  
  public void testAdd() throws Exception {
    LogMessage msg = new LogMessage();
    msg.setCode("01234");
    msg.setMessage("my message");
    msg.setSolution("my solution");
    classUnderTest.add("MYMODULE", msg);
    
    LogMessage result = classUnderTest.getMessage("MYMODULE", "01234");
    assertSame(msg, result);
    
    result = classUnderTest.getMessage("ANOTHERMOD", "01234");
    assertNull(result);

    result = classUnderTest.getMessage("MYMODULE", "01235");
    assertNull(result);
  }

  public void testAdd_nullMessage() throws Exception {
    try {
      classUnderTest.add("MYMODULE", null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      //pass
    }
  }

  public void testAdd_nullModule() throws Exception {
    LogMessage msg = new LogMessage();
    msg.setCode("01234");
    msg.setMessage("my message");
    msg.setSolution("my solution");
    try {
      classUnderTest.add(null, msg);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      //pass
    }
  }
  
  public void testGet_noSuchMessage() throws Exception {
    LogMessage result = classUnderTest.getMessage("MYMODULE", "01234");
    assertNull(result);
  }
  
  public void testRemove() throws Exception {
    LogMessage msg = new LogMessage();
    msg.setCode("01234");
    msg.setMessage("my message");
    msg.setSolution("my solution");
    classUnderTest.add("MYMODULE", msg);

    classUnderTest.remove("MYMODULE", "01234");
    LogMessage result = classUnderTest.getMessage("MYMODULE", "01234");
    assertNull(result);
  }

  public void testGetModuleMessages() throws Exception {
    LogMessage m1 = new LogMessage("00001", "Message 1");
    LogMessage m2 = new LogMessage("00002", "Message 1");
    LogMessage m3 = new LogMessage("00003", "Message 1");
    
    classUnderTest.add("MODULE1", m1);
    classUnderTest.add("MODULE1", m2);
    classUnderTest.add("MODULE2", m2);
    classUnderTest.add("MODULE2", m3);

    Map<String,LogMessage> result = classUnderTest.getModuleMessages("MODULE1");
    assertEquals(2, result.size());
    assertSame(m1, result.get("00001"));
    assertSame(m2, result.get("00002"));
    
    result = classUnderTest.getModuleMessages("MODULE2");
    assertEquals(2, result.size());
    assertSame(m2, result.get("00002"));
    assertSame(m3, result.get("00003"));
  }

  public void testGetMessage_args() throws Exception {
    LogMessage m1 = new LogMessage("00001", "Message %s ok");
    LogMessage m2 = new LogMessage("00001", "Message %s nok");

    classUnderTest.add("MODULE1", m1);
    classUnderTest.add("MODULE2", m2);
    
    String result = classUnderTest.getMessage("MODULE1", "00001", "Message %s was ok", "abc");
    assertEquals("Message abc ok", result);
    result = classUnderTest.getMessage("MODULE1", "00002", "Message %s was ok", "abc");
    assertEquals("Message abc was ok", result);
    result = classUnderTest.getMessage("MODULE2", "00001", "Message %s was ok", "abc");
    assertEquals("Message abc nok", result);
    result = classUnderTest.getMessage("MODULE2", "00002", "Message %s was ok", "abc");
    assertEquals("Message abc was ok", result);    
  }
  
  /*
  public String getMessage(String module, String code, String message, Object... args) {
    String msg = null;
    LogMessage logmsg = getMessage(module, code);
    if (logmsg != null) {
      try {
        msg = String.format(logmsg.getMessage(), args);
      } catch (Exception e) {
        // let default message be used instead and that one should not fail
      }
    }
    
    if (msg == null) {
      msg = String.format(message, args);
    }
    
    return msg;
  }*/
  
  public void testLoad() throws Exception {
    File f = new File(this.getClass().getResource(XML_LOAD_TEST_FIXTURE).getFile());
    classUnderTest.load(f.getAbsolutePath());
    LogMessage msg = classUnderTest.getMessage("BEAST", "00001");
    assertEquals("00001", msg.getCode());
    assertEquals("Registered adaptor '%s' of type %s", msg.getMessage());
    assertEquals(null, msg.getSolution());

    msg = classUnderTest.getMessage("BEAST", "00004");
    assertEquals("00004", msg.getCode());
    assertEquals("XMLRPC communication with '%s' FAILED", msg.getMessage());
    assertEquals("Verify that the product generation framework is running", msg.getSolution());

  }

  public void testLoad_manyFiles() throws Exception {
    File f = new File(this.getClass().getResource(XML_LOAD_TEST_FIXTURE).getFile());
    File f2 = new File(this.getClass().getResource(XML_LOAD_TEST_FIXTURE2).getFile());
    
    classUnderTest.load(new String[]{f.getAbsolutePath(), f2.getAbsolutePath()});
    LogMessage msg = classUnderTest.getMessage("BEAST", "00001");
    assertEquals("00001", msg.getCode());
    assertEquals("Registered adaptor '%s' of type %s", msg.getMessage());
    assertEquals(null, msg.getSolution());

    msg = classUnderTest.getMessage("BEAST", "00004");
    assertEquals("00004", msg.getCode());
    assertEquals("XMLRPC communication with '%s' FAILED", msg.getMessage());
    assertEquals("Verify that the product generation framework is running", msg.getSolution());

    msg = classUnderTest.getMessage("OTHER", "00001");
    assertEquals("00001", msg.getCode());
    assertEquals("Something", msg.getMessage());
    assertEquals(null, msg.getSolution());
  }

  public void testLoad_manyFiles_null() throws Exception {
    // Very silly test, but just verify that no exception is thrown
    classUnderTest.load((String[])null);
  }
  
  public void testAfterPropertiesSet() throws Exception {
    MockControl methodControl = MockControl.createControl(Methods.class);
    final Methods method = (Methods)methodControl.getMock();
    String[] filenames = new String[0];
    classUnderTest = new LogMessageRepository() {
      protected void load(String[] filenames) {
        method.load(filenames);
      }
    };
    classUnderTest.setFilenames(filenames);

    
    method.load(filenames);
    
    methodControl.replay();
    
    classUnderTest.afterPropertiesSet();
    
    methodControl.verify();
  }
}
