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
import static org.junit.Assert.*;

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

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.router.IMultiRoutedMessage;
import eu.baltrad.beast.router.IRoutedMessage;

/**
 * @author Anders Henja
 */
public class BltAdaptorManagerHandleTest extends EasyMockSupport {
  private IAdaptor adaptor1 = null;
  private IAdaptor adaptor2 = null;
  private BltAdaptorManager classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    adaptor1 = createMock(IAdaptor.class);
    adaptor2 = createMock(IAdaptor.class);
    Map<String, IAdaptor> adaptors = new HashMap<String, IAdaptor>();
    adaptors.put("A1", adaptor1);
    adaptors.put("A2", adaptor2);
    classUnderTest = new BltAdaptorManager();
    classUnderTest.setAdaptors(adaptors);
  }
  
  @After
  public void tearDown() throws Exception {
    adaptor1 = null;
    adaptor2 = null;
    classUnderTest = null;
  }

  @Test
  public void testHandle_MultiRouted() throws Exception {
    IMultiRoutedMessage message = createMock(IMultiRoutedMessage.class);
    List<String> destinations = new ArrayList<String>();
    destinations.add("A2");
    IBltMessage msg = new IBltMessage() {};
    
    // Mock setup
    expect(message.getDestinations()).andReturn(destinations);
    expect(message.getMessage()).andReturn(msg);
    adaptor2.handle(msg);

    replayAll();
    
    classUnderTest.handle(message);
    
    verifyAll();
  }

  @Test
  public void testHandle_MultiRouted_nullMessage() {
    IMultiRoutedMessage message = createMock(IMultiRoutedMessage.class);
    
    // Mock setup
    expect(message.getMessage()).andReturn(null);
    
    replayAll();
    
    classUnderTest.handle(message);
    
    verifyAll();
  }
  
  @Test
  public void testHandle_MultiRouted_noAdaptor() throws Exception {
    IMultiRoutedMessage message = createMock(IMultiRoutedMessage.class);

    List<String> destinations = new ArrayList<String>();
    destinations.add("A3");
    IBltMessage msg = new IBltMessage() {};
    
    // Mock setup
    expect(message.getDestinations()).andReturn(destinations);
    expect(message.getMessage()).andReturn(msg);

    replayAll();
    
    classUnderTest.handle(message);
    
    verifyAll();
  }

  @Test
  public void testHandle_MultiRouted_adaptorThrowsException() throws Exception {
    IMultiRoutedMessage message = createMock(IMultiRoutedMessage.class);
    AdaptorException exceptionMock = createMock(AdaptorException.class);

    List<String> destinations = new ArrayList<String>();
    destinations.add("A1");
    destinations.add("A2");
    IBltMessage msg = new IBltMessage() {};
    
    // Mock setup
    expect(message.getDestinations()).andReturn(destinations);
    expect(message.getMessage()).andReturn(msg);
    adaptor1.handle(msg);
    adaptor2.handle(msg);
    EasyMock.expectLastCall().andThrow(exceptionMock);
    exceptionMock.printStackTrace();
    EasyMock.expectLastCall();

    replayAll();

    classUnderTest.handle(message);
    
    verifyAll();
  }

  @Test
  public void testHandle() throws Exception {
    IRoutedMessage routedMessage = createMock(IRoutedMessage.class);
    
    IBltMessage msg = new IBltMessage() {};
    
    expect(routedMessage.getDestination()).andReturn("A2");
    expect(routedMessage.getMessage()).andReturn(msg);
    adaptor2.handle(msg);

    replayAll();
    
    // execute test
    classUnderTest.handle(routedMessage);
    
    // verify
    verifyAll();
  }
  
  @Test
  public void testHandle_nullMessage() throws Exception {
    IRoutedMessage routedMessage = createMock(IRoutedMessage.class);
    
    expect(routedMessage.getDestination()).andReturn("A2");
    expect(routedMessage.getMessage()).andReturn(null);

    replayAll();
    
    // execute test
    classUnderTest.handle(routedMessage);
    
    // verify
    verifyAll();
  }

  @Test
  public void testHandle_noMatchingAdaptor() throws Exception {
    IRoutedMessage routedMessage = createMock(IRoutedMessage.class);
    expect(routedMessage.getDestination()).andReturn("A3");

    replayAll();
    
    // execute test
    try {
      classUnderTest.handle(routedMessage);
      Assert.fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
    verifyAll();
  }
  
  @Test
  public void testHandle_withCallback() throws Exception {
    IRoutedMessage routedMessage = createMock(IRoutedMessage.class);
    
    IAdaptorCallback cb = new IAdaptorCallback(){
      public void error(IBltMessage message, Throwable t) {}
      public void success(IBltMessage message, Object result) {}
      public void timeout(IBltMessage message) {}
    };

    IBltMessage msg = new IBltMessage() {};
    
    expect(routedMessage.getDestination()).andReturn("A2");
    expect(routedMessage.getMessage()).andReturn(msg);
    adaptor2.handle(msg, cb);

    replayAll();
    
    // execute test
    classUnderTest.handle(routedMessage, cb);
    
    // verify
    verifyAll();
  }

  @Test
  public void testHandle_withCallback_nullMessage() throws Exception {
    IRoutedMessage routedMessage = createMock(IRoutedMessage.class);
    
    IAdaptorCallback cb = new IAdaptorCallback(){
      public void error(IBltMessage message, Throwable t) {}
      public void success(IBltMessage message, Object result) {}
      public void timeout(IBltMessage message) {}
    };

    expect(routedMessage.getDestination()).andReturn("A2");
    expect(routedMessage.getMessage()).andReturn(null);
   
    replayAll();
    
    // execute test
    classUnderTest.handle(routedMessage, cb);
    
    // verify
    verifyAll();
  }
  
  @Test
  public void testHandle_withCallback_noMatchingAdaptor() throws Exception {
    IRoutedMessage routedMessage = createMock(IRoutedMessage.class);
    
    IAdaptorCallback cb = new IAdaptorCallback(){
      public void error(IBltMessage message, Throwable t) {}
      public void success(IBltMessage message, Object result) {}
      public void timeout(IBltMessage message) {}
    };

    expect(routedMessage.getDestination()).andReturn("A3");

    replayAll();
    
    // execute test
    try {
      classUnderTest.handle(routedMessage, cb);
      Assert.fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
    verifyAll();
  }
}
