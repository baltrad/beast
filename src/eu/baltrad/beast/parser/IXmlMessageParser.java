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
package eu.baltrad.beast.parser;

import eu.baltrad.beast.message.IBltXmlMessage;
import eu.baltrad.beast.message.MessageParserException;

/**
 * Parser interface. Will atempt to create a baltrad message from a xml-message.
 * @author Anders Henja
 */
public interface IXmlMessageParser {
  /**
   * Parses a xml-string and atempts to create a message object from this xml
   * message.
   * @param xml - the xml message
   * @return a message object
   * @throws MessageParserException if xml not could be parsed or the object is not found
   */
  public IBltXmlMessage parse(String xml);
}
