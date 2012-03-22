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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;

import eu.baltrad.beast.message.MessageParserException;

/**
 * @author Anders Henja
 */
public class BltTriggerJobMessageTest {
  @Test
  public void testSetId() {
    BltTriggerJobMessage classUnderTest = new BltTriggerJobMessage();
    assertEquals(null, classUnderTest.getId());
    classUnderTest.setId("a.b");
    assertEquals("a.b", classUnderTest.getId());
  }

  @Test
  public void testSetName() {
    BltTriggerJobMessage classUnderTest = new BltTriggerJobMessage();
    assertEquals(null, classUnderTest.getName());
    classUnderTest.setName("a.b");
    assertEquals("a.b", classUnderTest.getName());
  }

  @Test
  public void testSetArguments() {
    BltTriggerJobMessage classUnderTest = new BltTriggerJobMessage();
    String[] result = classUnderTest.getArgs();
    assertEquals(0, result.length);
    classUnderTest.setArgs(new String[]{"a","b"});
    result = classUnderTest.getArgs();
    assertEquals(2, result.length);
    assertEquals("a", result[0]);
    assertEquals("b", result[1]);
  }
  
  @Test
  public void testSetArguments_null() {
    BltTriggerJobMessage classUnderTest = new BltTriggerJobMessage();
    classUnderTest.setArgs(null);
    String[] result = classUnderTest.getArgs();
    assertEquals(0, result.length);
  }

  @Test
  public void testFromDocument() throws Exception {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement("blttriggerjob");
    el.addElement("id").addText("a.id");
    el.addElement("name").addText("a.name");
    
    Element subtree = el.addElement("arguments");
    subtree.addElement("arg").addText("arg1");
    subtree.addElement("arg").addText("arg2");

    // execute
    BltTriggerJobMessage classUnderTest = new BltTriggerJobMessage();
    classUnderTest.fromDocument(document);
    
    // verify
    assertEquals("a.id", classUnderTest.getId());
    assertEquals("a.name", classUnderTest.getName());
    String[] args = classUnderTest.getArgs();
    assertEquals(2, args.length);
    assertEquals("arg1", args[0]);
    assertEquals("arg2", args[1]);
  }

  @Test
  public void testFromDocument_notBltGenerate() throws Exception {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement("bltadm");
    el.addElement("algorithm").addText("one.Algorithm");
    
    // execute
    BltTriggerJobMessage classUnderTest = new BltTriggerJobMessage();
    
    try {
      classUnderTest.fromDocument(document);
      fail("Expected MessageParserException");
    } catch (MessageParserException e) {
      // pass
    }
  }   
  
  @Test
  public void testFromDocument_noArguments() throws Exception {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement("blttriggerjob");
    el.addElement("id").addText("a.id");
    el.addElement("name").addText("a.name");
    
    // execute
    BltTriggerJobMessage classUnderTest = new BltTriggerJobMessage();
    classUnderTest.fromDocument(document);
    
    // verify
    assertEquals("a.id", classUnderTest.getId());
    assertEquals("a.name", classUnderTest.getName());
    String[] args = classUnderTest.getArgs();
    assertEquals(0, args.length);
  }
  
  @Test
  public void testToDocument() throws Exception {
    BltTriggerJobMessage classUnderTest = new BltTriggerJobMessage();
    classUnderTest.setId("a.id");
    classUnderTest.setName("a.name");
    classUnderTest.setArgs(new String[]{"-k","val"});
    Document result = classUnderTest.toDocument();
    assertEquals("blttriggerjob", result.getRootElement().getName());
    assertEquals("a.id", result.valueOf("//blttriggerjob/id"));
    assertEquals("a.name", result.valueOf("//blttriggerjob/name"));
    assertEquals("-k", result.valueOf("//blttriggerjob/arguments/arg[1]"));
    assertEquals("val", result.valueOf("//blttriggerjob/arguments/arg[2]"));
  }
}
