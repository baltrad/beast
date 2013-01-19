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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Anders Henja
 *
 */
public class GroupedStatusReporterTest extends EasyMockSupport  {
  private GroupedStatusReporter classUnderTest = null;
  private ISystemStatusReporter r1 = null;
  private ISystemStatusReporter r2 = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new GroupedStatusReporter("system");
    r1 = createMock(ISystemStatusReporter.class);
    r2 = createMock(ISystemStatusReporter.class);
    
    Map<String, ISystemStatusReporter> map = new HashMap<String, ISystemStatusReporter>();
    map.put("db", r1);
    map.put("bdb", r2);
    classUnderTest.setReporters(map);
  }
  
  @After
  public void tearDown() throws Exception {
    r1 = null;
    r2 = null;
    classUnderTest = null;
  }
  
  @Test
  public void testGetStatus() {
    Set<SystemStatus> s1 = EnumSet.of(SystemStatus.OK);
    Set<SystemStatus> s2 = EnumSet.of(SystemStatus.OK);
    
    expect(r1.getStatus()).andReturn(s1);
    expect(r2.getStatus()).andReturn(s2);
    
    replayAll();
    
    Set<SystemStatus> result = classUnderTest.getStatus("db,bdb");
    
    verifyAll();
    
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
  }

  @Test
  public void testGetStatus_2() {
    Set<SystemStatus> s2 = EnumSet.of(SystemStatus.OK);
    
    expect(r2.getStatus()).andReturn(s2);
    
    replayAll();
    
    Set<SystemStatus> result = classUnderTest.getStatus("bdb");
    
    verifyAll();
    
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
  }

  @Test
  public void testGetStatus_3() {
    Set<SystemStatus> s1 = EnumSet.of(SystemStatus.OK, SystemStatus.MEMORY_PROBLEM);
    Set<SystemStatus> s2 = EnumSet.of(SystemStatus.COMMUNICATION_PROBLEM, SystemStatus.EXCHANGE_PROBLEM);
    
    expect(r1.getStatus()).andReturn(s1);
    expect(r2.getStatus()).andReturn(s2);
    
    replayAll();
    
    Set<SystemStatus> result = classUnderTest.getStatus("db,bdb");
    
    verifyAll();
    
    Assert.assertEquals(4, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
    Assert.assertTrue(result.contains(SystemStatus.MEMORY_PROBLEM));
    Assert.assertTrue(result.contains(SystemStatus.COMMUNICATION_PROBLEM));
    Assert.assertTrue(result.contains(SystemStatus.EXCHANGE_PROBLEM));
  }

}
