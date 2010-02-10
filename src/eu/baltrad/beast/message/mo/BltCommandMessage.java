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
 * Represents a simple command message containing a command string.
 * @author Anders Henja
 */
public class BltCommandMessage implements IBltMessage, IBltXmlMessage {
  public static final String BLT_COMMAND = "bltcommand";
  
  /**
   * The command string.
   */
  private String command = null;

  /**
   * @param command the command to set
   */
  public void setCommand(String command) {
    this.command = command;
  }

  /**
   * @return the command
   */
  public String getCommand() {
    return command;
  }

  /**
   * @see eu.baltrad.beast.message.IBltXmlMessage#fromDocument(org.dom4j.Document)
   */
  @Override
  public void fromDocument(Document dom) {
    if (!dom.getRootElement().getName().equals(BLT_COMMAND)) {
      throw new MessageParserException("Atempting to create BltCommand from: " + dom.asXML());
    }
    setCommand(dom.valueOf("//bltcommand/command"));
  }

  /**
   * @see eu.baltrad.beast.message.IBltXmlMessage#toDocument()
   */
  @Override
  public Document toDocument() {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement(BLT_COMMAND);
    el.addElement("command").addText(this.command==null?"":this.command);
    return document;
  }
}
