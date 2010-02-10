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

import junit.framework.TestCase;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import eu.baltrad.beast.message.MessageParserException;

/**
 * Tests the Dex Data Message
 * @author Anders Henja
 */
public class BltDexDataMessageTest extends TestCase {
  public void testSetFilename() {
    BltDexDataMessage classUnderTest = new BltDexDataMessage();
    assertEquals(null, classUnderTest.getFilename());
    classUnderTest.setFilename("/this/filename.h5");
    assertEquals("/this/filename.h5", classUnderTest.getFilename());
  }
  
  public void testToDocument() {
    String expected =
      "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
      "<bltdexdata/>";
    BltDexDataMessage classUnderTest = new BltDexDataMessage();
    classUnderTest.setFilename("somefile.h5");
    Document result = classUnderTest.toDocument();
    // verify document by creating string representation.
    result.setXMLEncoding("ISO-8859-1");
    String xml = result.asXML();
    assertEquals(expected, xml);
  }  
  
  public void testFromDocument() {
    Document document = DocumentHelper.createDocument();
    document.addElement("bltdexdata");
    
    BltDexDataMessage classUnderTest = new BltDexDataMessage();
    classUnderTest.fromDocument(document);
  }  
  
  public void testFromDocument_invalidTag() {
    Document document = DocumentHelper.createDocument();
    document.addElement("bltdata");
    
    BltDexDataMessage classUnderTest = new BltDexDataMessage();
    try {
      classUnderTest.fromDocument(document);
      fail("Expected MessageParserException");
    } catch (MessageParserException e) {
      // pass
    }
  }  
  
}
