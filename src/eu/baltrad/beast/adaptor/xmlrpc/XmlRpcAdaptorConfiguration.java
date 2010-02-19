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

import eu.baltrad.beast.adaptor.IAdaptorConfiguration;

/**
 * @author Anders Henja
 */
public class XmlRpcAdaptorConfiguration implements IAdaptorConfiguration {
  public final static String TYPE = "XMLRPC";
  
  /**
   * The name of the adaptor
   */
  private String name = null;

  /**
   * The url that the rpc server is listening
   */
  private String url = null;
  
  /**
   * Default constructor
   */
  public XmlRpcAdaptorConfiguration() {
    this(null);
  }
  
  /**
   * Constructor
   * @param name the name of the adaptor
   */
  public XmlRpcAdaptorConfiguration(String name) {
    this.name = name;
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IAdaptorConfiguration#getName()
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name for the adaptor
   * @param name the name of the adaptor
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * Sets an url
   * @param url the url to set
   */
  public void setURL(String url) {
    this.url = url;
  }
  
  /**
   * Returns this url
   * @return the url to return
   */
  public String getURL() {
    return this.url;
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IAdaptorConfiguration#getType()
   */
  @Override
  public String getType() {
    return TYPE;
  }
}
