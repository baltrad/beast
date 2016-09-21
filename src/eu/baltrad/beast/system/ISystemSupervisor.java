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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Anders Henja
 */
public interface ISystemSupervisor {
  /**
   * Adds a system message to the supervisor.
   * @param message the message to add
   */
  public void add(SystemMessage message);
  
  /**
   * @return the messages
   */
  public List<SystemMessage> getMessages();
  
  /**
   * Returns the attribute names supported by the specified component
   * @param component the component
   * @return a set of attribute names
   */
  public Set<String> getSupportedAttributes(String component);
  
  /**
   * Returns the status for the specified component with the given arguments
   * @param component the component
   * @param values the arguments used for identifying specific parts
   * @return the system status as a set. That means that the status can be combined
   */
  public Set<SystemStatus> getStatus(String component, Map<String,Object> values);
  
  /**
   * Returns true if the specified component supports the IMappableStatusReporter interface.
   * @param component the component
   * @return true if the component supports the mappable reporter
   */
  public boolean supportsMappableStatus(String component);
  
  /**
   * Returns the status as a mapped list of <string, status>
   * @param component the component
   * @param values a value mapping between keys - values
   * @return the result for <component, <key, status>>
   */
  public Map<String, Map<Object, SystemStatus> > getMappedStatus(String component, Map<String, Object> values);

}
