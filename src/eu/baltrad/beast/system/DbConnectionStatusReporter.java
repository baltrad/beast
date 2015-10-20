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

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.JdbcOperations;

/**
 * @author Anders Henja
 *
 */
public class DbConnectionStatusReporter implements ISystemStatusReporter {
  /**
   * The jdbc template
   */
  private JdbcOperations template = null;
  
  /**
   * Sets the jdbc template, used for testing.
   * @param template the template to set
   */
  public void setJdbcTemplate(JdbcOperations template) {
    this.template = template;
  }

  /**
   * @see eu.baltrad.beast.system.ISystemStatusReporter#getName()
   */
  @Override
  public String getName() {
    return "db.status";
  }

  /**
   * @see eu.baltrad.beast.system.ISystemStatusReporter#getSupportedAttributes()
   */
  @Override
  public Set<String> getSupportedAttributes() {
    return new HashSet<String>();
  }
  
  /**
   * @see eu.baltrad.beast.system.ISystemStatusReporter#getStatus(java.lang.String[])
   */
  @Override
  public Set<SystemStatus> getStatus(Map<String,Object> values) {
    try {
      template.queryForObject("SELECT 1",int.class);
      return EnumSet.of(SystemStatus.OK);
    } catch (Exception e) {
      return EnumSet.of(SystemStatus.COMMUNICATION_PROBLEM);
    }
  }
}
