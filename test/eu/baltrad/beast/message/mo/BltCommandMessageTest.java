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
package eu.baltrad.beast.message.mo;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import eu.baltrad.beast.message.MessageParserException;

import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class BltCommandMessageTest extends TestCase {
  public void testSetCommand() {
    BltCommandMessage classUnderTest = new BltCommandMessage();
    assertEquals(null, classUnderTest.getCommand());
    classUnderTest.setCommand("ls");
    assertEquals("ls", classUnderTest.getCommand());
  }
  
  public void testToDocument() {
    BltCommandMessage classUnderTest = new BltCommandMessage();
    classUnderTest.setCommand("ls -la");
    Document result = classUnderTest.toDocument();
    assertEquals("bltcommand", result.getRootElement().getName());
    assertEquals("ls -la", result.valueOf("//bltcommand/command"));
  }

  public void testToDocument_noCmdString() {
    BltCommandMessage classUnderTest = new BltCommandMessage();
    Document result = classUnderTest.toDocument();
    assertEquals("bltcommand", result.getRootElement().getName());
    assertEquals("", result.valueOf("//bltcommand/command"));
  }
  
  public void testFromDocument() {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement("bltcommand");
    el.addElement("command").addText("ls -la");
    
    BltCommandMessage classUnderTest = new BltCommandMessage();
    classUnderTest.fromDocument(document);
    assertEquals("ls -la", classUnderTest.getCommand());
  }
  
  public void testFromDocument_notBltCommand() {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement("bltxcommand");
    el.addElement("command").addText("ls -la");
    BltCommandMessage classUnderTest = new BltCommandMessage();
    try {
      classUnderTest.fromDocument(document);
      fail("Expected MessageParserException");
    } catch (MessageParserException e) {
      // pass
    }
  }
}
