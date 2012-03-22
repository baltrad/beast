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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;

import java.io.File;
import java.util.UUID;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanInitializationException;

import eu.baltrad.bdb.FileCatalog;

/**
 * @author Anders Henja
 */
public class CatalogTest extends EasyMockSupport {
  private Catalog classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new Catalog();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void testAfterPropertiesSet() throws Exception {
    FileCatalog fc = createMock(FileCatalog.class);
    classUnderTest.setCatalog(fc);
    
    replayAll();
    
    classUnderTest.afterPropertiesSet();
    
    verifyAll();
  }

  @Test
  public void testGetFileCatalogPath() throws Exception {
    FileCatalog fc = createMock(FileCatalog.class);
    classUnderTest.setCatalog(fc);
    
    UUID entryUuid = UUID.randomUUID();
    expect(fc.getLocalPathForUuid(entryUuid)).andReturn(new File("/some/path"));
    
    replayAll();
    
    String result = classUnderTest.getFileCatalogPath(entryUuid.toString());
    
    verifyAll();
    assertEquals("/some/path", result);
  }
  
  @Test
  public void testAfterPropertiesSet_noFc() throws Exception {
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected BeanInitializingException");
    } catch (BeanInitializationException e) {
      // pass
    }
  }

}
