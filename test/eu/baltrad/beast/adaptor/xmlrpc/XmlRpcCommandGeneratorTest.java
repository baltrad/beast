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
package eu.baltrad.beast.adaptor.xmlrpc;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.message.mo.BltCommandMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class XmlRpcCommandGeneratorTest extends TestCase {
  private XmlRpcCommandGenerator classUnderTest = null;
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    classUnderTest = new XmlRpcCommandGenerator();
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    classUnderTest = null;
  }
  
  public void testGenerateBltCommand() throws Exception {
    BltCommandMessage msg = new BltCommandMessage();
    msg.setCommand("ls -l");
    
    XmlRpcCommand command = classUnderTest.generate(msg);
    assertEquals("execute", command.getMethod());
    Object[] objs = command.getObjects();
    assertEquals(objs[0], "ls -l");
  }
  
  public void testGenerateBltAlert() throws Exception {
    BltAlertMessage msg = new BltAlertMessage();
    msg.setCode("ABC");
    msg.setMessage("A message");
    
    XmlRpcCommand command = classUnderTest.generate(msg);
    assertEquals("alert", command.getMethod());
    Object[] objs = command.getObjects();
    assertEquals(2, objs.length);
    assertEquals("ABC", objs[0]);
    assertEquals("A message", objs[1]);
  }

  public void testGenerateBltGenerate() throws Exception {
    BltGenerateMessage msg = new BltGenerateMessage();
    msg.setAlgorithm("x.C");
    msg.setFiles(new String[]{"x", "y"});
    msg.setArguments(new String[]{"o", "a"});
    
    XmlRpcCommand command = classUnderTest.generate(msg);
    assertEquals("generate", command.getMethod());
    Object[] objs = command.getObjects();
    assertEquals(3, objs.length);
    assertEquals("x.C", objs[0]);
    assertEquals("x", ((Object[])objs[1])[0]);
    assertEquals("y", ((Object[])objs[1])[1]);
    assertEquals("o", ((Object[])objs[2])[0]);
    assertEquals("a", ((Object[])objs[2])[1]);
  }
  
  public void testGenerate_unknownMessage() throws Exception {
    IBltMessage message = new IBltMessage(){};
    try {
      classUnderTest.generate(message);
      fail("Expected XmlRpcCommandException");
    } catch (XmlRpcCommandException e) {
      // pass
    }
  }
}
