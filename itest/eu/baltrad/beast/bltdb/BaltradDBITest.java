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
package eu.baltrad.beast.bltdb;

import eu.baltrad.fc.oh5.File;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.fc.FileCatalog;

/**
 * @author Anders Henja
 *
 */
public class BaltradDBITest extends TestCase {
  private ApplicationContext context = null;
  private BeastDBTestHelper helper = null;
  private static String h5fixture = "ODIM_H5_pvol_ang_20090501T1200Z.h5";
  
  public BaltradDBITest(String name) {
    super(name);
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("testHelper");
  }
  
  public void testSomething() throws Exception {
    java.io.File f = new java.io.File(this.getClass().getResource(h5fixture).getFile());
    String filename = f.getAbsolutePath();
    
    FileCatalog fc = new FileCatalog(helper.getBaltradDbUri(), helper.getBaltradDbPth());
    File h5f = fc.catalog(filename);
    assertNotNull(h5f);
  }
}
