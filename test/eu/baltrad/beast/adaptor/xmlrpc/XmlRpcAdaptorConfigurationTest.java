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

import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class XmlRpcAdaptorConfigurationTest extends TestCase {
  public void testConstructor() throws Exception {
    XmlRpcAdaptorConfiguration classUnderTest = new XmlRpcAdaptorConfiguration();
    assertEquals(null, classUnderTest.getName());
    assertEquals("XMLRPC", classUnderTest.getType());
  }
  
  public void testConstructor_2() throws Exception {
    XmlRpcAdaptorConfiguration classUnderTest = new XmlRpcAdaptorConfiguration("NAME");
    assertEquals("NAME", classUnderTest.getName());
    assertEquals("XMLRPC", classUnderTest.getType());
  }

  public void testName() throws Exception {
    XmlRpcAdaptorConfiguration classUnderTest = new XmlRpcAdaptorConfiguration();
    classUnderTest.setName("NAME");
    assertEquals("NAME", classUnderTest.getName());
  }

  public void testURL() throws Exception {
    XmlRpcAdaptorConfiguration classUnderTest = new XmlRpcAdaptorConfiguration();
    classUnderTest.setURL("http://something");
    assertEquals("http://something", classUnderTest.getURL());
  }
  
}
