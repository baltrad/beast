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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    
    List<ISystemStatusReporter> list = new ArrayList<ISystemStatusReporter>();
    list.add(r1);
    list.add(r2);
    classUnderTest.setReporters(list);
  }
  
  @After
  public void tearDown() throws Exception {
    r1 = null;
    r2 = null;
    classUnderTest = null;
  }
  
  @Test
  public void testGetName() {
    classUnderTest = new GroupedStatusReporter("nisse");
    Assert.assertEquals("nisse", classUnderTest.getName());
  }
  
  @Test
  public void testGetSupportedAttributes() {
    Set<String> s1 = new HashSet<String>();
    s1.add("abc");
    s1.add("def");
    
    Set<String> s2 = new HashSet<String>();
    s1.add("def");
    s1.add("ghi");
    
    expect(r1.getSupportedAttributes()).andReturn(s1);
    expect(r2.getSupportedAttributes()).andReturn(s2);
    
    replayAll();
    
    Set<String> result = classUnderTest.getSupportedAttributes();
    
    verifyAll();
    Assert.assertEquals(3, result.size());
    Assert.assertTrue(result.contains("abc"));
    Assert.assertTrue(result.contains("def"));
    Assert.assertTrue(result.contains("ghi"));
  }
  
  @Test
  public void testGetStatus() {
    Set<SystemStatus> s1 = EnumSet.of(SystemStatus.OK);
    Set<SystemStatus> s2 = EnumSet.of(SystemStatus.OK);
    
    Map<String,Object> values = new HashMap<String, Object>();
    values.put("value", "123");

    expect(r1.getStatus(values)).andReturn(s1);
    expect(r2.getStatus(values)).andReturn(s2);
    
    replayAll();
    
    
    Set<SystemStatus> result = classUnderTest.getStatus(values);
    
    verifyAll();
    
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
  }

  @Test
  public void testGetStatus_3() {
    Set<SystemStatus> s1 = EnumSet.of(SystemStatus.OK, SystemStatus.MEMORY_PROBLEM);
    Set<SystemStatus> s2 = EnumSet.of(SystemStatus.COMMUNICATION_PROBLEM, SystemStatus.EXCHANGE_PROBLEM);
    Map<String,Object> values = new HashMap<String, Object>();
    values.put("values", "123");

    expect(r1.getStatus(values)).andReturn(s1);
    expect(r2.getStatus(values)).andReturn(s2);
    
    replayAll();
    
    Set<SystemStatus> result = classUnderTest.getStatus(values);
    
    verifyAll();
    
    Assert.assertEquals(4, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
    Assert.assertTrue(result.contains(SystemStatus.MEMORY_PROBLEM));
    Assert.assertTrue(result.contains(SystemStatus.COMMUNICATION_PROBLEM));
    Assert.assertTrue(result.contains(SystemStatus.EXCHANGE_PROBLEM));
  }

}
