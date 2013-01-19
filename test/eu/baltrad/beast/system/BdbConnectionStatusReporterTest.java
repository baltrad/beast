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

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.bdb.FileCatalog;
import eu.baltrad.bdb.db.Database;
import eu.baltrad.bdb.db.SourceManager;
import eu.baltrad.bdb.oh5.Source;

/**
 * @author Anders Henja
 */
public class BdbConnectionStatusReporterTest extends EasyMockSupport {
  private BdbConnectionStatusReporter classUnderTest = null;
  private FileCatalog fileCatalog = null;
  
  @Before
  public void setUp() throws Exception {
    fileCatalog = createMock(FileCatalog.class);
    classUnderTest = new BdbConnectionStatusReporter();
    classUnderTest.setCatalog(fileCatalog);
  }

  @After
  public void tearDown() throws Exception {
    fileCatalog = null;
    classUnderTest = null;
  }
  
  @Test
  public void testGetName() {
    Assert.assertEquals("bdb", classUnderTest.getName());
  }
  
  @Test
  public void testStatus() {
    Database dbmock = createMock(Database.class);
    SourceManager smmock = createMock(SourceManager.class);
    List<Source> slist = new ArrayList<Source>(); 
    Set<SystemStatus> result = null;
    
    expect(fileCatalog.getDatabase()).andReturn(dbmock);
    expect(dbmock.getSourceManager()).andReturn(smmock);
    expect(smmock.getSources()).andReturn(slist);
    
    replayAll();
    
    result = classUnderTest.getStatus();
    
    verifyAll();
    Assert.assertEquals(1, result.size());
    Assert.assertEquals(true, result.contains(SystemStatus.OK));
  }
  
  @Test
  public void testStatus_false() {
    Database dbmock = createMock(Database.class);
    Set<SystemStatus> result = null;
    
    expect(fileCatalog.getDatabase()).andReturn(dbmock);
    expect(dbmock.getSourceManager()).andThrow(new RuntimeException());
    
    replayAll();
    
    result = classUnderTest.getStatus();
    
    verifyAll();
    Assert.assertEquals(1, result.size());
    Assert.assertEquals(true, result.contains(SystemStatus.COMMUNICATION_PROBLEM));
  }
  
}
