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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;


/**
 * @author Anders Henja
 */
public class AnomalyDetectorManagerTest extends TestCase {
  interface MethodMock {
    public void validateName(String name);
  };

  private AnomalyDetectorManager classUnderTest = null;
  private MockControl jdbcControl = null;
  private SimpleJdbcOperations jdbcTemplate = null;
  
  public void setUp() throws Exception {
    super.setUp();
    jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    jdbcTemplate = (SimpleJdbcOperations)jdbcControl.getMock();
    classUnderTest = new AnomalyDetectorManager();
    classUnderTest.setJdbcTemplate(jdbcTemplate);
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
    classUnderTest = null;
  }
  
  protected void replay() {
    jdbcControl.replay();
  }

  protected void verify() {
    jdbcControl.verify();
  }
  
  public void testAdd() throws Exception {
    MockControl methodControl = MockControl.createControl(MethodMock.class);
    final MethodMock method = (MethodMock)methodControl.getMock();

    AnomalyDetector detector = new AnomalyDetector();
    detector.setName("uggh");
    detector.setDescription("uggh it");

    method.validateName("uggh");
    
    jdbcTemplate.update("insert into beast_anomaly_detectors (name, description) values (?,?)",
        new Object[]{"uggh", "uggh it"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);

    classUnderTest = new AnomalyDetectorManager() {
      protected void validateName(String name) {
        method.validateName(name);
      }
    };
    classUnderTest.setJdbcTemplate(jdbcTemplate);
    
    replay();
    methodControl.replay();
    
    classUnderTest.add(detector);
    
    verify();
    methodControl.verify();
  }
  
  public void testAdd_exception() throws Exception {
    AnomalyDetector detector = new AnomalyDetector();
    detector.setName("uggh");
    detector.setDescription("uggh it");

    jdbcTemplate.update("insert into beast_anomaly_detectors (name, description) values (?,?)",
        new Object[]{"uggh", "uggh it"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setThrowable(new DataAccessException(null){
      private static final long serialVersionUID = 1L;
    });
    
    replay();
    try {
      classUnderTest.add(detector);
      fail("Expected AnomalyException");
    } catch (AnomalyException e) {
      // pass
    }
    verify();
  }
  
  public void testUpdate() throws Exception {
    AnomalyDetector detector = new AnomalyDetector();
    detector.setName("uggh");
    detector.setDescription("uggh it");
    
    jdbcTemplate.update("update beast_anomaly_detectors set description=? where name=?",
        new Object[]{"uggh it", "uggh"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(1);
    
    replay();
   
    classUnderTest.update(detector);
    
    verify();
  }

  public void testUpdate_exception() throws Exception {
    AnomalyDetector detector = new AnomalyDetector();
    detector.setName("uggh");
    detector.setDescription("uggh it");
    
    jdbcTemplate.update("update beast_anomaly_detectors set description=? where name=?",
        new Object[]{"uggh it", "uggh"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setThrowable(new DataAccessException(null){
      private static final long serialVersionUID = 1L;
    });
    
    replay();

    try {
      classUnderTest.update(detector);
      fail("Expected AnomalyException");
    } catch (AnomalyException e) {
      // pass
    }
    
    verify();
  }
  
  public void testUpdate_noRowsAffected() throws Exception {
    AnomalyDetector detector = new AnomalyDetector();
    detector.setName("uggh");
    detector.setDescription("uggh it");
    
    jdbcTemplate.update("update beast_anomaly_detectors set description=? where name=?",
        new Object[]{"uggh it", "uggh"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    replay();

    try {
      classUnderTest.update(detector);
      fail("Expected AnomalyException");
    } catch (AnomalyException e) {
      // pass
    }
    
    verify();
  }  
  public void testList() throws Exception {
    final ParameterizedRowMapper<AnomalyDetector> mapper = new ParameterizedRowMapper<AnomalyDetector>() {
      @Override
      public AnomalyDetector mapRow(ResultSet rs, int rownr) throws SQLException {
        return null;
      }
    };
    List<String> actual = new ArrayList<String>();
    
    jdbcTemplate.query("select name,description from beast_anomaly_detectors order by name", 
        mapper, 
        (Object[])null);
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(actual);
    
    classUnderTest = new AnomalyDetectorManager() {
      protected ParameterizedRowMapper<AnomalyDetector> getMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbcTemplate);
    
    replay();
    
    List<AnomalyDetector> result = classUnderTest.list();
    
    verify();
    assertSame(actual, result);
  }
  
  public void testGet() throws Exception {
    AnomalyDetector detector = new AnomalyDetector();
    
    final ParameterizedRowMapper<AnomalyDetector> mapper = new ParameterizedRowMapper<AnomalyDetector>() {
      @Override
      public AnomalyDetector mapRow(ResultSet rs, int rownr) throws SQLException {
        return null;
      }
    };
    
    jdbcTemplate.queryForObject("select name,description from beast_anomaly_detectors where name=?", 
        mapper,
        new Object[]{"nisse"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(detector);
    
    classUnderTest = new AnomalyDetectorManager() {
      protected ParameterizedRowMapper<AnomalyDetector> getMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbcTemplate);
    
    replay();
    
    AnomalyDetector result = classUnderTest.get("nisse");
    
    verify();
    assertSame(detector, result);
  }

  public void testGet_notFound() throws Exception {
    final ParameterizedRowMapper<AnomalyDetector> mapper = new ParameterizedRowMapper<AnomalyDetector>() {
      @Override
      public AnomalyDetector mapRow(ResultSet rs, int rownr) throws SQLException {
        return null;
      }
    };
    
    jdbcTemplate.queryForObject("select name,description from beast_anomaly_detectors where name=?", 
        mapper,
        new Object[]{"nisse"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setThrowable(new DataAccessException(null) {
      private static final long serialVersionUID = 1L;
    });
    
    classUnderTest = new AnomalyDetectorManager() {
      protected ParameterizedRowMapper<AnomalyDetector> getMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbcTemplate);
    
    replay();
    
    try {
      classUnderTest.get("nisse");
      fail("Expected AnomalyException");
    } catch (AnomalyException e) {
      // pass
    }
    
    verify();
  }
  
  
  public void testRemove() throws Exception {
    jdbcTemplate.update("delete from beast_anomaly_detectors where name=?",
        new Object[]{"uggh"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    replay();
   
    classUnderTest.remove("uggh");
    
    verify();
  }

  public void testRemove_exception() throws Exception {
    jdbcTemplate.update("delete from beast_anomaly_detectors where name=?",
        new Object[]{"uggh"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setThrowable(new DataAccessException(null){
      private static final long serialVersionUID = 1L;
    });
    
    replay();

    try {
      classUnderTest.remove("uggh");
      fail("Expected AnomalyException");
    } catch (AnomalyException e) {
      // pass
    }
    
    verify();
  }

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
  
  public void testGetMapper() throws Exception {
    MockControl resultSetControl = MockControl.createControl(ResultSet.class);
    ResultSet resultSet = (ResultSet)resultSetControl.getMock();
    
    resultSet.getString("name");
    resultSetControl.setReturnValue("nisse");
    resultSet.getString("description");
    resultSetControl.setReturnValue("the description");
    resultSetControl.replay();
    
    ParameterizedRowMapper<AnomalyDetector> mapper = classUnderTest.getMapper();
    AnomalyDetector result = mapper.mapRow(resultSet, 1);

    resultSetControl.verify();
    assertEquals("nisse", result.getName());
    assertEquals("the description", result.getDescription());
  }
}
