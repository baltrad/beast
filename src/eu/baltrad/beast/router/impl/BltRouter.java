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
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.baltrad.beast.ManagerContext;
import eu.baltrad.beast.log.ISystemReporter;
import eu.baltrad.beast.log.NullReporter;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltMultiRoutedMessage;
import eu.baltrad.beast.message.mo.BltRoutedMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;
import eu.baltrad.beast.router.IMultiRoutedMessage;
import eu.baltrad.beast.router.IRoutedMessage;
import eu.baltrad.beast.router.IRouter;
import eu.baltrad.beast.router.IRouterManager;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.router.SystemRulesDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleIdAware;
import eu.baltrad.beast.rules.IRuleManager;
import eu.baltrad.beast.rules.IRuleRecipientAware;
import eu.baltrad.beast.rules.RuleException;

/**
 * The Baltrad router that determines all routes.
 * @author Anders Henja
 */
public class BltRouter implements IRouter, IRouterManager, InitializingBean {
  /**
   * The JDBC template managing the database connectivity.
   */
  private JdbcOperations template = null;

  /**
   * The route definitions.
   */
  private List<RouteDefinition> definitions = null;
  
  /**
   * The rule factory. 
   */
  private Map<String, IRuleManager> ruleManagers = null;
  
  /**
   * Manages all system specific rules.
   */
  private SystemRulesDefinition systemrules = null;
  
  /**
   * The reporter to use for reporting system messages
   */
  private ISystemReporter reporter = null;
  
  /**
   * Not used here but must be initialized in order for rule managers to be able to access the static context.
   */
  private ManagerContext context = null;
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(BltRouter.class);

  /**
   * Constructor
   */
  public BltRouter() {
    definitions = new ArrayList<RouteDefinition>();
    reporter = new NullReporter();
  };

  /**
   * Sets the reporter for managing system messages
   * @param reporter the reporter to use
   */
  public void setSystemReporter(ISystemReporter reporter) {
    if (reporter == null) {
      throw new IllegalArgumentException("reporter may not be null");
    }
    this.reporter = reporter;
  }
  
  /**
   * Sets the jdbc template.
   * @param template the template
   */
  public void setJdbcTemplate(JdbcOperations template) {
    this.template = template;
  }
  
  /**
   * Sets the rule managers.
   * @param ruleManagers the rule managers to set
   */
  public void setRuleManagers(Map<String,IRuleManager> ruleManagers) {
    this.ruleManagers = ruleManagers;
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
   * Sets the system definitions
   * @param def the system definition
   */
  public void setSystemRules(SystemRulesDefinition def) {
    systemrules = def;
  }
  
	/**
	 * @see IRouter#getMultiRoutedMessages(IBltMessage)
	 * @param msg - the message that should result in the multi routed messages.
	 * @return a list of zero or more multi routed messages.
	 */
	@Override
	public List<IMultiRoutedMessage> getMultiRoutedMessages(IBltMessage msg) {
	  List<IMultiRoutedMessage> result = new ArrayList<IMultiRoutedMessage>();
	  
	  if (systemrules != null) {
	    systemrules.handle(msg);
	  }
	  
	  if (msg instanceof IMultiRoutedMessage) {
	    result.add((IMultiRoutedMessage)msg);
	  } else if (msg instanceof IRoutedMessage) {
	    BltMultiRoutedMessage rms = new BltMultiRoutedMessage();
	    rms.setMessage(((IRoutedMessage) msg).getMessage());
      List<String> destinations = new ArrayList<String>();
      destinations.add(((IRoutedMessage) msg).getDestination());
	    rms.setDestinations(destinations);
	    result.add(rms);
	  } else {
	    if (msg instanceof BltTriggerJobMessage) {
	      String job = ((BltTriggerJobMessage)msg).getName();
	      RouteDefinition d = getDefinition(job);
	      result.addAll(getMultiRoutedMessages(msg, d));
	    } else {
	      // definitions might be updated while adding/executing all routes. Therefore, use a copy with current state
	      List<RouteDefinition> currentDefinitions = new ArrayList<RouteDefinition>(definitions);
	      for (RouteDefinition d : currentDefinitions) {
	        result.addAll(getMultiRoutedMessages(msg, d));
	      }
	    }
	  }
	  return result;
	}

	/**
	 * Used to get the routed messages from a specific rule 
	 * @param msg the message
	 * @param def the rule
	 */
	protected List<IMultiRoutedMessage> getMultiRoutedMessages(IBltMessage msg, RouteDefinition def) {
	  List<IMultiRoutedMessage> result = new ArrayList<IMultiRoutedMessage>();
	  if (def != null) {
	    IBltMessage nmsg = null;
	    try {
	      nmsg = def.handle(msg);
	    } catch (Exception e) {
	      logger.info("Rule caused exception", e);
	    }
	    if (nmsg != null) {
	      if (nmsg instanceof IMultiRoutedMessage) {
	        result.add((IMultiRoutedMessage) nmsg);
	      } else if (nmsg instanceof IRoutedMessage) {
	        BltMultiRoutedMessage rms = new BltMultiRoutedMessage();
	        rms.setMessage(((IRoutedMessage) nmsg).getMessage());
	        List<String> destinations = new ArrayList<String>();
	        destinations.add(((IRoutedMessage) nmsg).getDestination());
	        rms.setDestinations(destinations);
	        result.add(rms);
	      } else {
	        BltMultiRoutedMessage rms = new BltMultiRoutedMessage();
	        List<String> recipients = def.getRecipients();
	        rms.setMessage(nmsg);
	        rms.setDestinations(recipients);
	        if (recipients != null && recipients.size() > 0) {
	          logger.debug("Added " + recipients.get(0) + " + " + (recipients.size()-1) + " more to recipient list");
	        } else {
	          logger.debug("Added no recipients");
	        }
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
    
    if (systemrules != null) {
      systemrules.handle(msg);
    }
    
    if (msg instanceof IRoutedMessage) {
      result.add((IRoutedMessage)msg);
    } else if (msg instanceof IMultiRoutedMessage) {
      IBltMessage m = ((IMultiRoutedMessage)msg).getMessage();
      List<String> destinations = ((IMultiRoutedMessage)msg).getDestinations();
      if (destinations != null) {
        for (String d : destinations) {
          BltRoutedMessage rm = new BltRoutedMessage();
          rm.setDestination(d);
          rm.setMessage(m);
          result.add(rm);
        }
      }
    } else {
      if (msg instanceof BltTriggerJobMessage) {
        String job = ((BltTriggerJobMessage)msg).getName();
        RouteDefinition d = getDefinition(job);
        result.addAll(getRoutedMessages(msg, d));        
      } else {
        for (RouteDefinition d : definitions) {
          result.addAll(getRoutedMessages(msg, d));
        }
      }
    }
    return result;
	}

	 /**
   * Used to get the routed messages from a specific rule 
   * @param msg the message
   * @param def the rule
   */
	protected List<IRoutedMessage> getRoutedMessages(IBltMessage msg, RouteDefinition def) {
	  List<IRoutedMessage> result = new ArrayList<IRoutedMessage>();
	  if (def != null) {
	    IBltMessage nmsg = null;
	    try {
	      nmsg = def.handle(msg);
	    } catch (Exception e) {
        logger.info("Rule caused exception", e);
	    }
	    if (nmsg != null) {
	      if (nmsg instanceof IRoutedMessage) {
	        result.add((IRoutedMessage) nmsg);
	      } else if (nmsg instanceof IMultiRoutedMessage) {
	        List<String> recipients = ((IMultiRoutedMessage) nmsg).getDestinations();
	        if (recipients != null) {
	          for (String r : recipients) {
	            BltRoutedMessage bmsg = new BltRoutedMessage();
	            bmsg.setDestination(r);
	            bmsg.setMessage(((IMultiRoutedMessage) nmsg).getMessage());
	            result.add(bmsg);
	          }
	        }
	      } else {
	        for (String r : def.getRecipients()) {
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
   * @see eu.baltrad.beast.router.IRouterManager#getNames()
   */
  @Override
	public synchronized List<String> getNames() {
    List<String> result = new ArrayList<String>();
    for (RouteDefinition def : definitions) {
      result.add(def.getName());
    }
	  return result;
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
    List<RouteDefinition> result = new ArrayList<RouteDefinition>();
    result.addAll(this.definitions);
    return result;
  }

  /**
   * @see eu.baltrad.beast.router.IRouterManager#getDefinitions(List<String>)
   */
  @Override
  public synchronized List<RouteDefinition> getDefinitions(List<String> types) {
    if (types != null && types.size() > 0) {
      List<RouteDefinition> result = new ArrayList<RouteDefinition>();
      for (String t : types) {
        for (RouteDefinition d: this.definitions) {
          if (d.getRuleType().equals(t)) {
            result.add(d);
          }
        }
      }
      return result;
    }
    
    return getDefinitions();
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

  /**
   * @see eu.baltrad.beast.router.IRouterManager#create(String,String,boolean,String,List,IRule)
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
  

  // DB-specific stuff below.
  
  /**
   * @see eu.baltrad.beast.router.IRouterManager#deleteDefinition(java.lang.String)
   */
  @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
  @Override
  public synchronized void deleteDefinition(String name) {
    try {
      Map<String, Object> values = template.queryForMap(
          "select rule_id, type from beast_router_rules where name=?",
          new Object[]{name});
      int rule_id = (Integer)values.get("rule_id");
      IRuleManager manager = ruleManagers.get(values.get("type"));
      manager.delete(rule_id);
      template.update("delete from beast_router_dest where rule_id=?", new Object[]{rule_id});
      template.update("delete from beast_router_rules where rule_id=?", new Object[]{rule_id});
      reporter.info("00101", "Route definition '%s' removed", name);
    } catch (RuntimeException t) {
      reporter.warn("00102","Failed to remove route definition '%s'", name);
      throw new RuleException("Failed to remove rule: '" + name+"'", t);
    }
    removeDefinitionFromList(name);
  }

  /**
   * @see eu.baltrad.beast.router.IRouterManager#storeDefinition(eu.baltrad.beast.router.RouteDefinition)
   */
  @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
  @Override
  public synchronized void storeDefinition(RouteDefinition def) {
    IRule rule = def.getRule();
    String type = rule.getType();
    IRuleManager manager = ruleManagers.get(type);
    try {
      template.update(
          "insert into beast_router_rules (name,type,author,description,active)"+
          " values (?,?,?,?,?)",
          new Object[]{def.getName(), type, def.getAuthor(), def.getDescription(), 
              def.isActive()});
      int ruleid = template.queryForObject(
          "select rule_id from beast_router_rules where name=?",
          int.class,
          def.getName());
      manager.store(ruleid, rule);
      if (rule instanceof IRuleIdAware) {
        ((IRuleIdAware)rule).setRuleId(ruleid);
      }
      if (rule instanceof IRuleRecipientAware) {
        ((IRuleRecipientAware)rule).setRecipients(def.getRecipients());
      }
      storeRecipients(ruleid, def.getRecipients());
      reporter.info("00103", "%s added route '%s'", def.getAuthor(), def.getName());
    } catch (RuntimeException t) {
      reporter.warn("00104", "%s failed to add route '%s'", def.getAuthor(), t.getMessage());
      throw new RuleException("Failed to add router rule definition: " + t.getMessage(), t);
    }
    this.definitions.add(def);
  }

  /**
   * @see eu.baltrad.beast.router.IRouterManager#updateDefinition(eu.baltrad.beast.router.RouteDefinition)
   */
  @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
  @Override
  public synchronized void updateDefinition(RouteDefinition def) {
    IRule rule = def.getRule();
    String type = rule.getType();
    try {
      Map<String, Object> values = template.queryForMap(
          "select rule_id, type from beast_router_rules where name=?",
          new Object[]{def.getName()});
      int rule_id = (Integer)values.get("rule_id");
      IRuleManager manager = ruleManagers.get(values.get("type"));
      template.update("update beast_router_rules set type=?, author=?, description=?, active=? where rule_id=?", 
          new Object[]{type, def.getAuthor(), def.getDescription(), def.isActive(), rule_id});
      manager.delete(rule_id);
      
      if (rule instanceof IRuleIdAware) {
        ((IRuleIdAware)rule).setRuleId(rule_id);
      }
      if (rule instanceof IRuleRecipientAware) {
        ((IRuleRecipientAware)rule).setRecipients(def.getRecipients());
      }
      
      // store the new rule
      ruleManagers.get(def.getRule().getType()).store(rule_id, rule);

      // replace recipients
      storeRecipients(rule_id, def.getRecipients());
      
      reporter.info("00105", "%s updated route '%s'",def.getAuthor(), def.getName());
    } catch (RuntimeException t) {
      reporter.warn("00106", "%s failed to update route '%s'", def.getAuthor(), t.getMessage());
      throw new RuleException("Failed to update router rule definition: " + t.getMessage(), t);
    }
    
    // If all went well, then replace the existing definition with the new one.
    removeDefinitionFromList(def.getName());
    definitions.add(def);
  }
 
  /**
   * Spring framework will call this function after the bean has been created.
   */
  @Override
  public synchronized void afterPropertiesSet() throws Exception {
    if (context == null) {
      logger.error("Context could not be aquired. Might affect rule behaviour");
    }
    RowMapper<RouteDefinition> mapper = getRouteDefinitionMapper();
    definitions = template.query(
        "select rule_id, name,type,author,description,active from beast_router_rules",
        mapper);
  }
  
  /**
   * Stores the recipients
   * @param rule_id - the rule id
   * @param recipients a list of recipients
   */
  protected void storeRecipients(int rule_id, List<String> recipients) {
    template.update("delete from beast_router_dest where rule_id=?",
        new Object[]{rule_id});
    if (recipients != null) {
      for (String rec: recipients) {
        template.update("insert into beast_router_dest (rule_id, recipient) values (?,?)",
            new Object[]{rule_id, rec});
      }
    }
  }
  
  /**
   * Returns the recipients for the routing definition with specified name
   * @param rule_id the rule id.
   * @return a list of recipients.
   */
  protected synchronized List<String> getRecipients(int rule_id) {
    RowMapper<String> mapper = new RowMapper<String>() {
      public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString("name");
      }
    };
    return template.query("select ad.name from beast_adaptors ad, beast_router_dest rd, beast_router_rules rr where rr.rule_id=? and rd.rule_id=rr.rule_id and rd.recipient=ad.name",
        mapper,
        rule_id);
  }
  
  /**
   * Returns the definition mapper
   * @return the definition mapper
   */
  protected RowMapper<RouteDefinition> getRouteDefinitionMapper() {
    return new RowMapper<RouteDefinition>() {
      public RouteDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
        int rule_id = rs.getInt("rule_id");
        String name = rs.getString("name");
        String type = rs.getString("type");
        String author = rs.getString("author");
        String descr = rs.getString("description");
        boolean active = rs.getBoolean("active");
          
        RouteDefinition rd = new RouteDefinition();
        rd.setName(name);
        rd.setAuthor(author);
        rd.setDescription(descr);
        rd.setActive(active);
        List<String> recipients = getRecipients(rule_id);
        try {
          IRuleManager manager = ruleManagers.get(type);
          if (manager != null) {
            rd.setRule(manager.load(rule_id));
            if (rd.getRule() instanceof IRuleIdAware) {
              ((IRuleIdAware)rd.getRule()).setRuleId(rule_id);
            }
            if (rd.getRule() instanceof IRuleRecipientAware) {
              ((IRuleRecipientAware)rd.getRule()).setRecipients(recipients);
            }
          }
        } catch (RuleException re) {
          re.printStackTrace();
        }
        rd.setRecipients(recipients);
        return rd;
      }
    };
  }
  
  /**
   * @see IRouterManager#createRule(String)
   */
  @Override
  public IRule createRule(String type) {
    IRuleManager manager = ruleManagers.get(type);
    return manager.createRule();
  }

  /**
   * @param context the context to set
   */
  public void setContext(ManagerContext context) {
    this.context = context;
  }
}
