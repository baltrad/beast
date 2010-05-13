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
package eu.baltrad.beast.router;

import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.router.impl.BltRouter;
import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class BltRouterCompositeDBITest extends TestCase {
  private BltRouter classUnderTest = null;
  private ApplicationContext context = null;
  private BeastDBTestHelper helper = null;
  
  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.cleanInsert(this);
    classUnderTest = (BltRouter)context.getBean("router");
    classUnderTest.afterPropertiesSet();
  }
  
  public void tearDown() throws Exception {
    context = null;
    helper = null;
    classUnderTest = null;
  }

  public void testLoadCompositingDef() {
    RouteDefinition def = classUnderTest.getDefinition("admin");
    assertNotNull(def);
    assertEquals("admin", def.getName());
    assertEquals("Karl", def.getAuthor());
    assertEquals("blt_composite", def.getRuleType());
  }
}
