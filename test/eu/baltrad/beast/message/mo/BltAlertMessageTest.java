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
import org.junit.Test;

import eu.baltrad.beast.message.MessageParserException;

import static org.junit.Assert.*;

/**
 * @author Anders Henja
 */
public class BltAlertMessageTest {
  @Test
  public void testSetCode() {
    BltAlertMessage classUnderTest = new BltAlertMessage();
    assertEquals(null, classUnderTest.getCode());
    classUnderTest.setCode("XYZ");
    assertEquals("XYZ", classUnderTest.getCode());
  }
  
  @Test
  public void testSetMessage() {
    BltAlertMessage classUnderTest = new BltAlertMessage();
    assertEquals(null, classUnderTest.getMessage());
    classUnderTest.setMessage("howdy");
    assertEquals("howdy", classUnderTest.getMessage());
  }
  
  @Test
  public void testToDocument() {
    BltAlertMessage classUnderTest = new BltAlertMessage();
    classUnderTest.setCode("ABC");
    classUnderTest.setMessage("server failure");
    classUnderTest.setModule("MYMODULE");
    classUnderTest.setSeverity(BltAlertMessage.INFO);
    Document result = classUnderTest.toDocument();
    assertEquals("bltalert", result.getRootElement().getName());
    assertEquals("ABC", result.valueOf("//bltalert/code"));
    assertEquals("server failure", result.valueOf("//bltalert/message"));
    assertEquals("MYMODULE", result.valueOf("//bltalert/module"));
    assertEquals("INFO", result.valueOf("//bltalert/severity"));
  }

  @Test
  public void testToDocument_emptyNodes() {
    BltAlertMessage classUnderTest = new BltAlertMessage();
    Document result = classUnderTest.toDocument();
    assertEquals("bltalert", result.getRootElement().getName());
    assertEquals("", result.valueOf("//bltalert/code"));
    assertEquals("", result.valueOf("//bltalert/message"));
    assertEquals("", result.valueOf("//bltalert/module"));
    assertEquals("INFO", result.valueOf("//bltalert/severity"));
  }

  @Test
  public void testFromDocument() {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement("bltalert");
    el.addElement("code").addText("123");
    el.addElement("message").addText("severe");
    el.addElement("module").addText("MOD1");
    el.addElement("severity").addText("INFO");

    BltAlertMessage classUnderTest = new BltAlertMessage();
    classUnderTest.fromDocument(document);
    assertEquals("123", classUnderTest.getCode());
    assertEquals("severe", classUnderTest.getMessage());
    assertEquals("MOD1", classUnderTest.getModule());
    assertEquals("INFO", classUnderTest.getSeverity());
  }

  @Test
  public void testFromDocument_noModule() {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement("bltalert");
    el.addElement("code").addText("123");
    el.addElement("message").addText("severe");
    el.addElement("severity").addText("INFO");

    BltAlertMessage classUnderTest = new BltAlertMessage();
    classUnderTest.fromDocument(document);
    assertEquals("123", classUnderTest.getCode());
    assertEquals("severe", classUnderTest.getMessage());
    assertEquals("", classUnderTest.getModule());
  }
  
  @Test
  public void testFromDocument_noSeverity() {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement("bltalert");
    el.addElement("code").addText("123");
    el.addElement("message").addText("severe");
    el.addElement("module").addText("MOD1");

    BltAlertMessage classUnderTest = new BltAlertMessage();
    classUnderTest.fromDocument(document);
    assertEquals("123", classUnderTest.getCode());
    assertEquals("severe", classUnderTest.getMessage());
    assertEquals("MOD1", classUnderTest.getModule());
    assertEquals("INFO", classUnderTest.getSeverity());
  }
  
  @Test
  public void testFromDocument_emptyNodes() {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement("bltalert");
    el.addElement("code").addText("");
    el.addElement("message").addText("");

    BltAlertMessage classUnderTest = new BltAlertMessage();
    classUnderTest.fromDocument(document);
    assertEquals("", classUnderTest.getCode());
    assertEquals("", classUnderTest.getMessage());
  }
  
  @Test
  public void testFromDocument_missingNodes() {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement("bltalert");
    el.addElement("message").addText("severe");
    
    BltAlertMessage classUnderTest = new BltAlertMessage();
    classUnderTest.fromDocument(document);
    assertEquals("", classUnderTest.getCode());
    assertEquals("severe", classUnderTest.getMessage());
  }
  
  @Test
  public void testFromDocument_notBltAlert() {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement("bltxalert");
    el.addElement("message").addText("severe");

    BltAlertMessage classUnderTest = new BltAlertMessage();
    try {
      classUnderTest.fromDocument(document);
      fail("Expected MessageParserException");
    } catch (MessageParserException e) {
      // pass
    }
  }
}
