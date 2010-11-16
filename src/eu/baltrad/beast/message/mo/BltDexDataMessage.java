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

import eu.baltrad.beast.message.IBltXmlMessage;
import eu.baltrad.beast.message.MessageParserException;

/**
 * Wraps a baltrad DEX message. It will be used for communicating to
 * other baltrad nodes. The BltDexData message is a homebrew message
 * that does not contain any actual information. That is up to the
 * parser on the receiving side to manage.
 * 
 * This message is only used for dex communication and hence is managed
 * there. Each dex-data transfer contains two parts:
 * bltmessage:
 *   <bltdexdata />
 * bltdexdata:
 *   the binary representation of the ODIM H5 file.
 *   
 * So, when DEX parses the messages it will perform the following:
 *   xml = httprequest.getPart("bltmessage");
 *   message = xmlParser.parse(xml);
 *   if (message instanceof BltDexDataMessage) {
 *     BltDexDataMessage m = (BltDexDataMessage)message;
 *     ... dump file locally ....
 *     m.setFilename(...)
 *   }
 *   
 * This means that filename is not included in the xml-document but is a
 * part of the http message. 
 * @author Anders Henja
 */
public class BltDexDataMessage implements IBltXmlMessage {
  /**
   * The xml root tag for a baltrad dex data exchange message
   */
  public static final String BLT_DEXDATA = "bltdexdata";
  
  /**
   * The file name
   */
  private String filename;
  
  /**
   * Constructor
   */
  public BltDexDataMessage() {
    setFilename(null);
  }
  
  /**
   * Sets the file name
   * @param filename the file name
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }
  
  /**
   * Returns the file name
   * @return the file name
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @see eu.baltrad.beast.message.IBltXmlMessage#fromDocument(org.dom4j.Document)
   */
  @Override
  public void fromDocument(Document dom) {
    if (!dom.getRootElement().getName().equals(BLT_DEXDATA)) {
      throw new MessageParserException("Atempting to create BltDexData from: " + dom.asXML());
    }
  }

  /**
   * @see eu.baltrad.beast.message.IBltXmlMessage#toDocument()
   */
  @Override
  public Document toDocument() {
    Document document = DocumentHelper.createDocument();
    document.addElement(BLT_DEXDATA);
    return document;
  }
}
