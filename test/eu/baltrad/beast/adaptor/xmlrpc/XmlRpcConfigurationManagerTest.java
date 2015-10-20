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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.Test;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcOperations;

import eu.baltrad.beast.adaptor.AdaptorException;
import eu.baltrad.beast.adaptor.IAdaptor;
import eu.baltrad.beast.adaptor.IAdaptorConfiguration;
import eu.baltrad.beast.message.IBltMessage;

/**
 * @author Anders Henja
 */
public class XmlRpcConfigurationManagerTest extends EasyMockSupport {
  @Test
  public void testGetType() {
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    assertEquals("XMLRPC", classUnderTest.getType());
  }
  
  @Test
  public void testCreateConfiguration() {
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    IAdaptorConfiguration result = classUnderTest.createConfiguration("ABC");
    assertTrue(result.getClass() == XmlRpcAdaptorConfiguration.class);
    assertEquals("ABC", result.getName());
    assertEquals("XMLRPC", result.getType());
  }
  
  @Test
  public void testStore() throws Exception {
    JdbcOperations jdbc = createMock(JdbcOperations.class);
    
    IXmlRpcCommandGenerator generator = new IXmlRpcCommandGenerator() {
      public XmlRpcCommand generate(IBltMessage message) {return null;}
    };
    
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setGenerator(generator);
    
    XmlRpcAdaptorConfiguration conf = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("ABC");
    conf.setURL("http://somepath/somewhere");
    conf.setTimeout(6000);
    
    expect(jdbc.update("insert into beast_adaptors_xmlrpc (adaptor_id, uri, timeout) values (?,?,?)",
        new Object[]{2, "http://somepath/somewhere", (long)6000})).andReturn(0);
    
    replayAll();
    
    // execute test
    IAdaptor result = classUnderTest.store(2, conf);
    
    // verify
    verifyAll();
    assertNotNull(result);
    assertTrue (result.getClass() == XmlRpcAdaptor.class);
    assertEquals("ABC", ((XmlRpcAdaptor)result).getName());
    assertEquals("http://somepath/somewhere", ((XmlRpcAdaptor)result).getUrl());
    assertEquals(6000, ((XmlRpcAdaptor)result).getTimeout());
    assertSame(generator, ((XmlRpcAdaptor)result).getGenerator());
  }
  
  @Test
  public void testStore_cannotStore() throws Exception {
    JdbcOperations jdbc = createMock(JdbcOperations.class);
    
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    
    XmlRpcAdaptorConfiguration conf = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("ABC");
    conf.setURL("http://somepath/somewhere");
    conf.setTimeout(6000);
    
    expect(jdbc.update("insert into beast_adaptors_xmlrpc (adaptor_id, uri, timeout) values (?,?,?)",
        new Object[]{2, "http://somepath/somewhere", (long)6000})).andThrow(new DataRetrievalFailureException("x"));
    
    replayAll();
    
    // execute test
    try {
      classUnderTest.store(2, conf);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
    verifyAll();
  }  

  @Test
  public void testStore_canNotCreateAdaptor() throws Exception {
    JdbcOperations jdbc = createMock(JdbcOperations.class);
    
    IXmlRpcCommandGenerator generator = new IXmlRpcCommandGenerator() {
      public XmlRpcCommand generate(IBltMessage message) {return null;}
    };
    
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setGenerator(generator);
    
    XmlRpcAdaptorConfiguration conf = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("ABC");
    conf.setURL("httpsomebadurl");
    conf.setTimeout(6000);
    
    replayAll();
    
    // execute test
    try {
      classUnderTest.store(2, conf);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      //pass
    }
    
    // verify
    verifyAll();
  }  
  
  @Test
  public void testUpdate() throws Exception {
    JdbcOperations jdbc = createMock(JdbcOperations.class);

    IXmlRpcCommandGenerator generator = new IXmlRpcCommandGenerator() {
      public XmlRpcCommand generate(IBltMessage message) {return null;}
    };
    
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setGenerator(generator);
    
    XmlRpcAdaptorConfiguration conf = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("ABC");
    conf.setURL("http://somepath/somewhere");
    conf.setTimeout(6000);
    
    expect(jdbc.update("update beast_adaptors_xmlrpc set uri=?, timeout=? where adaptor_id=?",
        new Object[]{"http://somepath/somewhere", (long)6000, 2})).andReturn(0);

    replayAll();
    
    // execute test
    IAdaptor result = classUnderTest.update(2, conf);
    
    // verify
    verifyAll();
    assertNotNull(result);
    assertTrue (result.getClass() == XmlRpcAdaptor.class);
    assertEquals("ABC", ((XmlRpcAdaptor)result).getName());
    assertEquals("http://somepath/somewhere", ((XmlRpcAdaptor)result).getUrl());
    assertEquals(6000, ((XmlRpcAdaptor)result).getTimeout());
    assertSame(generator, ((XmlRpcAdaptor)result).getGenerator());
  }   

  @Test
  public void testUpdate_notDefined() throws Exception {
    JdbcOperations jdbc = createMock(JdbcOperations.class);

    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    
    XmlRpcAdaptorConfiguration conf = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("ABC");
    conf.setURL("http://somepath/somewhere");
    conf.setTimeout(6000);
    
    expect(jdbc.update("update beast_adaptors_xmlrpc set uri=?, timeout=? where adaptor_id=?",
        new Object[]{"http://somepath/somewhere", (long)6000, 2})).andThrow(new DataRetrievalFailureException("x"));

    replayAll();
    
    // execute test
    try {
      classUnderTest.update(2, conf);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
    verifyAll();
  }   

  @Test
  public void testUpdate_canNotUpdate() throws Exception {
    JdbcOperations jdbc = createMock(JdbcOperations.class);

    IXmlRpcCommandGenerator generator = new IXmlRpcCommandGenerator() {
      public XmlRpcCommand generate(IBltMessage message) {return null;}
    };
    
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setGenerator(generator);
    
    XmlRpcAdaptorConfiguration conf = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("ABC");
    conf.setURL("httpsomepath");
    conf.setTimeout(6000);
    
    replayAll();
    
    // execute test
    try {
      classUnderTest.update(2, conf);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
    verifyAll();
  }   
  
  @Test
  public void testRemove() throws Exception {
    JdbcOperations jdbc = createMock(JdbcOperations.class);
    
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    
    expect(jdbc.update("delete from beast_adaptors_xmlrpc where adaptor_id=?",
        new Object[]{10})).andReturn(0);
    
    replayAll();
    
    // execute test
    classUnderTest.remove(10);
    
    // verify
    verifyAll();
  }
  
  @Test
  public void testCannotRemove() throws Exception {
    JdbcOperations jdbc = createMock(JdbcOperations.class);
    
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    
    expect(jdbc.update("delete from beast_adaptors_xmlrpc where adaptor_id=?",
        new Object[]{10})).andThrow(new DataRetrievalFailureException("x"));
    
    replayAll();
    
    // execute test
    try {
      classUnderTest.remove(10);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
    verifyAll();
  }  
  
  @Test
  public void testRead() throws Exception {
    JdbcOperations jdbc = createMock(JdbcOperations.class);

    Map<String, Object> foundEntry = new HashMap<String, Object>();
    foundEntry.put("uri", "http://someurl");
    foundEntry.put("timeout", (int)2000);
    
    expect(jdbc.queryForMap("select uri, timeout from beast_adaptors_xmlrpc where adaptor_id=?",
        new Object[]{10})).andReturn(foundEntry);

    XmlRpcCommandGenerator generator = new XmlRpcCommandGenerator();
    XmlRpcConfigurationManager classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setGenerator(generator);
    replayAll();
    
    // Execute test
    XmlRpcAdaptor result = (XmlRpcAdaptor)classUnderTest.read(10, "SA1");

    // verify
    verifyAll();
    assertEquals("SA1", result.getName());
    assertEquals(2000, result.getTimeout());
    assertEquals("http://someurl", result.getUrl());
    assertSame(generator, result.getGenerator());
  }
}
