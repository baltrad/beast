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

package eu.baltrad.beast.system;

import static org.junit.Assert.assertEquals;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Anders Henja
 *
 */
public class XmlSystemStatusGeneratorTest {
  private XmlSystemStatusGenerator classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new XmlSystemStatusGenerator();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void testToDocument() {
    classUnderTest.add("radars", "abc,def", EnumSet.of(SystemStatus.OK));
    classUnderTest.add("products", "ghi,jkl", EnumSet.of(SystemStatus.OK, SystemStatus.COMMUNICATION_PROBLEM));
    
    Document result = classUnderTest.toDocument();
    
    assertEquals("system-status", result.getRootElement().getName());
    assertEquals("radars", result.valueOf("//system-status/reporter/@name"));
    assertEquals("abc,def", result.valueOf("//system-status/reporter/@value"));
    assertEquals("OK", result.valueOf("//system-status/reporter/@status"));
  }
  
  @Test
  public void testAdd_GetXmlString() {
    classUnderTest.add("radars", "abc,def", EnumSet.of(SystemStatus.OK));
    
    String result = classUnderTest.getXmlString();
    Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" +
        "<system-status>\n" +
    		"<reporter name=\"radars\" value=\"abc,def\" status=\"OK\"/>\n" +
        "</system-status>\n", result);
  }

  @Test
  public void testAdd_GetXmlString_2() {
    classUnderTest.add("radars", "abc,def", EnumSet.of(SystemStatus.OK,SystemStatus.MEMORY_PROBLEM));
    
    String result = classUnderTest.getXmlString();
    
    Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" +
        "<system-status>\n" +
        "<reporter name=\"radars\" value=\"abc,def\" status=\"OK|MEMORY_PROBLEM\"/>\n" +
        "</system-status>\n", result);
  }

  @Test
  public void testAdd_GetXmlString_3() {
    classUnderTest.add("radars", "abc,def", EnumSet.of(SystemStatus.OK));
    classUnderTest.add("products", "ghi,jkl", EnumSet.of(SystemStatus.OK, SystemStatus.COMMUNICATION_PROBLEM));
    
    String result = classUnderTest.getXmlString();
    
    Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" +
        "<system-status>\n" +
        "<reporter name=\"radars\" value=\"abc,def\" status=\"OK\"/>\n" +
        "<reporter name=\"products\" value=\"ghi,jkl\" status=\"OK|COMMUNICATION_PROBLEM\"/>\n" +
        "</system-status>\n", result);
  }

  @Test
  public void testAdd_GetXmlString_withEncoding() {
    classUnderTest.add("radars", "abc,def", EnumSet.of(SystemStatus.OK));
    classUnderTest.add("products", "ghi,jkl", EnumSet.of(SystemStatus.OK, SystemStatus.COMMUNICATION_PROBLEM));
    
    String result = classUnderTest.getXmlString("ISO-8859-1");
    
    Assert.assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n\n" +
        "<system-status>\n" +
        "<reporter name=\"radars\" value=\"abc,def\" status=\"OK\"/>\n" +
        "<reporter name=\"products\" value=\"ghi,jkl\" status=\"OK|COMMUNICATION_PROBLEM\"/>\n" +
        "</system-status>\n", result);
  }
  
  @Test
  public void testAdd_WithMapping() {
    boolean r1=false,r2=false,r3=false;
    Map<Object, SystemStatus> map = new HashMap<Object, SystemStatus>();
    map.put("se.oksite", SystemStatus.OK);
    map.put("se.badsite", SystemStatus.COMMUNICATION_PROBLEM);
    map.put("se.somesite", SystemStatus.PROCESSING_PROBLEM);
    
    classUnderTest.add("status", map);
    String result = classUnderTest.getXmlString("ISO-8859-1");
    String[] tokens = result.split("\n");
    
    Assert.assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>", tokens[0]);
    Assert.assertEquals("", tokens[1]);
    Assert.assertEquals("<system-status>", tokens[2]);
    Assert.assertEquals("<reporter name=\"status\">", tokens[3]);
    
    for (int i = 4; i < 7; i++) {
      if (tokens[i].equals("<reporter-status value=\"se.badsite\" status=\"COMMUNICATION_PROBLEM\"/>")) {
        r1=true;
      } else if (tokens[i].equals("<reporter-status value=\"se.somesite\" status=\"PROCESSING_PROBLEM\"/>")) {
        r2=true;
      } else if (tokens[i].equals("<reporter-status value=\"se.oksite\" status=\"OK\"/>")) {
        r3=true;
      }
    }
    
    Assert.assertTrue(r1);
    Assert.assertTrue(r2);
    Assert.assertTrue(r3);
    
    Assert.assertEquals("</reporter>", tokens[7]);
    Assert.assertEquals("</system-status>", tokens[8]);
  }
}

