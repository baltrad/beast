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
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltMultiRoutedMessage;
import eu.baltrad.beast.message.mo.BltRoutedMessage;
import eu.baltrad.beast.router.IMultiRoutedMessage;
import eu.baltrad.beast.router.IRoutedMessage;
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
public class BltRouter implements IRouter, IRouterManager, InitializingBean {
  /**
   * The JDBC template managing the database connectivity.
   */
  private SimpleJdbcOperations template = null;

  /**
   * The route definitions.
   */
  private List<RouteDefinition> definitions = null;
  
  /**
   * The rule factory. 
   */
  private IRuleFactory factory = null;
  
  /**
   * Constructor
   */
  public BltRouter() {
    definitions = new ArrayList<RouteDefinition>();
  };
  
  /**
   * Sets the data source that should be used by the SimpleJdbcTemplate instance
   * @param source the data source
   */
  public void setDataSource(DataSource source) {
    template = new SimpleJdbcTemplate(source);
  }

  /**
   * Sets the jdbc template, mostly used for testing since {@link #setDataSource(DataSource)} will create
   * a simple jdbc template.
   * @param template the template
   */
  void setJdbcTemplate(SimpleJdbcOperations template) {
    this.template = template;
  }
  
  /**
   * Sets the rule factory.
   * @param factory the factory to set
   */
  public void setRuleFactory(IRuleFactory factory) {
    this.factory = factory;
  }
  
  /**
   * Sets a known list of definitions for this router. Used
   * for test purposes, otherwise this data is retrieved from
   * the database.
   * @param definitions - a list of definitions.
   */
  void setDefinitions(List<RouteDefinition> definitions) {
    this.definitions = definitions;
  }
  
	/**
	 * @see IRouter#getMultiRoutedMessages(IBltMessage)
	 * @param msg - the message that should result in the multi routed messages.
	 * @return a list of zero or more multi routed messages.
	 */
	@Override
	public List<IMultiRoutedMessage> getMultiRoutedMessages(IBltMessage msg) {
	  List<IMultiRoutedMessage> result = new ArrayList<IMultiRoutedMessage>();
	  for (RouteDefinition d : definitions) {
	    IBltMessage nmsg = d.getRule().handle(msg);
	    if (nmsg != null) {
	      if (nmsg instanceof IMultiRoutedMessage) {
	        result.add((IMultiRoutedMessage)nmsg);
	      } else if (nmsg instanceof IRoutedMessage) {
	        BltMultiRoutedMessage rms = new BltMultiRoutedMessage();
	        rms.setMessage(((IRoutedMessage)nmsg).getMessage());
	        List<String> destinations = new ArrayList<String>();
	        destinations.add(((IRoutedMessage) nmsg).getDestination());
	        rms.setDestinations(destinations);
	        result.add(rms);
	      } else {
	        BltMultiRoutedMessage rms = new BltMultiRoutedMessage();
	        rms.setMessage(nmsg);
	        rms.setDestinations(d.getRecipients());
	        result.add(rms);
	      }
	    }
	  }
	  return result;
	}

	 /**
   * @see IRouter#getRoutedMessages(IBltMessage)
   * @param msg - the message that should result in the routed messages.
   * @return a list of zero or more routed messages.
   */
	@Override
	public List<IRoutedMessage> getRoutedMessages(IBltMessage msg) {
    List<IRoutedMessage> result = new ArrayList<IRoutedMessage>();
    for (RouteDefinition d : definitions) {
      IBltMessage nmsg = d.getRule().handle(msg);
      if (nmsg != null) {
        if (nmsg instanceof IRoutedMessage) {
          result.add((IRoutedMessage)nmsg);
        } else if (nmsg instanceof IMultiRoutedMessage) {
          List<String> recipients = ((IMultiRoutedMessage)nmsg).getDestinations();
          if (recipients != null) {
            for (String r: recipients) {
              BltRoutedMessage bmsg = new BltRoutedMessage();
              bmsg.setDestination(r);
              bmsg.setMessage(((IMultiRoutedMessage)nmsg).getMessage());
              result.add(bmsg);
            }
          }
        } else {
          for (String r : d.getRecipients()) {
            BltRoutedMessage bmsg = new BltRoutedMessage();
            bmsg.setDestination(r);
            bmsg.setMessage(nmsg);
            result.add(bmsg);
          }
        }
      }
    }
    return result;
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
      storeRecipients(def.getName(), def.getRecipients());
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
      storeRecipients(def.getName(), def.getRecipients());
    } catch (Throwable t) {
      throw new RuleException("Failed to update router rule definition: " + t.getMessage(), t);
    }
    // If all went well, then replace the existing definition with the new one.
    removeDefinitionFromList(def.getName());
    definitions.add(def);
  }
 
  /**
   * @see eu.baltrad.beast.router.IRouterManager#create(String,String,boolean,String,IRule)
   */
  @Override
  public RouteDefinition create(String name, String author, boolean active, String description, List<String> recipients, IRule rule) {
    RouteDefinition result = new RouteDefinition();
    result.setActive(active);
    result.setAuthor(author);
    result.setDescription(description);
    result.setName(name);
    result.setRecipients(recipients);
    result.setRule(rule);
    return result;
  }
  
  /**
   * Spring framework will call this function after the bean has been created.
   */
  public synchronized void afterPropertiesSet() throws Exception {
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
   * Stores the recipients
   * @param name - the route definition name
   * @param recipients a list of recipients
   */
  protected void storeRecipients(String name, List<String> recipients) {
    if (recipients != null) {
      for (String rec: recipients) {
        template.update("insert into router_dest (name, recipient) values (?,?)",
            new Object[]{name, rec});
      }
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
