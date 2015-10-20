/* --------------------------------------------------------------------
Copyright (C) 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.qc;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;


/**
 * @author Anders Henja
 */
public class AnomalyDetectorManagerTest extends EasyMockSupport {
  interface MethodMock {
    public void validateName(String name);
  };

  private AnomalyDetectorManager classUnderTest = null;
  private JdbcOperations jdbcTemplate = null;

  @Before
  public void setUp() throws Exception {
    jdbcTemplate = createMock(JdbcOperations.class);
    classUnderTest = new AnomalyDetectorManager();
    classUnderTest.setJdbcTemplate(jdbcTemplate);
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  @Test
  public void testAdd() throws Exception {
    final MethodMock method = createMock(MethodMock.class);

    AnomalyDetector detector = new AnomalyDetector();
    detector.setName("uggh");
    detector.setDescription("uggh it");

    method.validateName("uggh");
    
    expect(jdbcTemplate.update("insert into beast_anomaly_detectors (name, description) values (?,?)",
        new Object[]{"uggh", "uggh it"})).andReturn(0);

    classUnderTest = new AnomalyDetectorManager() {
      protected void validateName(String name) {
        method.validateName(name);
      }
    };
    classUnderTest.setJdbcTemplate(jdbcTemplate);
    
    replayAll();
    
    classUnderTest.add(detector);
    
    verifyAll();
  }

  @Test
  public void testAdd_exception() throws Exception {
    AnomalyDetector detector = new AnomalyDetector();
    detector.setName("uggh");
    detector.setDescription("uggh it");

    expect(jdbcTemplate.update("insert into beast_anomaly_detectors (name, description) values (?,?)",
        new Object[]{"uggh", "uggh it"})).andThrow(new DataAccessException(null) {
          private static final long serialVersionUID = 1L;
        });
    
    replayAll();
    
    try {
      classUnderTest.add(detector);
      fail("Expected AnomalyException");
    } catch (AnomalyException e) {
      // pass
    }
    
    verifyAll();
  }
  
  @Test
  public void testUpdate() throws Exception {
    AnomalyDetector detector = new AnomalyDetector();
    detector.setName("uggh");
    detector.setDescription("uggh it");
    
    expect(jdbcTemplate.update("update beast_anomaly_detectors set description=? where name=?",
        new Object[]{"uggh it", "uggh"})).andReturn(1);
    
    replayAll();
   
    classUnderTest.update(detector);
    
    verifyAll();
  }

  @Test
  public void testUpdate_exception() throws Exception {
    AnomalyDetector detector = new AnomalyDetector();
    detector.setName("uggh");
    detector.setDescription("uggh it");
    
    expect(jdbcTemplate.update("update beast_anomaly_detectors set description=? where name=?",
        new Object[]{"uggh it", "uggh"})).andThrow(new DataAccessException(null) {
          private static final long serialVersionUID = 1L;
        });
    
    replayAll();

    try {
      classUnderTest.update(detector);
      fail("Expected AnomalyException");
    } catch (AnomalyException e) {
      // pass
    }
    
    verifyAll();
  }
  
  @Test
  public void testUpdate_noRowsAffected() throws Exception {
    AnomalyDetector detector = new AnomalyDetector();
    detector.setName("uggh");
    detector.setDescription("uggh it");
    
    expect(jdbcTemplate.update("update beast_anomaly_detectors set description=? where name=?",
        new Object[]{"uggh it", "uggh"})).andReturn(0);
    
    replayAll();

    try {
      classUnderTest.update(detector);
      fail("Expected AnomalyException");
    } catch (AnomalyException e) {
      // pass
    }
    
    verifyAll();
  }  
  
  @Test
  public void testList() throws Exception {
    final RowMapper<AnomalyDetector> mapper = new RowMapper<AnomalyDetector>() {
      @Override
      public AnomalyDetector mapRow(ResultSet rs, int rownr) throws SQLException {
        return null;
      }
    };
    List<AnomalyDetector> actual = new ArrayList<AnomalyDetector>();
    
    expect(jdbcTemplate.query("select name,description from beast_anomaly_detectors order by name", 
        mapper, 
        (Object[])null)).andReturn(actual);
    
    classUnderTest = new AnomalyDetectorManager() {
      protected RowMapper<AnomalyDetector> getMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbcTemplate);
    
    replayAll();
    
    List<AnomalyDetector> result = classUnderTest.list();
    
    verifyAll();
    assertSame(actual, result);
  }
  
  @Test
  public void testGet() throws Exception {
    AnomalyDetector detector = new AnomalyDetector();
    
    final RowMapper<AnomalyDetector> mapper = new RowMapper<AnomalyDetector>() {
      @Override
      public AnomalyDetector mapRow(ResultSet rs, int rownr) throws SQLException {
        return null;
      }
    };
    
    expect(jdbcTemplate.queryForObject("select name,description from beast_anomaly_detectors where name=?", 
        mapper,
        new Object[]{"nisse"})).andReturn(detector);
    
    classUnderTest = new AnomalyDetectorManager() {
      protected RowMapper<AnomalyDetector> getMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbcTemplate);
    
    replayAll();
    
    AnomalyDetector result = classUnderTest.get("nisse");
    
    verifyAll();
    assertSame(detector, result);
  }

  @Test
  public void testGet_notFound() throws Exception {
    final RowMapper<AnomalyDetector> mapper = new RowMapper<AnomalyDetector>() {
      @Override
      public AnomalyDetector mapRow(ResultSet rs, int rownr) throws SQLException {
        return null;
      }
    };
    
    expect(jdbcTemplate.queryForObject("select name,description from beast_anomaly_detectors where name=?", 
        mapper,
        new Object[]{"nisse"})).andThrow(new DataAccessException(null) {
          private static final long serialVersionUID = 1L;
        });
    
    classUnderTest = new AnomalyDetectorManager() {
      protected RowMapper<AnomalyDetector> getMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbcTemplate);
    
    replayAll();
    
    try {
      classUnderTest.get("nisse");
      fail("Expected AnomalyException");
    } catch (AnomalyException e) {
      // pass
    }
    
    verifyAll();
  }
  
  @Test
  public void testRemove() throws Exception {
    expect(jdbcTemplate.update("delete from beast_anomaly_detectors where name=?",
        new Object[]{"uggh"})).andReturn(0);
    
    replayAll();
   
    classUnderTest.remove("uggh");
    
    verifyAll();
  }

  @Test
  public void testRemove_exception() throws Exception {
    expect(jdbcTemplate.update("delete from beast_anomaly_detectors where name=?",
        new Object[]{"uggh"})).andThrow(new DataAccessException(null){
          private static final long serialVersionUID = 1L;
        });
    
    replayAll();

    try {
      classUnderTest.remove("uggh");
      fail("Expected AnomalyException");
    } catch (AnomalyException e) {
      // pass
    }
    
    verifyAll();
  }

  @Test
  public void testValidateName() throws Exception {
    String[] validNames = new String[]{
      "abc.123", "abc_123", "ABCabc123___..."  
    };
    for (String name : validNames) {
      try {
        classUnderTest.validateName(name);
      } catch (AnomalyException e) {
        fail("Expected '" + name + "' to be valid but it wasn't");
      }
    }
  }
 
  @Test
  public void testValidateName_invalid() throws Exception {
    String[] validNames = new String[]{
      "abc£123", "abc _123", "!@,$&*+", "abcABZ?\\_-!*+:;%"  
    };
    for (String name : validNames) {
      try {
        classUnderTest.validateName(name);
        fail("Expected AnomalyException for " + name);
      } catch (AnomalyException e) {
        // pass
      }
    }
  }
  
  @Test
  public void testGetMapper() throws Exception {
    ResultSet resultSet = createMock(ResultSet.class);
    
    expect(resultSet.getString("name")).andReturn("nisse");
    expect(resultSet.getString("description")).andReturn("the description");
    replayAll();
    
    RowMapper<AnomalyDetector> mapper = classUnderTest.getMapper();
    AnomalyDetector result = mapper.mapRow(resultSet, 1);

    verifyAll();
    assertEquals("nisse", result.getName());
    assertEquals("the description", result.getDescription());
  }
}
