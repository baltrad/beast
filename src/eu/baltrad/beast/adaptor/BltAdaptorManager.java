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
package eu.baltrad.beast.adaptor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * @author Anders Henja
 *
 */
public class BltAdaptorManager implements IBltAdaptorManager, InitializingBean {
  /**
   * The available types with their corresponding managers
   */
  Map<String, IAdaptorConfigurationManager> typeRegistry = null;
  
  /**
   * The database access
   */
  private SimpleJdbcOperations template = null;
  
  /**
   * The list of registered adaptors.
   */
  private Map<String, IAdaptor> adaptors = null;
  
  /**
   * Default constructor
   */
  public BltAdaptorManager() {
    adaptors = new HashMap<String, IAdaptor>();
  }
  
  /**
   * Sets the data source
   * @param source the data source to set
   */
  public void setDataSource(DataSource source) {
    this.template = new SimpleJdbcTemplate(source);
  }
  
  /**
   * Sets the jdbc template to be used by this class
   * @param template the template
   */
  public void setJdbcTemplate(SimpleJdbcOperations template) {
    this.template = template;
  }
  
  /**
   * Sets the available types
   * @param typeRegistry the type registry
   */
  public void setTypeRegistry(List<IAdaptorConfigurationManager> managers) {
    this.typeRegistry = new HashMap<String, IAdaptorConfigurationManager>();
    for (IAdaptorConfigurationManager mgr: managers) {
      this.typeRegistry.put(mgr.getType(), mgr);
    }
  }
  
  /**
   * Sets the adaptors. Mostly used for test purposes otherwise this information
   * is read from the database.
   * @param adaptors the adaptors
   */
  public void setAdaptors(Map<String, IAdaptor> adaptors) {
    this.adaptors = adaptors;
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IBltAdaptorManager#store(java.lang.String, java.lang.String, eu.baltrad.beast.adaptor.IAdaptor)
   */
  @Override
  public synchronized IAdaptor register(IAdaptorConfiguration configuration) {
    String name = configuration.getName();
    String type = configuration.getType();
    IAdaptorConfigurationManager mgr = typeRegistry.get(type);
    if (mgr != null) {
      int index = 0;
      try {
        template.update("insert into adaptors (name,type) values (?,?)",
            new Object[]{name,type});
        index = template.queryForInt("select adaptor_id from adaptors where name=?", name);
      } catch (DataAccessException e) {
        throw new AdaptorException("Failed to add adaptor", e);
      }
      
      try {
        IAdaptor result = mgr.store(index, configuration);
        adaptors.put(name, result);
        return result;
      } catch (Throwable t) {
        try {
          template.update("delete adaptors where adaptor_id=?", new Object[]{index});
        } catch (Throwable x) {
        }
        throw new AdaptorException("Failed to store data", t);
      }
    }
    throw new AdaptorException("No such type: " + type);
  }

  /**
   * @see eu.baltrad.beast.adaptor.IBltAdaptorManager#unregister(java.lang.String)
   */
  @Override  
  public void unregister(String name) {
    Map<String, Object> result = template.queryForMap("select adaptor_id,type from adaptors where name=?",
        new Object[]{name});
    String type = (String)result.get("type");
    int adaptor_id = (Integer)result.get("adaptor_id");
    IAdaptorConfigurationManager mgr = typeRegistry.get(type);
    mgr.remove(adaptor_id);
    template.update("delete from adaptors where adaptor_id=?",
        new Object[]{adaptor_id});
    adaptors.remove(name);
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IBltAdaptorManager#getAvailableTypes()
   */
  public Set<String> getAvailableTypes() {
    return typeRegistry.keySet();
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IBltAdaptorManager#createConfiguration(String, String)
   */
  public IAdaptorConfiguration createConfiguration(String type, String name) {
    IAdaptorConfigurationManager mgr = typeRegistry.get(type);
    if (mgr == null) {
      throw new AdaptorException("No such type: " + type);
    }
    return mgr.createConfiguration(name);
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IBltAdaptorManager#getAvailableAdaptors()
   */
  public Set<String> getAvailableAdaptors() {
    return adaptors.keySet();
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IBltAdaptorManager#getAdaptor(String)
   */
  public IAdaptor getAdaptor(String name) {
    IAdaptor adaptor = adaptors.get(name);
    return adaptor;
  }

  /**
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    List<IAdaptor> l = template.query("select adaptor_id, name, type from adaptors", getAdaptorMapper(), (Object[])null);
    for (IAdaptor adaptor: l) {
      if (adaptor != null) {
        adaptors.put(adaptor.getName(), adaptor);
      }
    }
  }
  
  /**
   * Creates a ParameterizedRowMapper instance for fetching the data from the
   * database tables.
   * @return the parameterized row mapper
   */
  protected ParameterizedRowMapper<IAdaptor> getAdaptorMapper() {
    return new ParameterizedRowMapper<IAdaptor>() {
      @Override
      public IAdaptor mapRow(ResultSet rs, int rownum) throws SQLException {
        return doMapAdaptorRow(rs, rownum);
      }
    };
  }

  /**
   * Maps one adaptor row into an adaptor by using the types read function.
   * @param rs the result set
   * @param rownum the row number
   * @return the adaptor if found
   * @throws SQLException on any SQL related exception
   */
  protected IAdaptor doMapAdaptorRow(ResultSet rs, int rownum) throws SQLException {
    int id = rs.getInt("adaptor_id");
    String name = rs.getString("name");
    String type = rs.getString("type");
    IAdaptorConfigurationManager cfg = typeRegistry.get(type);
    if (cfg != null) {
      try {
        return cfg.read(id, name);
      } catch (AdaptorException e) {
        // failed to fetch data
      }
    }
    return null;
  }
}
