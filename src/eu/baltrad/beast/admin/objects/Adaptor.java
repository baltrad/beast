/* --------------------------------------------------------------------
Copyright (C) 2009-2021 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.admin.objects;

import org.codehaus.jackson.map.annotate.JsonRootName;

import eu.baltrad.beast.adaptor.IAdaptor;
import eu.baltrad.beast.adaptor.xmlrpc.XmlRpcAdaptor;

/**
 * Adaptor container used by the adaptor command
 * @author anders
 */
@JsonRootName("adaptor")
public class Adaptor {
  private String name = null;
  private String type = "XMLRPC";
  private String uri = null;
  private long timeout = 5000;
  
  /**
   * Constructor
   */
  public Adaptor() {
  }
  
  /**
   * Constructor
   * @param name of the adaptor
   */
  public Adaptor(String name) {
    setName(name);
  }
  
  /**
   * Constructor
   * @param name of the adaptor
   * @param type of the adaptor, typically XMLRPC
   */
  public Adaptor(String name, String type) {
    setName(name);
    setType(type);
  }

  /**
   * Constructor
   * @param name of the adaptor
   * @param type of the adaptor, typically XMLRPC
   * @param uri address of the recipient
   */
  public Adaptor(String name, String type, String uri) {
    setName(name);
    setType(type);
    setUri(uri);
  }

  /**
   * Constructor
   * @param name of the adaptor
   * @param type of the adaptor, typically XMLRPC
   * @param uri address of the recipient
   * @param timeout for any command
   */
  public Adaptor(String name, String type, String uri, long timeout) {
    setName(name);
    setType(type);
    setUri(uri);
    setTimeout(timeout);
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the uri
   */
  public String getUri() {
    return uri;
  }

  /**
   * @param uri the uri to set
   */
  public void setUri(String uri) {
    this.uri = uri;
  }

  /**
   * @return the timeout
   */
  public long getTimeout() {
    return timeout;
  }

  /**
   * @param timeout the timeout to set
   */
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }
  
  /**
   * Fills this object from the {@link IAdaptor}
   * @param adaptor
   */
  public void fromAdaptor(IAdaptor adaptor) {
    if (adaptor instanceof XmlRpcAdaptor) {
      XmlRpcAdaptor xra = (XmlRpcAdaptor)adaptor;
      this.setName(xra.getName());
      this.setTimeout(xra.getTimeout());
      this.setType(xra.getType());
      this.setUri(xra.getUrl());
    }
  }
}
