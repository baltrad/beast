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
package eu.baltrad.beast.db;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.springframework.beans.factory.BeanInitializationException;

import eu.baltrad.fc.FileCatalog;

import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class CatalogTest extends TestCase {
  private Catalog classUnderTest = null;
  
  public void setUp() throws Exception {
    classUnderTest = new Catalog();
  }
  
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  public void testAfterPropertiesSet() throws Exception {
    MockControl fcControl = MockClassControl.createControl(FileCatalog.class);
    FileCatalog fc = (FileCatalog)fcControl.getMock();
    classUnderTest.setCatalog(fc);
    
    fcControl.replay();
    
    classUnderTest.afterPropertiesSet();
    
    fcControl.verify();
  }

  public void testGetFileCatalogPath() throws Exception {
    MockControl fcControl = MockClassControl.createControl(FileCatalog.class);
    FileCatalog fc = (FileCatalog)fcControl.getMock();
    classUnderTest.setCatalog(fc);

    fc.local_path_for_uuid("xyz");
    fcControl.setReturnValue("/some/path");
    
    fcControl.replay();
    String result = classUnderTest.getFileCatalogPath("xyz");
    fcControl.verify();
    assertEquals("/some/path", result);
  }
  
  public void testAfterPropertiesSet_noFc() throws Exception {
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected BeanInitializingException");
    } catch (BeanInitializationException e) {
      // pass
    }
  }

}
