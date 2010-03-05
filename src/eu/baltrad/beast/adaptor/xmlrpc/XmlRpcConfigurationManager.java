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

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import eu.baltrad.beast.adaptor.AdaptorException;
import eu.baltrad.beast.adaptor.IAdaptor;
import eu.baltrad.beast.adaptor.IAdaptorConfiguration;
import eu.baltrad.beast.adaptor.IAdaptorConfigurationManager;


/**
 * @author Anders Henja
 */
public class XmlRpcConfigurationManager implements IAdaptorConfigurationManager {
  private SimpleJdbcOperations template = null;
  
  /**
   * Default constructor
   */
  public XmlRpcConfigurationManager() {
  }

  /**
   * Sets the data source
   * @param source the data source
   */
  public void setDataSource(DataSource source) {
    this.template = new SimpleJdbcTemplate(source);
  }
  
  /**
   * Sets the jdbc template, used for testing.
   * @param template the template to set
   */
  void setJdbcTemplate(SimpleJdbcOperations template) {
    this.template = template;
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IAdaptorConfigurationManager#createConfiguration()
   */
  @Override
  public IAdaptorConfiguration createConfiguration(String name) {
    return new XmlRpcAdaptorConfiguration(name);
  }

  /**
   * @see eu.baltrad.beast.adaptor.IAdaptorConfigurationManager#getType()
   */
  @Override
  public String getType() {
    return XmlRpcAdaptorConfiguration.TYPE;
  }

  /**
   * @see eu.baltrad.beast.adaptor.IAdaptorConfigurationManager#store(int, eu.baltrad.beast.adaptor.IAdaptorConfiguration)
   */
  @Override
  public IAdaptor store(int id, IAdaptorConfiguration configuration) {
    String url = ((XmlRpcAdaptorConfiguration)configuration).getURL();
    String name = configuration.getName();
    long timeout = ((XmlRpcAdaptorConfiguration)configuration).getTimeout();
    XmlRpcAdaptor result = null;
    try {
      template.update("insert into adaptors_xmlrpc (adaptor_id, uri, timeout) values (?,?,?)",
          new Object[]{id, url, timeout});
      result = new XmlRpcAdaptor();
      result.setName(name);
      result.setUrl(url);
      result.setTimeout(timeout);
    } catch (Throwable t) {
      throw new AdaptorException("Could not store XMLRPC adaptor: " + name, t);
    }
    
    return result;
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IAdaptorConfigurationManager#remove(int)
   */
  @Override
  public void remove(int id) {
    try {
      template.update("delete from adaptors_xmlrpc where adaptor_id=?", new Object[]{id});
    } catch (Throwable t) {
      throw new AdaptorException("Could not remove adaptor", t);
    }
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IAdaptorConfigurationManager#read(int, String)
   */
  @Override
  public IAdaptor read(int id, String name) {
    try {
      Map<String, Object> found = template.queryForMap("select uri, timeout from adaptors_xmlrpc where adaptor_id=?",
          new Object[]{id});
      XmlRpcAdaptor result = new XmlRpcAdaptor();
      result.setName(name);
      result.setTimeout((Integer)found.get("timeout"));
      result.setUrl((String)found.get("uri"));
      return result;
    } catch (Throwable t) {
    }
    return null;
  }
}
