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
   * Returns the status for the specified component with the given arguments
   * @param component the component
   * @param args the arguments used for identifying specific parts
   * @return the system status as a set. That means that the status can be combined
   */
  public Set<SystemStatus> getStatus(String component, String...args);

}
