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

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.IBltXmlMessage;
import eu.baltrad.beast.message.MessageParserException;

/**
 * Represents an alert message.
 * @author Anders Henja
 */
public class BltAlertMessage implements IBltMessage, IBltXmlMessage {
  /**
   * Defines the alert.
   */
  public static final String BLT_ALERT = "bltalert";
  
  /**
   * The error code
   */
  private String code = null;
  /**
   * The error message in readable form
   */
  private String msg = null;
  
  /**
   * @param code the code to set
   */
  public void setCode(String code) {
    this.code = code;
  }
  
  /**
   * @return the code
   */
  public String getCode() {
    return code;
  }

  /**
   * @param msg the message to set
   */
  public void setMessage(String msg) {
    this.msg = msg;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return msg;
  }

  /**
   * @see eu.baltrad.beast.message.IBltXmlMessage#fromDocument(org.dom4j.Document)
   */
  @Override
  public void fromDocument(Document dom) {
    if (!dom.getRootElement().getName().equals(BLT_ALERT)) {
      throw new MessageParserException("Atempting to create BltAlert from: " + dom.asXML());
    }
    setCode(dom.valueOf("//bltalert/code"));
    setMessage(dom.valueOf("//bltalert/message"));
  }

  /**
   * @see eu.baltrad.beast.message.IBltXmlMessage#toDocument()
   */
  @Override
  public Document toDocument() {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement(BLT_ALERT);
    el.addElement("code").addText(this.code==null?"":this.code);
    el.addElement("message").addText(this.msg==null?"":this.msg);
    return document;
  }
}
