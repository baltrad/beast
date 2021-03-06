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

import java.util.Map;

import eu.baltrad.beast.message.IBltXmlMessage;
import eu.baltrad.beast.parser.IXmlMessageFactory;
import eu.baltrad.beast.parser.XmlMessageFactoryException;

/**
 * @author Anders Henja
 *
 */
public class XmlMessageFactory implements IXmlMessageFactory {
  /**
   * Keeps track of xml-body tag - class name
   */
  private Map<String, String> registry = null;
  
  /**
   * Sets the registry
   * @param registry the registry
   */
  public void setRegistry(Map<String,String> registry) {
    this.registry = registry;
  }
  
  /**
   * @see IXmlMessageFactory#createMessage(String)
   */
  public IBltXmlMessage createMessage(String tagName){
    String cname = registry.get(tagName);
    if (cname == null) {
      throw new XmlMessageFactoryException("No class mapped to " + tagName);
    }
    try {
      return (IBltXmlMessage)Class.forName(cname).newInstance();
    } catch (IllegalAccessException t) {
      throw new XmlMessageFactoryException("Failed to create instance", t);
    } catch (InstantiationException t) {
      throw new XmlMessageFactoryException("Failed to create instance", t);
    } catch (ClassNotFoundException t) {
      throw new XmlMessageFactoryException("Failed to create instance", t);
    }
  }
}
