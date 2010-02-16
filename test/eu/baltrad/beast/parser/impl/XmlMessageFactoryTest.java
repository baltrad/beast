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
package eu.baltrad.beast.parser.impl;

import java.util.HashMap;
import java.util.Map;

import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.message.mo.BltCommandMessage;
import eu.baltrad.beast.parser.XmlMessageFactoryException;
import junit.framework.TestCase;

/**
 * Tests the xml message factory
 * @author Anders Henja
 */
public class XmlMessageFactoryTest extends TestCase {
  
  public void testCreateMessage() {
    Map<String,String> map = new HashMap<String, String>();
    map.put(BltAlertMessage.BLT_ALERT, BltAlertMessage.class.getName());
    map.put(BltCommandMessage.BLT_COMMAND, BltCommandMessage.class.getName());
    XmlMessageFactory classUnderTest = new XmlMessageFactory();
    classUnderTest.setRegistry(map);
    
    BltAlertMessage a = (BltAlertMessage)classUnderTest.createMessage(BltAlertMessage.BLT_ALERT);
    assertNotNull(a);
    BltCommandMessage b = (BltCommandMessage)classUnderTest.createMessage(BltCommandMessage.BLT_COMMAND);
    assertNotNull(b);
  }

  public void testCreateMessage_unreckognizedTag() {
    Map<String,String> map = new HashMap<String, String>();
    map.put(BltAlertMessage.BLT_ALERT, BltAlertMessage.class.getName());
    map.put(BltCommandMessage.BLT_COMMAND, BltCommandMessage.class.getName());
    XmlMessageFactory classUnderTest = new XmlMessageFactory();
    classUnderTest.setRegistry(map);
    
    try {
      classUnderTest.createMessage("noname");
      fail("Expected XmlMessageFactoryException");
    } catch (XmlMessageFactoryException e) {
      //pass
    }
  }  

  public void testCreateMessage_null() {
    Map<String,String> map = new HashMap<String, String>();
    map.put(BltAlertMessage.BLT_ALERT, BltAlertMessage.class.getName());
    map.put(BltCommandMessage.BLT_COMMAND, BltCommandMessage.class.getName());
    XmlMessageFactory classUnderTest = new XmlMessageFactory();
    classUnderTest.setRegistry(map);
    
    try {
      classUnderTest.createMessage(null);
      fail("Expected XmlMessageFactoryException");
    } catch (XmlMessageFactoryException e) {
      //pass
    }
  }  

  public void testCreateMessage_noSuchClass() {
    Map<String,String> map = new HashMap<String, String>();
    map.put(BltAlertMessage.BLT_ALERT, "no.such.class.COM");
    XmlMessageFactory classUnderTest = new XmlMessageFactory();
    classUnderTest.setRegistry(map);
    
    try {
      classUnderTest.createMessage(BltAlertMessage.BLT_ALERT);
      fail("Expected XmlMessageFactoryException");
    } catch (XmlMessageFactoryException e) {
      //pass
    }
  }  
}
