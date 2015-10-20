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
package eu.baltrad.beast.adaptor;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

import eu.baltrad.beast.message.IBltMessage;

/**
 * @author Anders Henja
 *
 */
public class BltAdaptorManagerTest extends EasyMockSupport {
  private static interface ReregisterMethods {
    public IAdaptor updateAdaptorConfiguration(int adaptor_id, IAdaptorConfiguration configuration);
    public IAdaptor redefineAdaptorConfiguration(int adaptor_id, String type, IAdaptorConfiguration configuration);
  }
 
  private IAdaptorConfigurationManager xyzManager = null;
  private JdbcOperations jdbcTemplate = null;
  
  private BltAdaptorManager classUnderTest = null;
  private IAdaptorConfiguration xyzConfiguration = null;

  @Before
  public void setUp() throws Exception {
    xyzManager = createMock(IAdaptorConfigurationManager.class);
    xyzConfiguration = new IAdaptorConfiguration() {
      public String getName() {
        return "SA1";
      }
      public String getType() {
        return "XYZ";
      }
    };
    jdbcTemplate = createMock(JdbcOperations.class); 

    classUnderTest = new BltAdaptorManager();
    classUnderTest.getTypeRegistry().put("XYZ", xyzManager);
    classUnderTest.setJdbcTemplate(jdbcTemplate);
  }
  
  @After
  public void tearDown() throws Exception {
    xyzManager = null;
    xyzConfiguration = null;
    classUnderTest = null;
  }
  
  @Test
  public void testCreateConfiguration() {
    expect(xyzManager.createConfiguration("SA1")).andReturn(xyzConfiguration);
    
    replayAll();
    
    // Execute test
    IAdaptorConfiguration result = classUnderTest.createConfiguration("XYZ", "SA1");
    
    // verify
    verifyAll();
    assertSame(result, xyzConfiguration);
  }

  @Test
  public void testCreateConfiguration_noSuchType() {
    replayAll();
    
    // Execute test
    try {
      classUnderTest.createConfiguration("ZYX", "SA1");
      Assert.fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
    verifyAll();
  }
  
  @Test
  public void testSetTypeRegistry() {
    IAdaptorConfigurationManager manager = createMock(IAdaptorConfigurationManager.class);
    List<IAdaptorConfigurationManager> list = new ArrayList<IAdaptorConfigurationManager>();
    list.add(xyzManager);
    list.add(manager);
    
    expect(xyzManager.getType()).andReturn("XYZ");
    expect(manager.getType()).andReturn("ZZZ");
    
    replayAll();
   
    // Execute
    classUnderTest.setTypes(list);
    
    // Verify
    verifyAll();
    IAdaptorConfigurationManager result = classUnderTest.getTypeRegistry().get("XYZ");
    assertSame(xyzManager, result);
    result = classUnderTest.getTypeRegistry().get("ZZZ");
    assertSame(manager, result);
  }

  @Test
  public void testGetAvailableTypes() {
    IAdaptorConfigurationManager manager = createMock(IAdaptorConfigurationManager.class);
    IAdaptorConfigurationManager manager2 = createMock(IAdaptorConfigurationManager.class);
    
    classUnderTest.getTypeRegistry().put("ZZZ", manager);
    classUnderTest.getTypeRegistry().put("ABC", manager2);
    
    replayAll();
    
    // Execute test
    List<String> result = classUnderTest.getAvailableTypes();

    assertEquals(3, result.size());
    assertEquals("ABC", result.get(0));
    assertEquals("XYZ", result.get(1));
    assertEquals("ZZZ", result.get(2));
    
    // Verify
    verifyAll();
  }
  
  @Test
  public void testRegister() {
    IAdaptor adaptor = new IAdaptor(){
      public String getName() {return "SA1";}
      public String getType() {return "XYZ";}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    };

    expect(jdbcTemplate.update("insert into beast_adaptors (name,type) values (?,?)",
           new Object[]{"SA1","XYZ"}))
      .andReturn(0);
    
    expect(jdbcTemplate.queryForObject("select adaptor_id from beast_adaptors where name=?",
           int.class,
           "SA1"))
      .andReturn(2);
    
    expect(xyzManager.store(2, xyzConfiguration)).andReturn(adaptor);
    
    replayAll();
    
    // Execute
    IAdaptor result = classUnderTest.register(xyzConfiguration);

    // Verify
    verifyAll();
    assertSame(adaptor, result);
    assertSame(adaptor, classUnderTest.getAdaptor("SA1"));
  }
  
  @Test
  public void testRegister_duplicateKey() {
    expect(jdbcTemplate.update("insert into beast_adaptors (name,type) values (?,?)",
        new Object[]{"SA1","XYZ"})).andThrow(new DataRetrievalFailureException("x"));

    replayAll();
    
    // Execute
    try {
      classUnderTest.register(xyzConfiguration);
      Assert.fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }

    // Verify
    verifyAll();
  }
  
  @Test
  public void testReregister_sameType() {
    final ReregisterMethods reregister = createMock(ReregisterMethods.class);
    
    Map<String,Object> map = new HashMap<String, Object>();
    map.put("type", "XYZ");
    map.put("adaptor_id", (int)2);
    
    IAdaptor adaptor = new IAdaptor(){
      public String getName() {return "SA1";}
      public String getType() {return "XYZ";}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    };

    expect(jdbcTemplate.queryForMap("select type, adaptor_id from beast_adaptors where name=?",
        new Object[]{"SA1"})).andReturn(map);

    expect(reregister.updateAdaptorConfiguration(2, xyzConfiguration)).andReturn(adaptor);
    
    classUnderTest = new BltAdaptorManager() {
      protected IAdaptor updateAdaptorConfiguration(int adaptor_id, IAdaptorConfiguration configuration) {
        return reregister.updateAdaptorConfiguration(adaptor_id, configuration);
      }
      protected IAdaptor redefineAdaptorConfiguration(int adaptor_id, String type, IAdaptorConfiguration configuration) {
        return reregister.redefineAdaptorConfiguration(adaptor_id, type, configuration);
      }
    };
    classUnderTest.setJdbcTemplate(jdbcTemplate); 

    replayAll();
    
    // Execute
    IAdaptor result = classUnderTest.reregister(xyzConfiguration);

    // Verify
    verifyAll();
    assertSame(adaptor, result);
    assertSame(adaptor, classUnderTest.getAdaptor("SA1"));
  }

  @Test
  public void testReregister_differentType() {
    final ReregisterMethods reregister = createMock(ReregisterMethods.class);
    
    Map<String,Object> map = new HashMap<String, Object>();
    map.put("type", "ABC");
    map.put("adaptor_id", (int)2);
    
    IAdaptor adaptor = new IAdaptor(){
      public String getName() {return "SA1";}
      public String getType() {return "XYZ";}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    };

    expect(jdbcTemplate.queryForMap("select type, adaptor_id from beast_adaptors where name=?",
        new Object[]{"SA1"})).andReturn(map);

    expect(reregister.redefineAdaptorConfiguration(2, "ABC", xyzConfiguration)).andReturn(adaptor);
    
    classUnderTest = new BltAdaptorManager() {
      protected IAdaptor updateAdaptorConfiguration(int adaptor_id, IAdaptorConfiguration configuration) {
        return reregister.updateAdaptorConfiguration(adaptor_id, configuration);
      }
      protected IAdaptor redefineAdaptorConfiguration(int adaptor_id, String type, IAdaptorConfiguration configuration) {
        return reregister.redefineAdaptorConfiguration(adaptor_id, type, configuration);
      }
    };
    classUnderTest.setJdbcTemplate(jdbcTemplate); 

    replayAll();
    
    // Execute
    IAdaptor result = classUnderTest.reregister(xyzConfiguration);

    // Verify
    verifyAll();
    assertSame(adaptor, result);
    assertSame(adaptor, classUnderTest.getAdaptor("SA1"));
  }

  @Test
  public void testReregister_nonExisting() {
    expect(jdbcTemplate.queryForMap("select type, adaptor_id from beast_adaptors where name=?",
        new Object[]{"SA1"})).andThrow(new DataRetrievalFailureException("x"));

    replayAll();
    
    // Execute
    try {
      classUnderTest.reregister(xyzConfiguration);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }

    // Verify
    verifyAll();
  }

  @Test
  public void testUpdateAdaptorConfiguration() throws Exception {
    IAdaptor adaptor = new IAdaptor(){
      public String getName() {return "SA1";}
      public String getType() {return "XYZ";}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    };
    
    expect(xyzManager.update(2, xyzConfiguration)).andReturn(adaptor);
    
    replayAll();
    
    IAdaptor result = classUnderTest.updateAdaptorConfiguration(2, xyzConfiguration);
    
    verifyAll();
    assertSame(adaptor, result);
  }
  
  @Test
  public void testRedefineAdaptorConfiguration() throws Exception {
    IAdaptorConfigurationManager oldtypeManager = createMock(IAdaptorConfigurationManager.class);
    
    IAdaptor adaptor = new IAdaptor(){
      public String getName() {return "SA1";}
      public String getType() {return "XYZ";}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    };

    expect(xyzManager.store(2, xyzConfiguration)).andReturn(adaptor);
    expect(jdbcTemplate.update("update beast_adaptors set type=? where adaptor_id=?", new Object[]{"XYZ", 2}))
      .andReturn(0);
    oldtypeManager.remove(2);

    classUnderTest.getTypeRegistry().put("OLDTYPE", oldtypeManager);
    
    replayAll();
    
    IAdaptor result = classUnderTest.redefineAdaptorConfiguration(2, "OLDTYPE", xyzConfiguration);
    
    verifyAll();
    assertSame(adaptor, result);
  }

  @Test
  public void testRedefineAdaptorConfiguration_failedToStoreNew() throws Exception {
    expect(xyzManager.store(2, xyzConfiguration)).andThrow(new AdaptorException());
    
    replayAll();

    try {
      classUnderTest.redefineAdaptorConfiguration(2, "OLDTYPE", xyzConfiguration);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    verifyAll();
  }

  @Test
  public void testRedefineAdaptorConfiguration_failedToUpdateType() throws Exception {
    IAdaptor adaptor = new IAdaptor(){
      public String getName() {return "SA1";}
      public String getType() {return "XYZ";}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    };

    expect(xyzManager.store(2, xyzConfiguration)).andReturn(adaptor);
    expect(jdbcTemplate.update("update beast_adaptors set type=? where adaptor_id=?",
        new Object[]{"XYZ", 2})).andThrow(new DataRetrievalFailureException("x"));
    
    replayAll();

    try {
      classUnderTest.redefineAdaptorConfiguration(2, "OLDTYPE", xyzConfiguration);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    verifyAll();
  }
  
  @Test
  public void testRedefineAdaptorConfiguration_failedToRemoveOldConfig() throws Exception {
    IAdaptorConfigurationManager oldtypeManager = createMock(IAdaptorConfigurationManager.class);
    
    IAdaptor adaptor = new IAdaptor(){
      public String getName() {return "SA1";}
      public String getType() {return "XYZ";}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    };

    expect(xyzManager.store(2, xyzConfiguration)).andReturn(adaptor);
    expect(jdbcTemplate.update("update beast_adaptors set type=? where adaptor_id=?", 
        new Object[]{"XYZ", 2})).andReturn(0);
    oldtypeManager.remove(2);
    EasyMock.expectLastCall().andThrow(new AdaptorException());

    classUnderTest.getTypeRegistry().put("OLDTYPE", oldtypeManager);
    
    replayAll();
    
    IAdaptor result = classUnderTest.redefineAdaptorConfiguration(2, "OLDTYPE", xyzConfiguration);
    
    verifyAll();
    assertSame(adaptor, result);
  }
  
  @Test
  public void testUnregister() {
    Map<String, Object> found = new HashMap<String, Object>();
    found.put("type", "XYZ");
    found.put("adaptor_id", new Integer(10));
    
    Map<String, IAdaptor> adaptors = new HashMap<String, IAdaptor>();
    adaptors.put("SA1", new IAdaptor() {
      public String getName() {return null;}
      public String getType() {return "XYZ";}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    });
    
    expect(jdbcTemplate.queryForMap("select adaptor_id,type from beast_adaptors where name=?",
        new Object[]{"SA1"})).andReturn(found);
    
    xyzManager.remove(10);
    
    expect(jdbcTemplate.update("delete from beast_adaptors where adaptor_id=?",
        new Object[]{10})).andReturn(0);

    classUnderTest.setAdaptors(adaptors);
    
    replayAll();
    
    // Execute
    classUnderTest.unregister("SA1");

    // Verify
    verifyAll();
    assertEquals(null, adaptors.get("SA1"));
  }  
  
  @Test
  public void testGetRegisteredAdaptors() throws Exception {
    IAdaptor a1 = new IAdaptor() {
      public String getName() {return "A1";}
      public String getType() {return null;}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    };
    IAdaptor a2 = new IAdaptor() {
      public String getName() {return "A2";}
      public String getType() {return null;}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    };
    Map<String, IAdaptor> adaptors = new HashMap<String, IAdaptor>();
    adaptors.put("A1", a1);
    adaptors.put("A2", a2);
    classUnderTest.setAdaptors(adaptors);
    
    replayAll();
    
    List<IAdaptor> result = classUnderTest.getRegisteredAdaptors();
    
    verifyAll();
    
    assertEquals(2, result.size());
    IAdaptor r1 = result.get(0);
    IAdaptor r2 = result.get(1);
   
    assertTrue((r1.getName().equals("A1") && r2.getName().equals("A2")) ||
        (r1.getName().equals("A2") && r2.getName().equals("A1")));
  }

  @Test
  public void testGetRegisteredAdaptors_noAdaptors() throws Exception {
    Map<String, IAdaptor> adaptors = new HashMap<String, IAdaptor>();
    classUnderTest.setAdaptors(adaptors);
    
    replayAll();
    
    List<IAdaptor> result = classUnderTest.getRegisteredAdaptors();
    
    verifyAll();
    
    assertEquals(0, result.size());
  }
  
  @Test
  public void testGetAdaptorNames() throws Exception {
    Map<String, IAdaptor> adaptors = new HashMap<String, IAdaptor>();
    adaptors.put("ABC", null);
    adaptors.put("DEF", null);
    adaptors.put("BEA", null);
    classUnderTest.setAdaptors(adaptors);
    
    replayAll();
    
    List<String> result = classUnderTest.getAdaptorNames();
    
    verifyAll();
    
    assertEquals(3, result.size());
    assertEquals("ABC", result.get(0));
    assertEquals("BEA", result.get(1));
    assertEquals("DEF", result.get(2));
  }
  
  @Test
  public void testGetAdaptorMapper() throws Exception {
    final IAdaptor adaptor = new IAdaptor() {
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
      public String getName() {return null;}
      public String getType() {return "XYZ";}
    };
    
    classUnderTest = new BltAdaptorManager() {
      protected IAdaptor doMapAdaptorRow(ResultSet rs, int rownum) throws SQLException {
        return adaptor;
      }
    };
    
    RowMapper<IAdaptor> result = classUnderTest.getAdaptorMapper();
    
    // Verify
    assertSame(adaptor, result.mapRow(null, 0));
  }
  
  @Test
  public void testdoMapAdaptorRow() throws Exception {
    ResultSet rset = createMock(ResultSet.class);
    IAdaptor adaptor = new IAdaptor() {
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
      public String getName() {return null;}
      public String getType() {return "XYZ";}
    };
    expect(rset.getInt("adaptor_id")).andReturn(10);
    expect(rset.getString("name")).andReturn("SA1");
    expect(rset.getString("type")).andReturn("XYZ");
    expect(xyzManager.read(10, "SA1")).andReturn(adaptor);
    
    replayAll();
    
    // Execute test
    IAdaptor result = classUnderTest.doMapAdaptorRow(rset, 1);
    
    // Verify result
    verifyAll();
    assertSame(adaptor, result);
  }
  
  @Test
  public void testdoMapAdaptorRow_failedRead() throws Exception {
    ResultSet rset = createMock(ResultSet.class);
    expect(rset.getInt("adaptor_id")).andReturn(10);
    expect(rset.getString("name")).andReturn("SA1");
    expect(rset.getString("type")).andReturn("XYZ");
    expect(xyzManager.read(10, "SA1")).andThrow(new AdaptorException());
    
    replayAll();
    
    // Execute test
    IAdaptor result = classUnderTest.doMapAdaptorRow(rset, 1);
    
    // Verify result
    verifyAll();
    assertEquals(null, result);
  }
  
  @Test
  public void testAfterPropertiesSet() throws Exception {
    List<IAdaptor> readAdaptors = new ArrayList<IAdaptor>();
    IAdaptor a1 = new IAdaptor() {
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
      public String getName() {return "A1";}
      public String getType() {return "XYZ";}
    };
    IAdaptor a2 = new IAdaptor() {
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
      public String getName() {return "A2";}
      public String getType() {return "XYZ";}
    };
    readAdaptors.add(a1);
    readAdaptors.add(a2);
    
    final RowMapper<IAdaptor> mapper = new RowMapper<IAdaptor>() {
      public IAdaptor mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };
    
    expect(jdbcTemplate.query("select adaptor_id, name, type from beast_adaptors", mapper, 
        (Object[])null)).andReturn(readAdaptors);
    
    classUnderTest = new BltAdaptorManager() {
      protected RowMapper<IAdaptor> getAdaptorMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbcTemplate);
    classUnderTest.getTypeRegistry().put("XYZ", xyzManager);
    
    replayAll();
    
    // Execute test
    classUnderTest.afterPropertiesSet();
   
    // Verify result
    verifyAll();
    assertSame(a1, classUnderTest.getAdaptor("A1"));
    assertSame(a2, classUnderTest.getAdaptor("A2"));
  }
}
