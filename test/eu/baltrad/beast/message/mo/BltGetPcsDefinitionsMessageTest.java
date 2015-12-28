/* --------------------------------------------------------------------
Copyright (C) 2009-2013 Swedish Meteorological and Hydrological Institute, SMHI,

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

import static org.junit.Assert.*;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import eu.baltrad.beast.message.MessageParserException;

/**
 * @author Anders Henja
 *
 */
public class BltGetPcsDefinitionsMessageTest {
  @Test
  public void testToDocument() {
    BltGetPcsDefinitionsMessage classUnderTest = new BltGetPcsDefinitionsMessage();
    Document result = classUnderTest.toDocument();
    assertEquals("blt_get_pcs_definitions", result.getRootElement().getName());
  }

  @Test
  public void testFromDocument() {
    Document document = DocumentHelper.createDocument();
    document.addElement("blt_get_pcs_definitions");
    BltGetPcsDefinitionsMessage classUnderTest = new BltGetPcsDefinitionsMessage();
    classUnderTest.fromDocument(document);
  }
  
  @Test
  public void testFromDocument_notBltCommand() {
    Document document = DocumentHelper.createDocument();
    document.addElement("blt_xget_pcs_definitions");
    BltGetPcsDefinitionsMessage classUnderTest = new BltGetPcsDefinitionsMessage();
    try {
      classUnderTest.fromDocument(document);
      fail("Expected MessageParserException");
    } catch (MessageParserException e) {
      // pass
    }
  }
}
