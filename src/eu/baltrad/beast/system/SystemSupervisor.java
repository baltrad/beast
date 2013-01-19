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
import java.util.List;
import java.util.Map;
import java.util.Set;

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
   * @see eu.baltad.beast.system.ISystemSupervisor#getStatus(java.lang.String, java.lang.String)
   */
  @Override  
  public Set<SystemStatus> getStatus(String component, String...args) {
    if (reporters.containsKey(component)) {
      return reporters.get(component).getStatus(args);
    }
    return EnumSet.of(SystemStatus.UNDEFINED);
  }
}
