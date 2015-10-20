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

import java.util.HashMap;
import java.util.Set;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * @author Anders Henja
 *
 */
public class DbConnectionStatusReporterTest extends EasyMockSupport {
  private DbConnectionStatusReporter classUnderTest = null;
  private JdbcOperations template = null;
  
  @Before
  public void setUp() throws Exception {
    template = createMock(JdbcOperations.class);
    classUnderTest = new DbConnectionStatusReporter();
    classUnderTest.setJdbcTemplate(template);
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void testName() {
    String result = classUnderTest.getName();
    Assert.assertEquals("db.status", result);
  }

  @Test
  public void testGetSupportedAttributes() {
    Set<String> result = classUnderTest.getSupportedAttributes();
    Assert.assertEquals(0, result.size());
  }
  
  @Test
  public void testStatus() {
    expect(template.queryForObject("SELECT 1", int.class)).andReturn(1);
    replayAll();
    
    Set<SystemStatus> result = classUnderTest.getStatus(new HashMap<String, Object>());
    
    verifyAll();
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
  }
  
  @Test
  public void testStatus_noConnection() {
    expect(template.queryForObject("SELECT 1", int.class)).andThrow(new DataAccessException("smugg") {
      private static final long serialVersionUID = 1L; });
    
    replayAll();
    
    Set<SystemStatus> result = classUnderTest.getStatus(new HashMap<String, Object>());
    
    verifyAll();
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.COMMUNICATION_PROBLEM));
  }
  
}
