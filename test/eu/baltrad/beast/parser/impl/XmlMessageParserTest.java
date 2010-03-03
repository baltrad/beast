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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.easymock.MockControl;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.IBltXmlMessage;
import eu.baltrad.beast.message.MessageParserException;
import eu.baltrad.beast.parser.IXmlMessageFactory;

/**
 * Tests the XmlMessageParser
 * @author Anders Henja
 */
public class XmlMessageParserTest extends TestCase {

  private static interface ParseXmlMethod {
    public Document parseXml(String xml);
  }
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testParse() throws Exception {
    // Setup
    MockControl factoryControl = MockControl.createControl(IXmlMessageFactory.class);
    IXmlMessageFactory factory = (IXmlMessageFactory)factoryControl.getMock();
    MockControl parseXmlControl = MockControl.createControl(ParseXmlMethod.class);
    final ParseXmlMethod parseXml = (ParseXmlMethod)parseXmlControl.getMock();
    MockControl msgControl = MockControl.createControl(IBltXmlMessage.class);
    IBltXmlMessage msg = (IBltXmlMessage)msgControl.getMock();
    
    XmlMessageParser classUnderTest = new XmlMessageParser() {
      protected Document parseXml(String xml) {
        return parseXml.parseXml(xml);
      }
    };
    classUnderTest.setFactory(factory);
    
    // Execute
    Document dom = DocumentHelper.createDocument();
    dom.addElement("sometag");
    String xml = "somexml";
    parseXml.parseXml(xml);
    parseXmlControl.setReturnValue(dom);
    factory.createMessage("sometag");
    factoryControl.setReturnValue(msg);
    msg.fromDocument(dom);
    
    parseXmlControl.replay();
    factoryControl.replay();
    msgControl.replay();
    
    IBltMessage result = classUnderTest.parse(xml);
    
    // Verify
    parseXmlControl.verify();
    factoryControl.verify();
    msgControl.verify();
    assertSame(msg, result);
    
  }
  
  public void testParse_illegalXml() {
    String xml = "<bltdexdata";
    XmlMessageParser classUnderTest = new XmlMessageParser();
    try {
      classUnderTest.parse(xml);
      fail("Expected MessageParserException");
    } catch (MessageParserException e) {
      // pass
    }
  }
  
  public void testParseXml_1() throws Exception {
    String xml =
      "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
      "<bltdexdata/>";   
    XmlMessageParser classUnderTest = new XmlMessageParser();
    Document result = classUnderTest.parseXml(xml);
    assertEquals("bltdexdata", result.getRootElement().getName());
  }

  public void testParseXml_2() throws Exception {
    String xml =
      "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
      "<bltdexdata>\n"+
      "  <datanode>some text</datanode>\n"+
      "</bltdexdata>";
    XmlMessageParser classUnderTest = new XmlMessageParser();
    Document result = classUnderTest.parseXml(xml);
    assertEquals("bltdexdata", result.getRootElement().getName());
    assertEquals("some text", result.valueOf("//bltdexdata/datanode"));
  }
}
