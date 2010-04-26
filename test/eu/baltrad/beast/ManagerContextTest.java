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

import eu.baltrad.beast.ManagerContext;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class ManagerContextTest extends TestCase {
  
  public void setUp() throws Exception {
    new ManagerContext().setTimeoutManager(null);
  }
  
  public void testTimeoutManager() {
    TimeoutManager manager = new TimeoutManager();
    new ManagerContext().setTimeoutManager(manager);
    assertSame(manager, ManagerContext.getTimeoutManager());
  }
  
  public void testAfterPropertiesSet() throws Exception {
    TimeoutManager manager = new TimeoutManager();
    ManagerContext context = new ManagerContext();
    context.setTimeoutManager(manager);
    context.afterPropertiesSet();
  }
  
  public void testAfterPropertiesSet_noTimeoutManager() throws Exception {
    ManagerContext context = new ManagerContext();
    try {
      context.afterPropertiesSet();
      fail("Expected BeanCreationException");
    } catch (BeanCreationException e) {
      //pass
    }
  }
}
