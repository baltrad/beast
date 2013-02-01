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

import eu.baltrad.bdb.FileCatalog;

/**
 * Tests the connection to the bdb server
 * @author Anders Henja
 */
public class BdbConnectionStatusReporter implements ISystemStatusReporter {
  /**
   * The connection to the bdb
   */
  private FileCatalog fc = null;
  
  /**
   * @param fc the catalog to set
   */
  public void setCatalog(FileCatalog fc) {
    this.fc = fc;
  }
  
  /**
   * @return the file catalog
   */
  public FileCatalog getCatalog() {
    return this.fc;
  }
  
  /**
   * @see eu.baltrad.beast.system.ISystemStatusReporter#getName()
   */
  @Override
  public String getName() {
    return "bdb.status";
  }

  /**
   * @see eu.baltrad.beast.system.ISystemStatusReporter#getSupportedAttributes()
   */
  @Override
  public Set<String> getSupportedAttributes() {
    return new HashSet<String>();
  }
  
  /**
   * @see eu.baltrad.beast.system.ISystemStatusReporter#getStatus()
   */
  @Override
  public Set<SystemStatus> getStatus(Map<String,Object> values) {
    try {
      fc.getDatabase().getSourceManager().getSources();
      return EnumSet.of(SystemStatus.OK);
    } catch (Exception e) {
      return EnumSet.of(SystemStatus.COMMUNICATION_PROBLEM);
    }
  }

}
