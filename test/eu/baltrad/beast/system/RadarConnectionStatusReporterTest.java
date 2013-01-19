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

import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Anders Henja
 *
 */
public class RadarConnectionStatusReporterTest {
  private RadarConnectionStatusReporter classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new RadarConnectionStatusReporter();
    classUnderTest.setStatus("node1", SystemStatus.OK);
    classUnderTest.setStatus("node2", SystemStatus.COMMUNICATION_PROBLEM);
    classUnderTest.setStatus("node3", SystemStatus.EXCHANGE_PROBLEM);
    classUnderTest.setStatus("node4", SystemStatus.OK);
  }

  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  @Test
  public void testGetName() {
    Assert.assertEquals("radar", classUnderTest.getName());
  }
  
  @Test
  public void testGetStatus() {
    Set<SystemStatus> result = classUnderTest.getStatus("node1");
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
  }

  @Test
  public void testGetStatus_commastr() {
    Set<SystemStatus> result = classUnderTest.getStatus("node1,node3");
    Assert.assertEquals(2, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
    Assert.assertTrue(result.contains(SystemStatus.EXCHANGE_PROBLEM));
  }

  @Test
  public void testGetStatus_commastr_2() {
    Set<SystemStatus> result = classUnderTest.getStatus("node1,node3,node4");
    Assert.assertEquals(2, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
    Assert.assertTrue(result.contains(SystemStatus.EXCHANGE_PROBLEM));
  }

  @Test
  public void testGetStatus_commastr_3() {
    Set<SystemStatus> result = classUnderTest.getStatus("node1,,node3");
    Assert.assertEquals(2, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
    Assert.assertTrue(result.contains(SystemStatus.EXCHANGE_PROBLEM));
  }
  
  @Test
  public void testGetStatus_noSuchNode() {
    Set<SystemStatus> result = classUnderTest.getStatus("node5");
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.UNDEFINED));
  }
  
  @Test
  public void testGetStatus_oneNodeNotExisting() {
    Set<SystemStatus> result = classUnderTest.getStatus("node1,node5");
    Assert.assertEquals(2, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
    Assert.assertTrue(result.contains(SystemStatus.UNDEFINED));
  }
  
  @Test
  public void testTokenizeString() {
    String s1 = "abc,def";
    String[] tokens = classUnderTest.tokenizeString(s1);
    Assert.assertEquals(2, tokens.length);
    Assert.assertEquals("abc", tokens[0]);
    Assert.assertEquals("def", tokens[1]);
  }

  @Test
  public void testTokenizeString_1() {
    String s1 = "abc,,def";
    String[] tokens = classUnderTest.tokenizeString(s1);
    Assert.assertEquals(2, tokens.length);
    Assert.assertEquals("abc", tokens[0]);
    Assert.assertEquals("def", tokens[1]);
  }

  @Test
  public void testTokenizeString_2() {
    String s1 = ",def";
    String[] tokens = classUnderTest.tokenizeString(s1);
    Assert.assertEquals(1, tokens.length);
    Assert.assertEquals("def", tokens[0]);
  }

}
