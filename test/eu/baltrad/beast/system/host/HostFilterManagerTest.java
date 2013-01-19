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

package eu.baltrad.beast.system.host;

import static org.easymock.EasyMock.expect;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

/**
 * @author Anders Henja
 */
public class HostFilterManagerTest extends EasyMockSupport {
  private HostFilterManager classUnderTest = null;
  private SimpleJdbcOperations template = null;
  
  @Before
  public void setUp() throws Exception {
    template = createMock(SimpleJdbcOperations.class);
    classUnderTest = new HostFilterManager();
    classUnderTest.setTemplate(template);
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void testAdd() {
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "127.0.0.1")).andReturn(1);
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "192.168.2.*")).andReturn(1);
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "192.168.*.*")).andReturn(1);
    
    replayAll();

    classUnderTest.add("127.0.0.1");
    classUnderTest.add("192.168.2.*");
    classUnderTest.add("192.168.*.*");
    
    List<String> result = classUnderTest.getPatterns();
    
    verifyAll();
    Assert.assertEquals(3, result.size());
    Assert.assertEquals("127.0.0.1", result.get(0));
    Assert.assertEquals("192.168.2.*", result.get(1));
    Assert.assertEquals("192.168.*.*", result.get(2));
  }
  
  @Test
  public void testAdd_invalidPattern() {
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "127.0.0.1")).andReturn(1);
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "192.168.*.*")).andReturn(1);
    
    replayAll();

    classUnderTest.add("127.0.0.1");
    try {
      classUnderTest.add("192.168.*");
      Assert.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      //pass
    }
    classUnderTest.add("192.168.*.*");
    
    List<String> result = classUnderTest.getPatterns();
    
    verifyAll();
    Assert.assertEquals(2, result.size());
    Assert.assertEquals("127.0.0.1", result.get(0));
    Assert.assertEquals("192.168.*.*", result.get(1));
  }
  
  @Test
  public void testAdd_nullPattern() {
    replayAll();

    try {
      classUnderTest.add(null);
      Assert.fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      //pass
    }
    
    verifyAll();
    Assert.assertEquals(0, classUnderTest.getPatterns().size());
  }

  
  @Test
  public void testRemove() {
    // SETUP
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "127.0.0.1")).andReturn(1);
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "192.168.*.*")).andReturn(1);
    
    // THIS IS WHAT WE WANT TO VERIFY
    expect(template.update("DELETE FROM beast_host_filter WHERE name = ?", "192.168.*.*")).andReturn(1);
    
    replayAll();

    classUnderTest.add("127.0.0.1");
    classUnderTest.add("192.168.*.*");

    // Execute test
    classUnderTest.remove("192.168.*.*");
    
    List<String> result = classUnderTest.getPatterns();
    
    verifyAll();
    Assert.assertEquals(1, result.size());
    Assert.assertEquals("127.0.0.1", result.get(0));
  }

  @Test
  public void testRemove_noSuchEntry() {
    // SETUP
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "127.0.0.1")).andReturn(1);
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "192.168.*.*")).andReturn(1);
    
    // THIS IS WHAT WE WANT TO VERIFY
    expect(template.update("DELETE FROM beast_host_filter WHERE name = ?", "192.168.2.*")).andReturn(1);
    
    replayAll();

    classUnderTest.add("127.0.0.1");
    classUnderTest.add("192.168.*.*");

    // Execute test
    classUnderTest.remove("192.168.2.*");
    
    List<String> result = classUnderTest.getPatterns();
    
    verifyAll();
    Assert.assertEquals(2, result.size());
    Assert.assertEquals("127.0.0.1", result.get(0));
    Assert.assertEquals("192.168.*.*", result.get(1));
  }
  
  @Test
  public void testRemove_null() {
    replayAll();
    
    try {
      classUnderTest.remove(null);
      Assert.fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // pass
    }
    
    verifyAll();
  }

  @Test
  public void testAccepted() {
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "192.168.0.*")).andReturn(1);
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "127.0.0.1")).andReturn(1);
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "192.*.*.*")).andReturn(1);
    
    String[] ipaddresses = new String[] {
      "192.168.0.255",
      "192.168.0.0",
      "192.255.1.1",
      "127.0.0.1"
    };

    replayAll();
    
    classUnderTest.add("192.168.0.*");
    classUnderTest.add("127.0.0.1");
    classUnderTest.add("192.*.*.*");

    for (String s : ipaddresses) {
      boolean result = classUnderTest.accepted(s);
      Assert.assertEquals(true, result);
    }
    
    verifyAll();
  }
 
  @Test
  public void testIsRegistered() {
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "192.168.0.*")).andReturn(1);
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "127.0.0.1")).andReturn(1);

    replayAll();
    
    classUnderTest.add("192.168.0.*");
    classUnderTest.add("127.0.0.1");
    
    boolean t1 = classUnderTest.isRegistered("192.168.0.*");
    boolean t2 = classUnderTest.isRegistered("127.0.0.1");
    boolean t3 = classUnderTest.isRegistered("192.168.*.*");
    boolean t4 = classUnderTest.isRegistered("*");
    boolean t5 = classUnderTest.isRegistered(null);
    
    Assert.assertEquals(true, t1);
    Assert.assertEquals(true, t2);
    Assert.assertEquals(false, t3);
    Assert.assertEquals(false, t4);
    Assert.assertEquals(false, t5);
    
  }
  
  @Test
  public void testAfterPropertiesSet() throws Exception {
    List<String> strings = new ArrayList<String>();
    strings.add("192.168.1.1");
    strings.add("192.168.1.2");
    
    final ParameterizedRowMapper<String> mapper = new ParameterizedRowMapper<String>() {
      @Override
      public String mapRow(ResultSet rs, int rown) throws SQLException {
        return null;
      }
    };
    classUnderTest = new HostFilterManager() {
      protected ParameterizedRowMapper<String> getEntryMapper() {
        return mapper;
      }
    };
    classUnderTest.setTemplate(template);

    expect(template.query("SELECT name FROM beast_host_filter", mapper)).andReturn(strings);
    
    replayAll();
    
    classUnderTest.afterPropertiesSet();
    
    verifyAll();
    List<String> result = classUnderTest.getPatterns();
    Assert.assertEquals(2, result.size());
    Assert.assertEquals("192.168.1.1", result.get(0));
    Assert.assertEquals("192.168.1.2", result.get(1));
  }

  @Test
  public void testAfterPropertiesSet_invalidFilter() throws Exception {
    List<String> strings = new ArrayList<String>();
    strings.add("192.168.1.1");
    strings.add("192.168.a.1");
    strings.add("192.168.1.2");
    
    final ParameterizedRowMapper<String> mapper = new ParameterizedRowMapper<String>() {
      @Override
      public String mapRow(ResultSet rs, int rown) throws SQLException {
        return null;
      }
    };
    classUnderTest = new HostFilterManager() {
      protected ParameterizedRowMapper<String> getEntryMapper() {
        return mapper;
      }
    };
    classUnderTest.setTemplate(template);

    expect(template.query("SELECT name FROM beast_host_filter", mapper)).andReturn(strings);
    
    replayAll();
    
    classUnderTest.afterPropertiesSet();
    
    verifyAll();
    List<String> result = classUnderTest.getPatterns();
    Assert.assertEquals(2, result.size());
    Assert.assertEquals("192.168.1.1", result.get(0));
    Assert.assertEquals("192.168.1.2", result.get(1));
  }

  @Test
  public void testNotAccepted() {
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "192.168.0.*")).andReturn(1);
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "127.0.0.1")).andReturn(1);
    expect(template.update("INSERT INTO beast_host_filter (name) VALUES (?)", "192.*.*.*")).andReturn(1);
    
    String[] ipaddresses = new String[] {
      "193.168.0.255",
      "191.168.0.1",
      "255.255.255.255",
      "127.0.0.2"
    };

    replayAll();
    
    classUnderTest.add("192.168.0.*");
    classUnderTest.add("127.0.0.1");
    classUnderTest.add("192.*.*.*");

    for (String s : ipaddresses) {
      boolean result = classUnderTest.accepted(s);
      Assert.assertEquals(false, result);
    }
    
    verifyAll();
  }
  
  @Test
  public void testIsValidPattern() {
    String[] validPatterns = new String[] {
        "192.*.*.*",
        "*.*.*.*",
        "127.*.0.1",
        "127.0.0.1",
        "123.123.123.123"
    };
    
    replayAll();
    
    for (String s : validPatterns) {
        boolean result = classUnderTest.isValidPattern(s);
        Assert.assertEquals(true, result);
    }
    verifyAll();
  }

  @Test
  public void testInvalidPattern() {
    String[] validPatterns = new String[] {
        "192.*.*",
        "*..*.*",
        "abcd.com",
        "127.[0-9].1.1",
        "123.123*.0"
    };
    
    replayAll();
    
    for (String s : validPatterns) {
        boolean result = classUnderTest.isValidPattern(s);
        Assert.assertEquals(false, result);
    }
    verifyAll();
  }
}
