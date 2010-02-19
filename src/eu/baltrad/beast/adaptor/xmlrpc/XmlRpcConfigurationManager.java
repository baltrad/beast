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

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import eu.baltrad.beast.adaptor.IAdaptor;
import eu.baltrad.beast.adaptor.IAdaptorConfiguration;
import eu.baltrad.beast.adaptor.IAdaptorConfigurationManager;


/**
 * @author Anders Henja
 */
public class XmlRpcConfigurationManager implements IAdaptorConfigurationManager {
  SimpleJdbcOperations template = null;
  
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
    template.update("insert into adaptors_xmlrpc (adaptor_id, url) values (?,?)",
        new Object[]{id, url});
    XmlRpcAdaptor result = new XmlRpcAdaptor();
    result.setName(name);
    result.setURL(url);
    return result;
  }
}
