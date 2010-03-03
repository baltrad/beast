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

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.message.IBltXmlMessage;
import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.message.mo.BltCommandMessage;
import eu.baltrad.beast.message.mo.BltDexDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.parser.IXmlMessageFactory;


/**
 * @author Anders Henja
 */
public class XmlMessageParserITest extends TestCase {
  private ApplicationContext context = null;
  private XmlMessageParser classUnderTest = null;
  
  public XmlMessageParserITest(String name) {
    super(name);
    context = BeastDBTestHelper.loadContext(this);    
  }
  
  public void setUp() throws Exception {
    classUnderTest = new XmlMessageParser();
    classUnderTest.setFactory((IXmlMessageFactory)context.getBean("xmlmsgFactory"));
   }
   
   /**
    * Teardown of test
    */
   public void tearDown() throws Exception {
     classUnderTest = null;
   }
   
   public void testParse_bltalert() throws Exception {
     String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n";
     xml += "<bltalert><code>123</code><message>severe</message></bltalert>\n";
     IBltXmlMessage msg = classUnderTest.parse(xml);
     assertSame(BltAlertMessage.class, msg.getClass());
     assertEquals("123", ((BltAlertMessage)msg).getCode());
     assertEquals("severe", ((BltAlertMessage)msg).getMessage());
   }

   public void testParse_bltcommand() throws Exception {
     String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n";
     xml += "<bltcommand><command>ls -la</command></bltcommand>\n";
     IBltXmlMessage msg = classUnderTest.parse(xml);
     assertSame(BltCommandMessage.class, msg.getClass());
     assertEquals("ls -la", ((BltCommandMessage)msg).getCommand());
   }
   
   public void testParse_bltdexdata() throws Exception {
     String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n";
     xml += "<bltdexdata />\n";
     IBltXmlMessage msg = classUnderTest.parse(xml);
     assertSame(BltDexDataMessage.class, msg.getClass());
     assertEquals(null, ((BltDexDataMessage)msg).getFilename()); // Since this is handled by the multipart
   }   

   public void testParse_bltgenerate() throws Exception {
     String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n";
     xml += "<bltgenerate><algorithm>one.Algorithm</algorithm><filelist><file>file1</file><file>file2</file></filelist><arguments><arg>arg1</arg><arg>arg2</arg></arguments></bltgenerate>\n";
     IBltXmlMessage msg = classUnderTest.parse(xml);
     assertSame(BltGenerateMessage.class, msg.getClass());
     BltGenerateMessage result = (BltGenerateMessage)msg;
     assertEquals("one.Algorithm", result.getAlgorithm());
     String[] strs = result.getFiles();
     assertEquals(2, strs.length);
     assertEquals("file1", strs[0]);
     assertEquals("file2", strs[1]);
     strs = result.getArguments();
     assertEquals(2, strs.length);
     assertEquals("arg1", strs[0]);
     assertEquals("arg2", strs[1]);
   }   
}
