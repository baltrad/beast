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

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;

import eu.baltrad.beast.adaptor.AdaptorException;
import eu.baltrad.beast.adaptor.IAdaptor;
import eu.baltrad.beast.adaptor.IAdaptorConfiguration;
import eu.baltrad.beast.adaptor.IAdaptorConfigurationManager;


/**
 * @author Anders Henja
 */
public class XmlRpcConfigurationManager implements IAdaptorConfigurationManager {
  /**
   * The jdbc template
   */
  private JdbcOperations template = null;
  
  /**
   * The xmlrpc command generator, will be set in all created adaptors
   */
  private IXmlRpcCommandGenerator generator = null;
  
  /**
   * Default constructor
   */
  public XmlRpcConfigurationManager() {
  }

  /**
   * Sets the jdbc template, used for testing.
   * @param template the template to set
   */
  public void setJdbcTemplate(JdbcOperations template) {
    this.template = template;
  }
  
  /**
   * The command generator to be added to the adaptors;
   * @param generator
   */
  public void setGenerator(IXmlRpcCommandGenerator generator) {
    this.generator = generator;
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IAdaptorConfigurationManager#createConfiguration(java.lang.String)
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
      // First atempt to create adaptor to see if it works before atempting to store it
      // in the database.
      result = new XmlRpcAdaptor();
      result.setName(name);
      result.setUrl(url);
      result.setTimeout(timeout);
      result.setGenerator(this.generator);
      
      template.update("insert into beast_adaptors_xmlrpc (adaptor_id, uri, timeout) values (?,?,?)",
          new Object[]{id, url, timeout});
    } catch (DataAccessException t) {
      throw new AdaptorException("Could not store XMLRPC adaptor: " + name, t);
    }
    
    return result;
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IAdaptorConfigurationManager#update(int, eu.baltrad.beast.adaptor.IAdaptorConfiguration)
   */
  public IAdaptor update(int id, IAdaptorConfiguration configuration) {
    String url = ((XmlRpcAdaptorConfiguration)configuration).getURL();
    String name = configuration.getName();
    long timeout = ((XmlRpcAdaptorConfiguration)configuration).getTimeout();
    XmlRpcAdaptor result = null;
    try {
      result = new XmlRpcAdaptor();
      result.setName(name);
      result.setUrl(url);
      result.setTimeout(timeout);
      result.setGenerator(this.generator);

      template.update("update beast_adaptors_xmlrpc set uri=?, timeout=? where adaptor_id=?",
          new Object[]{url, timeout, id});
    } catch (DataAccessException t) {
      throw new AdaptorException("Could not update XMLRPC adaptor: " + name, t);
    }
    
    return result;
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IAdaptorConfigurationManager#remove(int)
   */
  @Override
  public void remove(int id) {
    try {
      template.update("delete from beast_adaptors_xmlrpc where adaptor_id=?", new Object[]{id});
    } catch (DataAccessException t) {
      throw new AdaptorException("Could not remove adaptor", t);
    }
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IAdaptorConfigurationManager#read(int, String)
   */
  @Override
  public IAdaptor read(int id, String name) {
    try {
      Map<String, Object> found = template.queryForMap("select uri, timeout from beast_adaptors_xmlrpc where adaptor_id=?",
          new Object[]{id});
      XmlRpcAdaptor result = new XmlRpcAdaptor();
      result.setName(name);
      result.setTimeout((Integer)found.get("timeout"));
      result.setUrl((String)found.get("uri"));
      result.setGenerator(this.generator);
      return result;
    } catch (DataAccessException t) {
      t.printStackTrace();
    }
    return null;
  }
}
