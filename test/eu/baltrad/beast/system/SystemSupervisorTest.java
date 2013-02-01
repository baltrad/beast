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
public class SystemSupervisorTest extends EasyMockSupport {
  private SystemSupervisor classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new SystemSupervisor();
  }

  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void testCheckStatus() {
    Set<SystemStatus> rcstatus = EnumSet.of(SystemStatus.OK);
    RadarConnectionStatusReporter rcmock = createMock(RadarConnectionStatusReporter.class);
    Map<String,Object> values = new HashMap<String, Object>();
    values.put("sources", "seang");
    
    classUnderTest.register("radar", rcmock);
    
    expect(rcmock.getStatus(values)).andReturn(rcstatus);

    replayAll();
    
    Set<SystemStatus> result = classUnderTest.getStatus("radar", values);

    verifyAll();
    Assert.assertSame(rcstatus, result);
  }
  
  @Test
  public void testCheckStatus_noSuchReporter() {
    Map<String,Object> values = new HashMap<String, Object>();
    values.put("sources", "seang");
    
    replayAll();
    
    Set<SystemStatus> result = classUnderTest.getStatus("radar", values);

    verifyAll();
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.UNDEFINED));
  }

  @Test
  public void testCheckStatus_system() {
    Set<SystemStatus> s1 = EnumSet.of(SystemStatus.COMMUNICATION_PROBLEM);
    ISystemStatusReporter smock = createMock(ISystemStatusReporter.class);
    Map<String,Object> values = new HashMap<String, Object>();
    values.put("components", "bdb,db");
    
    classUnderTest.register("system", smock);
    
    expect(smock.getStatus(values)).andReturn(s1);
    
    replayAll();
    
    Set<SystemStatus> result = classUnderTest.getStatus("system", values);
    
    verifyAll();
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.COMMUNICATION_PROBLEM));
  }
}
