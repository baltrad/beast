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
package eu.baltrad.beast.log.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Anders Henja
 */
public class LogMessageRepositoryTest extends EasyMockSupport {
  interface Methods {
    public void load(String[] filenames);
  }
  
  private static String XML_LOAD_TEST_FIXTURE = "load-test-log-messages.xml";
  private static String XML_LOAD_TEST_FIXTURE2 = "load-test-log-messages2.xml";
  private LogMessageRepository classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    classUnderTest = new LogMessageRepository();
  }

  @After
  public void tearDown() throws Exception {
    classUnderTest = null;    
  }

  @Test
  public void testFilenames_init() throws Exception {
    String[] result = classUnderTest.getFilenames();
    assertEquals(0, result.length);
  }

  @Test
  public void testFilenames_setArray() throws Exception {
    String[] arr = new String[]{"A", "B", "C"};
    classUnderTest.setFilenames(arr);
    assertSame(arr, classUnderTest.getFilenames());
  }

  @Test
  public void testFilenames_setNull() throws Exception {
    classUnderTest.setFilenames(null);
    String[] result = classUnderTest.getFilenames();
    assertEquals(0, result.length);
  }
  
  @Test
  public void testAdd() throws Exception {
    LogMessage msg = new LogMessage();
    msg.setCode("01234");
    msg.setMessage("my message");
    msg.setSolution("my solution");
    msg.setModule("MYMODULE");
    classUnderTest.add(msg);
    
    LogMessage result = classUnderTest.getMessage("01234");
    assertSame(msg, result);
    
    result = classUnderTest.getMessage("01235");
    assertNull(result);
  }

  @Test
  public void testAdd_nullMessage() throws Exception {
    try {
      classUnderTest.add(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      //pass
    }
  }

  @Test
  public void testAdd_missingModuleInformation() throws Exception {
    LogMessage msg = new LogMessage();
    msg.setCode("12345");
    try {
      classUnderTest.add(msg);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }
  
  @Test
  public void testAdd_missingEcodeInformation() throws Exception {
    LogMessage msg = new LogMessage();
    msg.setModule("MODULE");
    try {
      classUnderTest.add(msg);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }
  
  @Test
  public void testGet_noSuchMessage() throws Exception {
    LogMessage result = classUnderTest.getMessage("01234");
    assertNull(result);
  }
  
  @Test
  public void testRemove() throws Exception {
    LogMessage msg = new LogMessage();
    msg.setCode("01234");
    msg.setMessage("my message");
    msg.setSolution("my solution");
    msg.setModule("MYMODULE");
    classUnderTest.add(msg);

    classUnderTest.remove("01234");
    LogMessage result = classUnderTest.getMessage("01234");
    assertNull(result);
  }

  @Test
  public void testGetModuleMessages() throws Exception {
    LogMessage m1 = new LogMessage("MODULE1", "00001", "Message 1");
    LogMessage m2 = new LogMessage("MODULE1", "00002", "Message 1");
    LogMessage m3 = new LogMessage("MODULE2", "00003", "Message 1");
    
    classUnderTest.add(m1);
    classUnderTest.add(m2);
    classUnderTest.add(m2);
    classUnderTest.add(m3);

    List<LogMessage> result = classUnderTest.getModuleMessages();
    assertEquals(3, result.size());
    assertEquals("00001", result.get(0).getCode());
    assertEquals("00002", result.get(1).getCode());
    assertEquals("00003", result.get(2).getCode());
  }

  @Test
  public void testGetModuleMessages_2() throws Exception {
    LogMessage m1 = new LogMessage("MODULE1", "00001", "Message 1");
    LogMessage m2 = new LogMessage("MODULE1", "00002", "Message 1");
    LogMessage m3 = new LogMessage("MODULE2", "00001", "Message 1");
    
    classUnderTest.add(m1);
    classUnderTest.add(m2);
    classUnderTest.add(m2);
    classUnderTest.add(m3);

    List<LogMessage> result = classUnderTest.getModuleMessages();
    assertEquals(2, result.size());
    assertEquals("00001", result.get(0).getCode());
    assertEquals("MODULE2", result.get(0).getModule());
    assertEquals("00002", result.get(1).getCode());
  }
  
  
  @Test
  public void testGetMessage_args() throws Exception {
    LogMessage m1 = new LogMessage("MODULE1", "00001", "Message %s ok");
    LogMessage m2 = new LogMessage("MODULE1", "00002", "Message %s nok");

    classUnderTest.add(m1);
    classUnderTest.add(m2);
    
    String result = classUnderTest.getMessage("00001", "Message %s was ok", "abc");
    assertEquals("Message abc ok", result);
    result = classUnderTest.getMessage("00002", "Message %s was ok", "abc");
    assertEquals("Message abc nok", result);
  }

  @Test
  public void testLoad() throws Exception {
    File f = new File(this.getClass().getResource(XML_LOAD_TEST_FIXTURE).getFile());
    classUnderTest.load(f.getAbsolutePath());
    LogMessage msg = classUnderTest.getMessage("00001");
    assertEquals("00001", msg.getCode());
    assertEquals("BEAST", msg.getModule());
    assertEquals(MessageSeverity.INFO, msg.getSeverity());
    assertEquals("Registered adaptor '%s' of type %s", msg.getMessage());
    assertEquals(null, msg.getSolution());

    msg = classUnderTest.getMessage("00004");
    assertEquals("00004", msg.getCode());
    assertEquals("BEAST", msg.getModule());
    assertEquals(MessageSeverity.ERROR, msg.getSeverity());
    assertEquals("XMLRPC communication with '%s' FAILED", msg.getMessage());
    assertEquals("Verify that the product generation framework is running", msg.getSolution());

  }

  @Test
  public void testLoad_manyFiles() throws Exception {
    File f = new File(this.getClass().getResource(XML_LOAD_TEST_FIXTURE).getFile());
    File f2 = new File(this.getClass().getResource(XML_LOAD_TEST_FIXTURE2).getFile());
    
    classUnderTest.load(new String[]{f.getAbsolutePath(), f2.getAbsolutePath()});
    LogMessage msg = classUnderTest.getMessage("00001");
    assertEquals("00001", msg.getCode());
    assertEquals("BEAST", msg.getModule());
    assertEquals("Registered adaptor '%s' of type %s", msg.getMessage());
    assertEquals(null, msg.getSolution());

    msg = classUnderTest.getMessage("00004");
    assertEquals("00004", msg.getCode());
    assertEquals("BEAST", msg.getModule());
    assertEquals("XMLRPC communication with '%s' FAILED", msg.getMessage());
    assertEquals("Verify that the product generation framework is running", msg.getSolution());

    msg = classUnderTest.getMessage("10001");
    assertEquals("10001", msg.getCode());
    assertEquals("OTHER", msg.getModule());
    assertEquals("Something", msg.getMessage());
    assertEquals(MessageSeverity.UNDEFINED, msg.getSeverity());
    assertEquals(null, msg.getSolution());
  }

  @Test
  public void testLoad_manyFiles_null() throws Exception {
    // Very silly test, but just verify that no exception is thrown
    classUnderTest.load((String[])null);
  }
  
  @Test
  public void testAfterPropertiesSet() throws Exception {
    final Methods method = createMock(Methods.class);
    String[] filenames = new String[0];
    classUnderTest = new LogMessageRepository() {
      protected void load(String[] filenames) {
        method.load(filenames);
      }
    };
    classUnderTest.setFilenames(filenames);

    
    method.load(filenames);
    
    replayAll();
    
    classUnderTest.afterPropertiesSet();
    
    verifyAll();
  }
}
