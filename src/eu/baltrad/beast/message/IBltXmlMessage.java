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
package eu.baltrad.beast.message;

import org.dom4j.Document;


/**
 * Any object supporting the BltXmlMessage should implement this
 * interface.
 * @todo: Probably going to be changed to annotation driven instead.
 * @author Anders Henja
 */
public interface IBltXmlMessage extends IBltMessage {
  /**
   * Sets the object with data from the xml code
   * @param xml the xml string
   */
  public void fromDocument(Document dom);
  
  /**
   * Creates an dom document from this object.
   * @return the xml string
   */
  public Document toDocument();
}
