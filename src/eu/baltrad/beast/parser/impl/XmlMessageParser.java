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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.MessageParserException;
import eu.baltrad.beast.parser.IXmlMessageFactory;
import eu.baltrad.beast.parser.IXmlMessageParser;

/**
 * Manages the parsing of xml documents.
 * @author Anders Henja
 */
public class XmlMessageParser implements IXmlMessageParser {
  /**
   * The message factory
   */
  private IXmlMessageFactory factory = null;
  
  /**
   * Sets the factory
   * @param factory the factory
   */
  public void setFactory(IXmlMessageFactory factory) {
    this.factory = factory;
  }
  
  /**
   * @see eu.baltrad.beast.parser.IXmlMessageParser#parse(java.lang.String)
   */
  @Override
  public IBltMessage parse(String xml) {
    Document dom = parseXml(xml);
    return factory.createMessage(dom.getRootElement().getName());
  }
  
  /**
   * Creates a dom Document from a xml string.
   * @param xml the xml string
   * @return a Document
   * @throws MessageParserException
   */
  protected Document parseXml(String xml) {
    try {
      SAXReader xmlReader = new SAXReader();
      InputStream in = new ByteArrayInputStream(xml.getBytes("ISO-8859-1"));
      return xmlReader.read(in);
    } catch (Throwable t) {
      throw new MessageParserException("Failed to parse xml", t);
    }
  }
}
