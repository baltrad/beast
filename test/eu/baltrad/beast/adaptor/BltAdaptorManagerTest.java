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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.message.IBltMessage;

/**
 * @author Anders Henja
 *
 */
public class BltAdaptorManagerTest extends TestCase {
  private static interface ReregisterMethods {
    public IAdaptor updateAdaptorConfiguration(int adaptor_id, IAdaptorConfiguration configuration);
    public IAdaptor redefineAdaptorConfiguration(int adaptor_id, String type, IAdaptorConfiguration configuration);
  }
 
  private MockControl xyzManagerControl = null;
  private IAdaptorConfigurationManager xyzManager = null;
  private MockControl jdbcControl = null;
  private SimpleJdbcOperations jdbcTemplate = null;
  
  private BltAdaptorManager classUnderTest = null;
  private IAdaptorConfiguration xyzConfiguration = null;
 
  public void setUp() throws Exception {
    super.setUp();
    
    xyzManagerControl = MockControl.createControl(IAdaptorConfigurationManager.class);
    xyzManager = (IAdaptorConfigurationManager)xyzManagerControl.getMock();
    xyzConfiguration = new IAdaptorConfiguration() {
      public String getName() {
        return "SA1";
      }
      public String getType() {
        return "XYZ";
      }
    };
    jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    jdbcTemplate = (SimpleJdbcOperations)jdbcControl.getMock(); 

    classUnderTest = new BltAdaptorManager();
    classUnderTest.typeRegistry = new HashMap<String, IAdaptorConfigurationManager>();
    classUnderTest.typeRegistry.put("XYZ", xyzManager);
    classUnderTest.setJdbcTemplate(jdbcTemplate);
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
    xyzManagerControl = null;
    xyzManager = null;
    xyzConfiguration = null;
    classUnderTest = null;
  }
  
  protected void replay() {
    xyzManagerControl.replay();
    jdbcControl.replay();
  }

  protected void verify() {
    xyzManagerControl.verify();
    jdbcControl.verify();
  }
  
  public void testCreateConfiguration() {
    xyzManager.createConfiguration("SA1");
    xyzManagerControl.setReturnValue(xyzConfiguration);
    
    replay();
    
    // Execute test
    IAdaptorConfiguration result = classUnderTest.createConfiguration("XYZ", "SA1");
    
    // verify
    verify();
    assertSame(result, xyzConfiguration);
  }

  public void testCreateConfiguration_noSuchType() {
    replay();
    
    // Execute test
    try {
      classUnderTest.createConfiguration("ZYX", "SA1");
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
    verify();
  }
  
  public void testSetTypeRegistry() {
    MockControl managerControl = MockControl.createControl(IAdaptorConfigurationManager.class);
    IAdaptorConfigurationManager manager = (IAdaptorConfigurationManager)managerControl.getMock();
    List<IAdaptorConfigurationManager> list = new ArrayList<IAdaptorConfigurationManager>();
    list.add(xyzManager);
    list.add(manager);
    
    xyzManager.getType();
    xyzManagerControl.setReturnValue("XYZ");
    manager.getType();
    managerControl.setReturnValue("ZZZ");
    
    replay();
    managerControl.replay();
   
    // Execute
    classUnderTest.setTypeRegistry(list);
    
    // Verify
    verify();
    managerControl.verify();
    IAdaptorConfigurationManager result = classUnderTest.typeRegistry.get("XYZ");
    assertSame(xyzManager, result);
    result = classUnderTest.typeRegistry.get("ZZZ");
    assertSame(manager, result);
  }
  
  public void testGetAvailableTypes() {
    MockControl managerControl = MockControl.createControl(IAdaptorConfigurationManager.class);
    IAdaptorConfigurationManager manager = (IAdaptorConfigurationManager)managerControl.getMock();
    MockControl manager2Control = MockControl.createControl(IAdaptorConfigurationManager.class);
    IAdaptorConfigurationManager manager2 = (IAdaptorConfigurationManager)managerControl.getMock();
    
    classUnderTest.typeRegistry.put("ZZZ", manager);
    classUnderTest.typeRegistry.put("ABC", manager2);
    
    replay();
    managerControl.replay();
    manager2Control.replay();
    
    // Execute test
    List<String> result = classUnderTest.getAvailableTypes();

    assertEquals(3, result.size());
    assertEquals("ABC", result.get(0));
    assertEquals("XYZ", result.get(1));
    assertEquals("ZZZ", result.get(2));
    
    // Verify
    verify();
    managerControl.verify();
    manager2Control.verify();
  }
  
  public void testRegister() {
    IAdaptor adaptor = new IAdaptor(){
      public String getName() {return "SA1";}
      public String getType() {return "XYZ";}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    };

    jdbcTemplate.update("insert into adaptors (name,type) values (?,?)",
        new Object[]{"SA1","XYZ"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    jdbcTemplate.queryForInt("select adaptor_id from adaptors where name=?", 
        new Object[]{"SA1"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(2);
    xyzManager.store(2, xyzConfiguration);
    xyzManagerControl.setReturnValue(adaptor);

    replay();
    
    // Execute
    IAdaptor result = classUnderTest.register(xyzConfiguration);

    // Verify
    verify();
    assertSame(adaptor, result);
    assertSame(adaptor, classUnderTest.getAdaptor("SA1"));
  }

  public void testRegister_duplicateKey() {
    jdbcTemplate.update("insert into adaptors (name,type) values (?,?)",
        new Object[]{"SA1","XYZ"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setThrowable(new DataRetrievalFailureException("x"));

    replay();
    
    // Execute
    try {
      classUnderTest.register(xyzConfiguration);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }

    // Verify
    verify();
  }

  public void testRegister_failedToStoreAdaptor() {
    jdbcTemplate.update("insert into adaptors (name,type) values (?,?)",
        new Object[]{"SA1","XYZ"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    jdbcTemplate.queryForInt("select adaptor_id from adaptors where name=?", 
        new Object[]{"SA1"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(2);
    xyzManager.store(2, xyzConfiguration);
    xyzManagerControl.setThrowable(new AdaptorException());
    jdbcTemplate.update("delete adaptors where adaptor_id=?",
        new Object[]{2});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    replay();
    
    // Execute
    try {
      classUnderTest.register(xyzConfiguration);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }

    // Verify
    verify();
  }
  
  public void testReregister_sameType() {
    MockControl reregisterControl = MockControl.createControl(ReregisterMethods.class);
    final ReregisterMethods reregister = (ReregisterMethods)reregisterControl.getMock();
    
    Map<String,Object> map = new HashMap<String, Object>();
    map.put("type", "XYZ");
    map.put("adaptor_id", (int)2);
    
    IAdaptor adaptor = new IAdaptor(){
      public String getName() {return "SA1";}
      public String getType() {return "XYZ";}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    };

    jdbcTemplate.queryForMap("select type, adaptor_id from adaptors where name=?",
        new Object[]{"SA1"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(map);
    reregister.updateAdaptorConfiguration(2, xyzConfiguration);
    reregisterControl.setReturnValue(adaptor);
    
    classUnderTest = new BltAdaptorManager() {
      protected IAdaptor updateAdaptorConfiguration(int adaptor_id, IAdaptorConfiguration configuration) {
        return reregister.updateAdaptorConfiguration(adaptor_id, configuration);
      }
      protected IAdaptor redefineAdaptorConfiguration(int adaptor_id, String type, IAdaptorConfiguration configuration) {
        return reregister.redefineAdaptorConfiguration(adaptor_id, type, configuration);
      }
    };
    classUnderTest.setJdbcTemplate(jdbcTemplate); 

    replay();
    reregisterControl.replay();
    
    // Execute
    IAdaptor result = classUnderTest.reregister(xyzConfiguration);

    // Verify
    verify();
    reregisterControl.verify();
    assertSame(adaptor, result);
    assertSame(adaptor, classUnderTest.getAdaptor("SA1"));
  }

  public void testReregister_differentType() {
    MockControl reregisterControl = MockControl.createControl(ReregisterMethods.class);
    final ReregisterMethods reregister = (ReregisterMethods)reregisterControl.getMock();
    
    Map<String,Object> map = new HashMap<String, Object>();
    map.put("type", "ABC");
    map.put("adaptor_id", (int)2);
    
    IAdaptor adaptor = new IAdaptor(){
      public String getName() {return "SA1";}
      public String getType() {return "XYZ";}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    };

    jdbcTemplate.queryForMap("select type, adaptor_id from adaptors where name=?",
        new Object[]{"SA1"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(map);
    reregister.redefineAdaptorConfiguration(2, "ABC", xyzConfiguration);
    reregisterControl.setReturnValue(adaptor);
    
    classUnderTest = new BltAdaptorManager() {
      protected IAdaptor updateAdaptorConfiguration(int adaptor_id, IAdaptorConfiguration configuration) {
        return reregister.updateAdaptorConfiguration(adaptor_id, configuration);
      }
      protected IAdaptor redefineAdaptorConfiguration(int adaptor_id, String type, IAdaptorConfiguration configuration) {
        return reregister.redefineAdaptorConfiguration(adaptor_id, type, configuration);
      }
    };
    classUnderTest.setJdbcTemplate(jdbcTemplate); 

    replay();
    reregisterControl.replay();
    
    // Execute
    IAdaptor result = classUnderTest.reregister(xyzConfiguration);

    // Verify
    verify();
    reregisterControl.verify();
    assertSame(adaptor, result);
    assertSame(adaptor, classUnderTest.getAdaptor("SA1"));
  }
  
  public void testReregister_nonExisting() {
    jdbcTemplate.queryForMap("select type, adaptor_id from adaptors where name=?",
        new Object[]{"SA1"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setThrowable(new DataRetrievalFailureException("x"));

    replay();
    
    // Execute
    try {
      classUnderTest.reregister(xyzConfiguration);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }

    // Verify
    verify();
  }

  public void testUpdateAdaptorConfiguration() throws Exception {
    IAdaptor adaptor = new IAdaptor(){
      public String getName() {return "SA1";}
      public String getType() {return "XYZ";}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    };
    
    xyzManager.update(2, xyzConfiguration);
    xyzManagerControl.setReturnValue(adaptor);
    
    replay();
    
    IAdaptor result = classUnderTest.updateAdaptorConfiguration(2, xyzConfiguration);
    
    verify();
    assertSame(adaptor, result);
  }
  
  public void testRedefineAdaptorConfiguration() throws Exception {
    MockControl oldtypeManagerControl = MockControl.createControl(IAdaptorConfigurationManager.class);
    IAdaptorConfigurationManager oldtypeManager = (IAdaptorConfigurationManager)oldtypeManagerControl.getMock();
    
    IAdaptor adaptor = new IAdaptor(){
      public String getName() {return "SA1";}
      public String getType() {return "XYZ";}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    };

    xyzManager.store(2, xyzConfiguration);
    xyzManagerControl.setReturnValue(adaptor);
    jdbcTemplate.update("update adaptors set type=? where adaptor_id=?", new Object[]{"XYZ", 2});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    oldtypeManager.remove(2);

    classUnderTest.typeRegistry.put("OLDTYPE", oldtypeManager);
    
    replay();
    oldtypeManagerControl.replay();
    
    IAdaptor result = classUnderTest.redefineAdaptorConfiguration(2, "OLDTYPE", xyzConfiguration);
    
    verify();
    oldtypeManagerControl.verify();
    assertSame(adaptor, result);
  }

  public void testRedefineAdaptorConfiguration_failedToStoreNew() throws Exception {
    xyzManager.store(2, xyzConfiguration);
    xyzManagerControl.setThrowable(new AdaptorException());
    
    replay();

    try {
      classUnderTest.redefineAdaptorConfiguration(2, "OLDTYPE", xyzConfiguration);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    verify();
  }

  public void testRedefineAdaptorConfiguration_failedToUpdateType() throws Exception {
    IAdaptor adaptor = new IAdaptor(){
      public String getName() {return "SA1";}
      public String getType() {return "XYZ";}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    };

    xyzManager.store(2, xyzConfiguration);
    xyzManagerControl.setReturnValue(adaptor);
    jdbcTemplate.update("update adaptors set type=? where adaptor_id=?", new Object[]{"XYZ", 2});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setThrowable(new DataRetrievalFailureException("x"));
    xyzManager.remove(2);
    
    replay();

    try {
      classUnderTest.redefineAdaptorConfiguration(2, "OLDTYPE", xyzConfiguration);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    verify();
  }
  
  public void testRedefineAdaptorConfiguration_failedToRemoveOldConfig() throws Exception {
    MockControl oldtypeManagerControl = MockControl.createControl(IAdaptorConfigurationManager.class);
    IAdaptorConfigurationManager oldtypeManager = (IAdaptorConfigurationManager)oldtypeManagerControl.getMock();
    
    IAdaptor adaptor = new IAdaptor(){
      public String getName() {return "SA1";}
      public String getType() {return "XYZ";}
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
    };

    xyzManager.store(2, xyzConfiguration);
    xyzManagerControl.setReturnValue(adaptor);
    jdbcTemplate.update("update adaptors set type=? where adaptor_id=?", new Object[]{"XYZ", 2});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    oldtypeManager.remove(2);
    oldtypeManagerControl.setThrowable(new AdaptorException());

    classUnderTest.typeRegistry.put("OLDTYPE", oldtypeManager);
    
    replay();
    oldtypeManagerControl.replay();
    
    IAdaptor result = classUnderTest.redefineAdaptorConfiguration(2, "OLDTYPE", xyzConfiguration);
    
    verify();
    oldtypeManagerControl.verify();
    assertSame(adaptor, result);
  }
  
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
    
    jdbcTemplate.queryForMap("select adaptor_id,type from adaptors where name=?",
        new Object[]{"SA1"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(found);
    
    xyzManager.remove(10);
    
    jdbcTemplate.update("delete from adaptors where adaptor_id=?",
        new Object[]{10});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);

    classUnderTest.setAdaptors(adaptors);
    
    replay();
    
    // Execute
    classUnderTest.unregister("SA1");

    // Verify
    verify();
    assertEquals(null, adaptors.get("SA1"));
  }  
  
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
    
    replay();
    
    List<IAdaptor> result = classUnderTest.getRegisteredAdaptors();
    
    verify();
    
    assertEquals(2, result.size());
    IAdaptor r1 = result.get(0);
    IAdaptor r2 = result.get(1);
   
    assertTrue((r1.getName().equals("A1") && r2.getName().equals("A2")) ||
        (r1.getName().equals("A2") && r2.getName().equals("A1")));
  }

  public void testGetRegisteredAdaptors_noAdaptors() throws Exception {
    Map<String, IAdaptor> adaptors = new HashMap<String, IAdaptor>();
    classUnderTest.setAdaptors(adaptors);
    
    replay();
    
    List<IAdaptor> result = classUnderTest.getRegisteredAdaptors();
    
    verify();
    
    assertEquals(0, result.size());
  }
  
  public void testGetAdaptorNames() throws Exception {
    Map<String, IAdaptor> adaptors = new HashMap<String, IAdaptor>();
    adaptors.put("ABC", null);
    adaptors.put("DEF", null);
    adaptors.put("BEA", null);
    classUnderTest.setAdaptors(adaptors);
    
    replay();
    
    List<String> result = classUnderTest.getAdaptorNames();
    
    verify();
    
    assertEquals(3, result.size());
    assertEquals("ABC", result.get(0));
    assertEquals("BEA", result.get(1));
    assertEquals("DEF", result.get(2));
  }
  
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
    
    ParameterizedRowMapper<IAdaptor> result = classUnderTest.getAdaptorMapper();
    
    // Verify
    assertSame(adaptor, result.mapRow(null, 0));
  }
  
  public void testdoMapAdaptorRow() throws Exception {
    MockControl rsetControl = MockControl.createControl(ResultSet.class);
    ResultSet rset = (ResultSet)rsetControl.getMock();
    IAdaptor adaptor = new IAdaptor() {
      public void handle(IBltMessage msg) {}
      public void handle(IBltMessage msg, IAdaptorCallback callback) {}
      public String getName() {return null;}
      public String getType() {return "XYZ";}
    };
    rset.getInt("adaptor_id");
    rsetControl.setReturnValue(10);
    rset.getString("name");
    rsetControl.setReturnValue("SA1");
    rset.getString("type");
    rsetControl.setReturnValue("XYZ");
    
    xyzManager.read(10, "SA1");
    xyzManagerControl.setReturnValue(adaptor);
    
    rsetControl.replay();
    replay();
    
    // Execute test
    IAdaptor result = classUnderTest.doMapAdaptorRow(rset, 1);
    
    // Verify result
    rsetControl.verify();
    verify();
    assertSame(adaptor, result);
  }
  
  public void testdoMapAdaptorRow_failedRead() throws Exception {
    MockControl rsetControl = MockControl.createControl(ResultSet.class);
    ResultSet rset = (ResultSet)rsetControl.getMock();
    rset.getInt("adaptor_id");
    rsetControl.setReturnValue(10);
    rset.getString("name");
    rsetControl.setReturnValue("SA1");
    rset.getString("type");
    rsetControl.setReturnValue("XYZ");
    
    xyzManager.read(10, "SA1");
    xyzManagerControl.setThrowable(new AdaptorException());
    
    rsetControl.replay();
    replay();
    
    // Execute test
    IAdaptor result = classUnderTest.doMapAdaptorRow(rset, 1);
    
    // Verify result
    rsetControl.verify();
    verify();
    assertEquals(null, result);
  }
  
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
    
    final ParameterizedRowMapper<IAdaptor> mapper = new ParameterizedRowMapper<IAdaptor>() {
      public IAdaptor mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };
    
    jdbcTemplate.query("select adaptor_id, name, type from adaptors", mapper, (Object[])null);
    jdbcControl.setReturnValue(readAdaptors);
    
    classUnderTest = new BltAdaptorManager() {
      protected ParameterizedRowMapper<IAdaptor> getAdaptorMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbcTemplate);
    classUnderTest.typeRegistry = new HashMap<String, IAdaptorConfigurationManager>();
    classUnderTest.typeRegistry.put("XYZ", xyzManager);
    
    replay();
    
    // Execute test
    classUnderTest.afterPropertiesSet();
   
    // Verify result
    verify();
    assertSame(a1, classUnderTest.getAdaptor("A1"));
    assertSame(a2, classUnderTest.getAdaptor("A2"));
  }
}
