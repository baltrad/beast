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
package eu.baltrad.beast.adaptor.xmlrpc;

/**
 * @author Anders Henja
 *
 */
public class XmlRpcCommand {
  /**
   * A list of arguments
   */
  private Object[] objects = null;
  /**
   * The rpc method name
   */
  private String method = null;
  
  /**
   * Default constructor
   */
  public XmlRpcCommand() {
  }
  
  /**
   * @param objects the objects to set
   */
  public void setObjects(Object[] objects) {
    this.objects = objects;
  }
  /**
   * @return the objects
   */
  public Object[] getObjects() {
    return objects;
  }
  /**
   * @param method the method to set
   */
  public void setMethod(String method) {
    this.method = method;
  }
  /**
   * @return the method
   */
  public String getMethod() {
    return method;
  }
}
