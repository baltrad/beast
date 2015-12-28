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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.message.mo.BltCommandMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.message.mo.BltGetAreasMessage;
import eu.baltrad.beast.message.mo.BltGetQualityControlsMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;

/**
 * @author Anders Henja
 */
public class XmlRpcCommandGeneratorTest{
  private XmlRpcCommandGenerator classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    classUnderTest = new XmlRpcCommandGenerator();
  }

  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void testGenerateBltCommand() throws Exception {
    BltCommandMessage msg = new BltCommandMessage();
    msg.setCommand("ls -l");
    
    XmlRpcCommand command = classUnderTest.generate(msg);
    assertEquals("execute", command.getMethod());
    Object[] objs = command.getObjects();
    assertEquals(objs[0], "ls -l");
  }
  
  @Test
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

  @Test
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

  @Test
  public void testGenerateBltTriggerJob() throws Exception {
    BltTriggerJobMessage msg = new BltTriggerJobMessage();
    msg.setId("a.id");
    msg.setName("a.name");
    msg.setArgs(new String[]{"o", "a"});
    
    XmlRpcCommand command = classUnderTest.generate(msg);
    assertEquals("triggerjob", command.getMethod());
    Object[] objs = command.getObjects();
    assertEquals(3, objs.length);
    assertEquals("a.id", objs[0]);
    assertEquals("a.name", objs[1]);
    assertEquals("o", ((Object[])objs[2])[0]);
    assertEquals("a", ((Object[])objs[2])[1]);
  }
  
  @Test
  public void testGenerateBltGetQualityControlsMessage() throws Exception {
    BltGetQualityControlsMessage msg = new BltGetQualityControlsMessage();
    XmlRpcCommand command = classUnderTest.generate(msg);
    assertEquals("get_quality_controls", command.getMethod());
    Object[] objs = command.getObjects();
    assertEquals(0, objs.length);
  }
  
  @Test
  public void testGenerateBltGetAreasMessage() throws Exception {
    BltGetAreasMessage msg = new BltGetAreasMessage();
    XmlRpcCommand command = classUnderTest.generate(msg);
    assertEquals("get_areas", command.getMethod());
    Object[] objs = command.getObjects();
    assertEquals(0, objs.length);
  }
  
  @Test
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
