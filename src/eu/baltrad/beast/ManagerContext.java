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
package eu.baltrad.beast;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * Inject instances to be accessible statically. If there
 * is a better way to do this, please change it....
 * @author Anders Henja
 */
public class ManagerContext implements InitializingBean {
  /**
   * Timeout Manager
   */
  protected static TimeoutManager timeoutManager = null;
  
  /**
   * The catalog for db access
   */
  protected static Catalog catalog = null;
  
  /**
   * The utilities
   */
  protected static IRuleUtilities utilities = null;
  
  /**
   * @param manager the timeout manager to set
   */
  public void setTimeoutManager(TimeoutManager manager) {
    timeoutManager = manager;
  }
  
  /**
   * @return the manager
   */
  public static TimeoutManager getTimeoutManager() {
    return timeoutManager;
  }

  /**
   * @param c the catalog to set
   */
  public void setCatalog(Catalog c) {
    catalog = c;
  }
  
  /**
   * @return the catalog
   */
  public static Catalog getCatalog() {
    return catalog;
  }

  /**
   * @param utils the utilities to set to set
   */
  public void setUtilities(IRuleUtilities utils) {
    utilities = utils;
  }
  
  /**
   * @return the utilities
   */
  public static IRuleUtilities getUtilities() {
    return utilities;
  }
  
  /**
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (timeoutManager == null) {
      throw new BeanCreationException("timeout manager not set");
    }
    if (catalog == null) {
      throw new BeanCreationException("catalog not set");
    }
    if (utilities == null) {
      throw new BeanCreationException("utilities not set");
    }
  }
}
