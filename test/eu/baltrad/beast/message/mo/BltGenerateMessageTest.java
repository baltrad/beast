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

import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class BltGenerateMessageTest extends TestCase {
  public void testSetAlgorithm() {
    BltGenerateMessage classUnderTest = new BltGenerateMessage();
    assertEquals(null, classUnderTest.getAlgorithm());
    classUnderTest.setAlgorithm("a.b");
    assertEquals("a.b", classUnderTest.getAlgorithm());
  }
  
  public void testSetFiles() {
    BltGenerateMessage classUnderTest = new BltGenerateMessage();
    String[] result = classUnderTest.getFiles();
    assertEquals(0, result.length);
    classUnderTest.setFiles(new String[]{"a","b"});
    result = classUnderTest.getFiles();
    assertEquals(2, result.length);
    assertEquals("a", result[0]);
    assertEquals("b", result[1]);
  }

  public void testSetFiles_null() {
    BltGenerateMessage classUnderTest = new BltGenerateMessage();
    try {
      classUnderTest.setFiles(null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }

  public void testSetArguments() {
    BltGenerateMessage classUnderTest = new BltGenerateMessage();
    String[] result = classUnderTest.getArguments();
    assertEquals(0, result.length);
    classUnderTest.setArguments(new String[]{"a","b"});
    result = classUnderTest.getArguments();
    assertEquals(2, result.length);
    assertEquals("a", result[0]);
    assertEquals("b", result[1]);
  }
  
  public void testSetArguments_null() {
    BltGenerateMessage classUnderTest = new BltGenerateMessage();
    try {
      classUnderTest.setArguments(null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }

  public void testFromDocument() throws Exception {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement("bltgenerate");
    el.addElement("algorithm").addText("one.Algorithm");
    Element subtree = el.addElement("filelist");
    subtree.addElement("file").addText("file1");
    subtree.addElement("file").addText("file2");
    
    subtree = el.addElement("arguments");
    subtree.addElement("arg").addText("arg1");
    subtree.addElement("arg").addText("arg2");

    // execute
    BltGenerateMessage classUnderTest = new BltGenerateMessage();
    classUnderTest.fromDocument(document);
    
    // verify
    assertEquals("one.Algorithm", classUnderTest.getAlgorithm());
    String[] files = classUnderTest.getFiles();
    assertEquals(2, files.length);
    assertEquals("file1", files[0]);
    assertEquals("file2", files[1]);
    String[] args = classUnderTest.getArguments();
    assertEquals(2, args.length);
    assertEquals("arg1", args[0]);
    assertEquals("arg2", args[1]);
  }
  
  public void testFromDocument_nofiles() throws Exception {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement("bltgenerate");
    el.addElement("algorithm").addText("one.Algorithm");
    
    Element subtree = el.addElement("arguments");
    subtree.addElement("arg").addText("arg1");
    subtree.addElement("arg").addText("arg2");

    // execute
    BltGenerateMessage classUnderTest = new BltGenerateMessage();
    classUnderTest.fromDocument(document);
    
    // verify
    assertEquals("one.Algorithm", classUnderTest.getAlgorithm());
    String[] files = classUnderTest.getFiles();
    assertEquals(0, files.length);
    String[] args = classUnderTest.getArguments();
    assertEquals(2, args.length);
    assertEquals("arg1", args[0]);
    assertEquals("arg2", args[1]);
  }  
  
  public void testFromDocument_noArguments() throws Exception {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement("bltgenerate");
    el.addElement("algorithm").addText("one.Algorithm");
    Element subtree = el.addElement("filelist");
    subtree.addElement("file").addText("file1");
    subtree.addElement("file").addText("file2");
    
    // execute
    BltGenerateMessage classUnderTest = new BltGenerateMessage();
    classUnderTest.fromDocument(document);
    
    // verify
    assertEquals("one.Algorithm", classUnderTest.getAlgorithm());
    String[] files = classUnderTest.getFiles();
    assertEquals(2, files.length);
    assertEquals("file1", files[0]);
    assertEquals("file2", files[1]);
    String[] args = classUnderTest.getArguments();
    assertEquals(0, args.length);
  }
  
  public void testToDocument() throws Exception {
    BltGenerateMessage classUnderTest = new BltGenerateMessage();
    classUnderTest.setAlgorithm("some.Algorithm");
    classUnderTest.setFiles(new String[]{"a", "b"});
    classUnderTest.setArguments(new String[]{"-k","val"});
    Document result = classUnderTest.toDocument();
    assertEquals("bltgenerate", result.getRootElement().getName());
    assertEquals("some.Algorithm", result.valueOf("//bltgenerate/algorithm"));
    assertEquals("a", result.valueOf("//bltgenerate/filelist/file[1]"));
    assertEquals("b", result.valueOf("//bltgenerate/filelist/file[2]"));
    assertEquals("-k", result.valueOf("//bltgenerate/arguments/arg[1]"));
    assertEquals("val", result.valueOf("//bltgenerate/arguments/arg[2]"));
  }
}
