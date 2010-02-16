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
package eu.baltrad.beast.router.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.router.IRoute;
import eu.baltrad.beast.router.IRouter;
import eu.baltrad.beast.router.IRouterManager;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleFactory;
import eu.baltrad.beast.rules.RuleException;

/**
 * The Baltrad router that determines all routes.
 * @author Anders Henja
 */
public class BltRouter implements IRouter, IRouterManager {
  /**
   * The JDBC template managing the database connectivity.
   */
  private SimpleJdbcTemplate template = null;

  /**
   * The route definitions.
   */
  private List<RouteDefinition> definitions = null;
  
  /**
   * The rule factory. 
   */
  private IRuleFactory factory = null;
  
  /**
   * Sets the data source that should be used by the SimpleJdbcTemplate instance
   * @param source the data source
   */
  public void setDataSource(DataSource source) {
    template = new SimpleJdbcTemplate(source);
  }

  /**
   * Sets the rule factory.
   * @param factory the factory to set
   */
  public void setRuleFactory(IRuleFactory factory) {
    this.factory = factory;
  }
  
	/**
	 * Creates a list of zero or more routes.
	 * @param msg - the message that should result in the route(s).
	 * @return a list of zero or more routes.
	 */
	@Override
	public synchronized List<IRoute> getRoutes(IBltMessage msg) {
		// TODO Auto-generated method stub
		return null;
	}

  /**
   * @see eu.baltrad.beast.router.IRouterManager#deleteDefinition(java.lang.String)
   */
  @Override
  public synchronized void deleteDefinition(String name) {
    try {
      template.update("delete from router_dest where name=?", new Object[]{name});
      template.update("delete from router_rules where name=?", new Object[]{name});
    } catch (Throwable t) {
      throw new RuleException("Failed to remove rule: '" + name+"'");
    }
    removeDefinitionFromList(name);
  }

  /**
   * @see eu.baltrad.beast.router.IRouterManager#getDefinition(java.lang.String)
   */
  @Override
  public synchronized RouteDefinition getDefinition(String name) {
    for (RouteDefinition def: definitions) {
      if (def.getName().equals(name)) {
        return def;
      }
    }
    return null;
  }

  /**
   * @see eu.baltrad.beast.router.IRouterManager#getDefinitions()
   */
  @Override
  public synchronized List<RouteDefinition> getDefinitions() {
    return this.definitions;
  }

  /**
   * @see eu.baltrad.beast.router.IRouterManager#storeDefinition(eu.baltrad.beast.router.RouteDefinition)
   */
  @Override
  public synchronized void storeDefinition(RouteDefinition def) {
    IRule rule = def.getRule();
    String type = rule.getType();
    String definition = rule.getDefinition();
    try {
      template.update(
          "insert into router_rules (name,type,author,description,active,definition)"+
          "  values (?,?,?,?,?,?)",
          new Object[]{def.getName(), type, def.getAuthor(), def.getDescription(), 
              def.isActive(), definition});
      List<String> recipients = def.getRecipients();
      for (String rec: recipients) {
        template.update("insert into router_dest (name, recipient) values (?,?)",
          new Object[]{def.getName(), rec});
      }      
    } catch (Throwable t) {
      throw new RuleException("Failed to add router rule definition: " + t.getMessage(), t);
    }
    this.definitions.add(def);
  }

  /**
   * @see eu.baltrad.beast.router.IRouterManager#updateDefinition(eu.baltrad.beast.router.RouteDefinition)
   */
  @Override
  public synchronized void updateDefinition(RouteDefinition def) {
    IRule rule = def.getRule();
    String type = rule.getType();
    String definition = rule.getDefinition();
    try {
      template.update("update router_rules set type=?, author=?, description=?, active=?, definition=? where name=?", 
          new Object[]{type, def.getAuthor(), def.getDescription(), def.isActive(), definition, def.getName()});
      template.update("delete from router_dest where name=?", def.getName());
      List<String> recipients = def.getRecipients();
      for (String rec: recipients) {
        template.update("insert into router_dest (name, recipient) values (?,?)",
          new Object[]{def.getName(), rec});
      }
    } catch (Throwable t) {
      throw new RuleException("Failed to update router rule definition: " + t.getMessage(), t);
    }
    // If all went well, then replace the existing definition with the new one.
    removeDefinitionFromList(def.getName());
    definitions.add(def);
  }
  
  /**
   * Spring framework will call this function after the bean has been created.
   */
  public synchronized void afterPropertiesSet() throws Exception {
    if (definitions != null) {
      return;
    }

    ParameterizedRowMapper<RouteDefinition> mapper = new ParameterizedRowMapper<RouteDefinition>() {
      public RouteDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
        String name = rs.getString("name");
        String type = rs.getString("type");
        String author = rs.getString("author");
        String descr = rs.getString("description");
        String definition = rs.getString("definition");
        boolean active = rs.getBoolean("active");
            
        RouteDefinition rd = new RouteDefinition();
        rd.setName(name);
        rd.setAuthor(author);
        rd.setDescription(descr);
        rd.setActive(active);
        try {
          IRule rule = factory.create(type, definition);
          rd.setRule(rule);
        } catch (RuleException re) {
          re.printStackTrace();
        }
        return rd;
      }
    };
        
    definitions = template.query("select name,type,author,description,active,definition from router_rules",
            mapper, new Object[]{});
        
    for (RouteDefinition def: definitions) {
      def.setRecipients(getRecipients(def.getName()));
    }
  }
  
  /**
   * Returns the recipients for the routing definition with specified name
   * @param name the name of the routing defintion.
   * @return a list of recipients.
   */
  protected synchronized List<String> getRecipients(String name) {
    ParameterizedRowMapper<String> mapper = new ParameterizedRowMapper<String>() {
      public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString("name");
      }
    };    
    return template.query("select ad.name from adaptors ad, router_dest rd, router_rules rr where rr.name=? and rd.name=rr.name and rd.recipient=ad.name",
        mapper,
        name);
  }
  
  /**
   * Removes the specified definition from the list.
   * @param name the name of the definition that should be removed
   */
  protected synchronized void removeDefinitionFromList(String name) {
    int nlen = definitions.size();
    int indexToRemove = -1;
    for (int i = 0; i < nlen; i++) {
      RouteDefinition rule = definitions.get(i);
      if (rule.getName().equals(name)) {
        indexToRemove = i;
        break;
      }
    }
    if (indexToRemove >= 0) {
      definitions.remove(indexToRemove);
    }    
  }
  
}
