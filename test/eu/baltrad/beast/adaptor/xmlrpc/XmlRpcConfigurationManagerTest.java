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
package eu.baltrad.beast.adaptor.xmlrpc;

import java.util.HashMap;
import java.util.Map;

import org.easymock.MockControl;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.adaptor.AdaptorException;
import eu.baltrad.beast.adaptor.IAdaptor;
import eu.baltrad.beast.adaptor.IAdaptorConfiguration;
import eu.baltrad.beast.message.IBltMessage;
import junit.framework.TestCase;

/**
 * @author Anders Henja
 *
 */
public class XmlRpcConfigurationManagerTest extends TestCase {
  public void testGetType() {
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    assertEquals("XMLRPC", classUnderTest.getType());
  }
  
  public void testCreateConfiguration() {
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    IAdaptorConfiguration result = classUnderTest.createConfiguration("ABC");
    assertTrue(result.getClass() == XmlRpcAdaptorConfiguration.class);
    assertEquals("ABC", result.getName());
    assertEquals("XMLRPC", result.getType());
  }
  
  public void testStore() throws Exception {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    
    IXmlRpcCommandGenerator generator = new IXmlRpcCommandGenerator() {
      public XmlRpcCommand generate(IBltMessage message) {return null;}
    };
    
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setGenerator(generator);
    
    XmlRpcAdaptorConfiguration conf = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("ABC");
    conf.setURL("http://somepath/somewhere");
    conf.setTimeout(6000);
    
    jdbc.update("insert into beast_adaptors_xmlrpc (adaptor_id, uri, timeout) values (?,?,?)",
        new Object[]{2, "http://somepath/somewhere", (long)6000});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    jdbcControl.replay();
    
    // execute test
    IAdaptor result = classUnderTest.store(2, conf);
    
    // verify
    jdbcControl.verify();
    assertNotNull(result);
    assertTrue (result.getClass() == XmlRpcAdaptor.class);
    assertEquals("ABC", ((XmlRpcAdaptor)result).getName());
    assertEquals("http://somepath/somewhere", ((XmlRpcAdaptor)result).getUrl());
    assertEquals(6000, ((XmlRpcAdaptor)result).getTimeout());
    assertSame(generator, ((XmlRpcAdaptor)result).getGenerator());
  }
  
  public void testStore_cannotStore() throws Exception {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    
    XmlRpcAdaptorConfiguration conf = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("ABC");
    conf.setURL("http://somepath/somewhere");
    conf.setTimeout(6000);
    
    jdbc.update("insert into beast_adaptors_xmlrpc (adaptor_id, uri, timeout) values (?,?,?)",
        new Object[]{2, "http://somepath/somewhere", (long)6000});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setThrowable(new DataRetrievalFailureException("x"));
    
    jdbcControl.replay();
    
    // execute test
    try {
      classUnderTest.store(2, conf);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
    jdbcControl.verify();
  }  

  public void testStore_canNotCreateAdaptor() throws Exception {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    
    IXmlRpcCommandGenerator generator = new IXmlRpcCommandGenerator() {
      public XmlRpcCommand generate(IBltMessage message) {return null;}
    };
    
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setGenerator(generator);
    
    XmlRpcAdaptorConfiguration conf = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("ABC");
    conf.setURL("httpsomebadurl");
    conf.setTimeout(6000);
    
    jdbcControl.replay();
    
    // execute test
    try {
      classUnderTest.store(2, conf);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      //pass
    }
    
    // verify
    jdbcControl.verify();
  }  
  
  public void testUpdate() throws Exception {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbc = (SimpleJdbcOperations)jdbcControl.getMock();

    IXmlRpcCommandGenerator generator = new IXmlRpcCommandGenerator() {
      public XmlRpcCommand generate(IBltMessage message) {return null;}
    };
    
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setGenerator(generator);
    
    XmlRpcAdaptorConfiguration conf = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("ABC");
    conf.setURL("http://somepath/somewhere");
    conf.setTimeout(6000);
    
    jdbc.update("update beast_adaptors_xmlrpc set uri=?, timeout=? where adaptor_id=?",
        new Object[]{"http://somepath/somewhere", (long)6000, 2});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    jdbcControl.replay();
    
    // execute test
    IAdaptor result = classUnderTest.update(2, conf);
    
    // verify
    jdbcControl.verify();
    assertNotNull(result);
    assertTrue (result.getClass() == XmlRpcAdaptor.class);
    assertEquals("ABC", ((XmlRpcAdaptor)result).getName());
    assertEquals("http://somepath/somewhere", ((XmlRpcAdaptor)result).getUrl());
    assertEquals(6000, ((XmlRpcAdaptor)result).getTimeout());
    assertSame(generator, ((XmlRpcAdaptor)result).getGenerator());
  }   

  public void testUpdate_notDefined() throws Exception {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbc = (SimpleJdbcOperations)jdbcControl.getMock();

    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    
    XmlRpcAdaptorConfiguration conf = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("ABC");
    conf.setURL("http://somepath/somewhere");
    conf.setTimeout(6000);
    
    jdbc.update("update beast_adaptors_xmlrpc set uri=?, timeout=? where adaptor_id=?",
        new Object[]{"http://somepath/somewhere", (long)6000, 2});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setThrowable(new DataRetrievalFailureException("x"));

    jdbcControl.replay();
    
    // execute test
    try {
      classUnderTest.update(2, conf);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
    jdbcControl.verify();
  }   

  public void testUpdate_canNotUpdate() throws Exception {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbc = (SimpleJdbcOperations)jdbcControl.getMock();

    IXmlRpcCommandGenerator generator = new IXmlRpcCommandGenerator() {
      public XmlRpcCommand generate(IBltMessage message) {return null;}
    };
    
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setGenerator(generator);
    
    XmlRpcAdaptorConfiguration conf = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("ABC");
    conf.setURL("httpsomepath");
    conf.setTimeout(6000);
    
    jdbcControl.replay();
    
    // execute test
    try {
      classUnderTest.update(2, conf);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
    jdbcControl.verify();
  }   
  
  public void testRemove() throws Exception {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    
    jdbc.update("delete from beast_adaptors_xmlrpc where adaptor_id=?",
        new Object[]{10});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    jdbcControl.replay();
    
    // execute test
    classUnderTest.remove(10);
    
    // verify
    jdbcControl.verify();
  }
  
  public void testCannotRemove() throws Exception {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    
    jdbc.update("delete from beast_adaptors_xmlrpc where adaptor_id=?",
        new Object[]{10});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setThrowable(new DataRetrievalFailureException("x"));
    
    jdbcControl.replay();
    
    // execute test
    try {
      classUnderTest.remove(10);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
    jdbcControl.verify();
  }  
  
  public void testRead() throws Exception {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbc = (SimpleJdbcOperations)jdbcControl.getMock();

    Map<String, Object> foundEntry = new HashMap<String, Object>();
    foundEntry.put("uri", "http://someurl");
    foundEntry.put("timeout", (int)2000);
    
    jdbc.queryForMap("select uri, timeout from beast_adaptors_xmlrpc where adaptor_id=?", new Object[]{10});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(foundEntry);

    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);

    jdbcControl.replay();
    
    // Execute test
    XmlRpcAdaptor result = (XmlRpcAdaptor)classUnderTest.read(10, "SA1");

    // verify
    jdbcControl.verify();
    assertEquals("SA1", result.getName());
    assertEquals(2000, result.getTimeout());
    assertEquals("http://someurl", result.getUrl());
  }
}
