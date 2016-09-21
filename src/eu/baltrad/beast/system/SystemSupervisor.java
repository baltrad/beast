/* --------------------------------------------------------------------
Copyright (C) 2009-2013 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.system;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import eu.baltrad.beast.log.message.MessageSeverity;

/**
 * A system supervisor. Keeps tracks of the reported alerts.
 * @author Anders Henja
 */
public class SystemSupervisor implements ISystemSupervisor {
  /**
   * The saved messages.
   */
  private List<SystemMessage> messages = new ArrayList<SystemMessage>();

  /**
   * A list of status reporters.
   */
  private Map<String, ISystemStatusReporter> reporters = new HashMap<String, ISystemStatusReporter>();
  
  private final static Logger logger = LogManager.getLogger(SystemSupervisor.class);
  
  /**
   * @see eu.baltrad.beast.system.ISystemSupervisor#add(SystemMessage)
   */
  @Override
  public void add(SystemMessage message) {
    if (message == null) {
      throw new NullPointerException();
    }
    messages.add(message);
  }
  
  /**
   * @see eu.baltrad.beast.system.ISystemSupervisor#getMessages()
   */
  @Override
  public List<SystemMessage> getMessages() {
    return messages;
  }
  
  /**
   * Creates a new system message
   * @param code the error code
   * @param args the argument list
   * @return the created system message
   */
  public SystemMessage createMessage(String module, String code, MessageSeverity severity, String message, String solution) {
    SystemMessage result = new SystemMessage();
    result.setModule(module);
    result.setCode(code);
    result.setSeverity(severity);
    result.setMessage(message);
    result.setSolution(solution);
    return result;
  }
  
  /**
   * Registers a reporter with specified name
   * @param name the name that the reporter should be found by
   * @param reporter the reporter
   */
  public void register(String name, ISystemStatusReporter reporter) {
    reporters.put(name, reporter);
  }
  
  /**
   * Sets all reporters used by this supervisor
   * @param reporters
   */
  public void setReporters(Map<String, ISystemStatusReporter> reporters) {
    this.reporters = reporters;
  }
  
  /**
   * @see eu.baltad.beast.system.ISystemSupervisor#getSupportedAttributes(java.lang.String)
   */
  @Override
  public Set<String> getSupportedAttributes(String component) {
    if (reporters.containsKey(component)) {
      return reporters.get(component).getSupportedAttributes();
    }
    return new HashSet<String>();
  }
  
  /**
   * @see eu.baltad.beast.system.ISystemSupervisor#getStatus(java.lang.String, java.util.Map)
   */
  @Override  
  public Set<SystemStatus> getStatus(String component, Map<String,Object> values) {
    if (reporters.containsKey(component)) {
      return reporters.get(component).getStatus(values);
    }
    return EnumSet.of(SystemStatus.UNDEFINED);
  }

  /**
   * @see eu.baltrad.beast.system.ISystemSupervisor#supportsMappableStatus(java.lang.String)
   */
  @Override
  public boolean supportsMappableStatus(String component) {
    logger.info("supportsMappableStatus("+component+")");
    boolean result = false;
    if (reporters.containsKey(component)) {
      logger.info("supportsMappableStatus: Checking instance");
      result = reporters.get(component) instanceof IMappableStatusReporter;
    }
    logger.info("supportsMappableStatus("+component+") = " + result);
    return result;
  }

  /**
   * @see eu.baltrad.beast.system.ISystemSupervisor#getMappedStatus(java.lang.String, java.util.Map)
   */
  @Override
  public Map<String, Map<Object, SystemStatus>> getMappedStatus(String component, Map<String, Object> values) {
    logger.info("getMappedStatus");
    if (reporters.containsKey(component) && supportsMappableStatus(component)) {
      logger.info("Returning mapped status values");
      return ((IMappableStatusReporter)reporters.get(component)).getMappedStatus(values);
    }
    return null;
  }
}
