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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.router.Route;

/**
 * @author Anders Henja
 *
 */
public class BltAdaptorManagerTest extends TestCase {
  private MockControl xyzManagerControl = MockControl.createControl(IAdaptorConfigurationManager.class);
  private IAdaptorConfigurationManager xyzManager = (IAdaptorConfigurationManager)xyzManagerControl.getMock();
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

    classUnderTest = new BltAdaptorManager();
    classUnderTest.typeRegistry = new HashMap<String, IAdaptorConfigurationManager>();
    classUnderTest.typeRegistry.put("XYZ", xyzManager);
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
    xyzManagerControl = null;
    xyzManager = null;
    xyzConfiguration = null;
    classUnderTest = null;
  }
  
  public void testCreateConfiguration() {
    xyzManager.createConfiguration("SA1");
    xyzManagerControl.setReturnValue(xyzConfiguration);
    
    xyzManagerControl.replay();
    
    // Execute test
    IAdaptorConfiguration result = classUnderTest.createConfiguration("XYZ", "SA1");
    
    // verify
    xyzManagerControl.verify();
    assertSame(result, xyzConfiguration);
  }

  public void testCreateConfiguration_noSuchType() {
    xyzManagerControl.replay();
    
    // Execute test
    try {
      classUnderTest.createConfiguration("ZYX", "SA1");
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
    xyzManagerControl.verify();
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
    
    xyzManagerControl.replay();
    managerControl.replay();
   
    // Execute
    classUnderTest.setTypeRegistry(list);
    
    // Verify
    xyzManagerControl.verify();
    managerControl.verify();
    IAdaptorConfigurationManager result = classUnderTest.typeRegistry.get("XYZ");
    assertSame(xyzManager, result);
    result = classUnderTest.typeRegistry.get("ZZZ");
    assertSame(manager, result);
  }
  
  public void testGetAvailableTypes() {
    MockControl managerControl = MockControl.createControl(IAdaptorConfigurationManager.class);
    IAdaptorConfigurationManager manager = (IAdaptorConfigurationManager)managerControl.getMock();
    
    classUnderTest.typeRegistry.put("ZZZ", manager);
    
    xyzManagerControl.replay();
    managerControl.replay();
    
    // Execute test
    Set<String> result = classUnderTest.getAvailableTypes();

    assertEquals(2, result.size());
    assertTrue(result.contains("XYZ"));
    assertTrue(result.contains("ZZZ"));
    
    // Verify
    xyzManagerControl.verify();
    managerControl.verify();
  }
  
  public void testRegister() {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbcTemplate = (SimpleJdbcOperations)jdbcControl.getMock(); 
    
    IAdaptor adaptor = new IAdaptor(){
      public String getName() {
        return null;
      }
      public void handle(Route route) {
      }
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

    classUnderTest.setJdbcTemplate(jdbcTemplate); 

    xyzManagerControl.replay();
    jdbcControl.replay();
    
    // Execute
    IAdaptor result = classUnderTest.register(xyzConfiguration);

    // Verify
    xyzManagerControl.verify();
    jdbcControl.verify();
    assertSame(result, adaptor);
  }

  public void testRegister_duplicateKey() {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbcTemplate = (SimpleJdbcOperations)jdbcControl.getMock(); 
    
    jdbcTemplate.update("insert into adaptors (name,type) values (?,?)",
        new Object[]{"SA1","XYZ"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setThrowable(new DataRetrievalFailureException("x"));

    classUnderTest.setJdbcTemplate(jdbcTemplate); 

    xyzManagerControl.replay();
    jdbcControl.replay();
    
    // Execute
    try {
      classUnderTest.register(xyzConfiguration);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }

    // Verify
    xyzManagerControl.verify();
    jdbcControl.verify();
  }

  public void testRegister_failedToStoreAdaptor() {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbcTemplate = (SimpleJdbcOperations)jdbcControl.getMock(); 
    
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
    
    classUnderTest.setJdbcTemplate(jdbcTemplate); 

    xyzManagerControl.replay();
    jdbcControl.replay();
    
    // Execute
    try {
      classUnderTest.register(xyzConfiguration);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }

    // Verify
    xyzManagerControl.verify();
    jdbcControl.verify();
  }
  
  public void testUnregister() {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbcTemplate = (SimpleJdbcOperations)jdbcControl.getMock(); 

    Map<String, Object> found = new HashMap<String, Object>();
    found.put("type", "XYZ");
    found.put("adaptor_id", new Integer(10));
    
    Map<String, IAdaptor> adaptors = new HashMap<String, IAdaptor>();
    adaptors.put("SA1", new IAdaptor() {
      public String getName() {return null;}
      public void handle(Route route) {}
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
    classUnderTest.setJdbcTemplate(jdbcTemplate); 
    
    xyzManagerControl.replay();
    jdbcControl.replay();
    
    // Execute
    classUnderTest.unregister("SA1");

    // Verify
    xyzManagerControl.verify();
    jdbcControl.verify();
    assertEquals(null, adaptors.get("SA1"));
  }  
}
