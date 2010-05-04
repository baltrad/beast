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
import junit.framework.TestCase;

import org.springframework.beans.factory.BeanCreationException;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.rules.timer.TimeoutManager;

/**
 * @author Anders Henja
 */
public class ManagerContextTest extends TestCase {
  
  public void setUp() throws Exception {
    new ManagerContext().setTimeoutManager(null);
    new ManagerContext().setCatalog(null);
  }
  
  public void testTimeoutManager() {
    TimeoutManager manager = new TimeoutManager();
    new ManagerContext().setTimeoutManager(manager);
    assertSame(manager, ManagerContext.getTimeoutManager());
  }
  
  public void testCatalog() {
    Catalog c = new Catalog();
    new ManagerContext().setCatalog(c);
    assertSame(c, ManagerContext.getCatalog());
  }
  
  public void testAfterPropertiesSet() throws Exception {
    TimeoutManager manager = new TimeoutManager();
    Catalog c = new Catalog();
    ManagerContext context = new ManagerContext();
    context.setTimeoutManager(manager);
    context.setCatalog(c);
    context.afterPropertiesSet();
  }
  
  public void testAfterPropertiesSet_noTimeoutManager() throws Exception {
    ManagerContext context = new ManagerContext();
    Catalog c = new Catalog();
    context.setCatalog(c);
    try {
      context.afterPropertiesSet();
      fail("Expected BeanCreationException");
    } catch (BeanCreationException e) {
      //pass
    }
  }

  public void testAfterPropertiesSet_noCatalog() throws Exception {
    ManagerContext context = new ManagerContext();
    TimeoutManager m = new TimeoutManager();
    context.setTimeoutManager(m);
    try {
      context.afterPropertiesSet();
      fail("Expected BeanCreationException");
    } catch (BeanCreationException e) {
      //pass
    }
  }
  
}
