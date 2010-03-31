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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import eu.baltrad.beast.router.Route;

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
    typeRegistry = new HashMap<String, IAdaptorConfigurationManager>();    
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
   * @see IBltAdaptorManager#reregister(IAdaptorConfiguration)
   */
  @Override
  public synchronized IAdaptor reregister(IAdaptorConfiguration configuration) {
    String name = configuration.getName();
    String type = configuration.getType();
    Map<String,Object> entry = null;
    IAdaptor result = null;
    
    try {
      entry = template.queryForMap("select type, adaptor_id from adaptors where name=?", new Object[]{name});
    } catch (Throwable t) {
      throw new AdaptorException("No configuration with that name stored");
    }
    
    if (type.equals(entry.get("type"))) {
      result = updateAdaptorConfiguration((Integer)entry.get("adaptor_id"), configuration);
    } else {
      result = redefineAdaptorConfiguration((Integer)entry.get("adaptor_id"), (String)entry.get("type"), configuration);
    }
    
    adaptors.put(name, result);
    
    return result;
  }
  
  /**
   * Updates the adaptor specific configuration.
   * @param adaptor_id the adaptor
   * @param configuration the configuration
   * @return an adaptor
   * @throws AdaptorException on failure
   */
  protected IAdaptor updateAdaptorConfiguration(int adaptor_id, IAdaptorConfiguration configuration) {
    String type = configuration.getType();
    IAdaptorConfigurationManager mgr = typeRegistry.get(type);
    return mgr.update(adaptor_id, configuration);
  }
  
  /**
   * Will reregister the adaptor specific configuration for the new type of adaptor, then
   * the old adaptor specific configuration will be removed.
   * @param adaptor_id the adaptor id
   * @param type the old type of adaptor configuration
   * @param configuration the new adaptor configuration
   * @return an adaptor
   * @throws AdaptorException on failure
   */
  protected IAdaptor redefineAdaptorConfiguration(int adaptor_id, String type, IAdaptorConfiguration configuration) {
    String ntype = configuration.getType();
    IAdaptorConfigurationManager mgr = typeRegistry.get(ntype);
    IAdaptor result = mgr.store(adaptor_id, configuration);
    
    // Try to modify type for the adaptor
    try {
      template.update("update adaptors set type=? where adaptor_id=?", new Object[]{ntype, adaptor_id});
    } catch (Throwable t) {
      mgr.remove(adaptor_id);
      throw new AdaptorException("Failed to change type of adaptor");
    }
    
    // Remove the old configuration entry but do not try to recover if it can not be removed,
    // just print an error message and continue..
    mgr = typeRegistry.get(type);
    try {
      mgr.remove(adaptor_id);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    return result;
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
   * @see eu.baltrad.beast.adaptor.IBltAdaptorManager#getRegisteredAdaptors()
   */
  @Override
  public List<IAdaptor> getRegisteredAdaptors() {
    ArrayList<IAdaptor> result = new ArrayList<IAdaptor>();
    for (IAdaptor adaptor : this.adaptors.values()) {
      result.add(adaptor);
    }
    return result;
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IBltAdaptorManager#getAvailableTypes()
   */
  public List<String> getAvailableTypes() {
    List<String> result = new ArrayList<String>();
    Iterator<String> i = typeRegistry.keySet().iterator();
    while (i.hasNext()) {
      result.add(i.next());
    }
    Collections.sort(result);
    return result;
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
   * @see eu.baltrad.beast.adaptor.IBltAdaptorManager#getAdaptorNames()
   */
  public List<String> getAdaptorNames() {
    List<String> result = new ArrayList<String>();
    Iterator<String> i = adaptors.keySet().iterator();
    while (i.hasNext()) {
      result.add(i.next());
    }
    Collections.sort(result);
    return result;
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IBltAdaptorManager#getAdaptor(String)
   */
  public IAdaptor getAdaptor(String name) {
    IAdaptor adaptor = adaptors.get(name);
    return adaptor;
  }

  /**
   * @see eu.baltrad.beast.adaptor.IBltAdaptorManager#handle(eu.baltrad.beast.router.Route)
   */
  @Override
  public void handle(Route route) {
    IAdaptor adaptor = adaptors.get(route.getDestination());
    if (adaptor == null) {
      throw new AdaptorException("No adaptor able to handle the route");
    }
    adaptor.handle(route.getMessage()); 
  }

  /**
   * @see eu.baltrad.beast.adaptor.IBltAdaptorManager#handle(eu.baltrad.beast.router.Route, eu.baltrad.beast.adaptor.IAdaptorCallback)
   */
  @Override
  public void handle(Route route, IAdaptorCallback callback) {
    IAdaptor adaptor = adaptors.get(route.getDestination());
    if (adaptor == null) {
      throw new AdaptorException("No adaptor able to handle the route");
    }
    adaptor.handle(route.getMessage(), callback);    
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
