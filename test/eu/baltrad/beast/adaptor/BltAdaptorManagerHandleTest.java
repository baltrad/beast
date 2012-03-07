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

import org.easymock.MockControl;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.router.IMultiRoutedMessage;
import eu.baltrad.beast.router.IRoutedMessage;

import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class BltAdaptorManagerHandleTest extends TestCase {
  private MockControl adaptor1Control = MockControl.createControl(IAdaptor.class);
  private IAdaptor adaptor1 = (IAdaptor)adaptor1Control.getMock();
  private MockControl adaptor2Control = MockControl.createControl(IAdaptor.class);
  private IAdaptor adaptor2 = (IAdaptor)adaptor2Control.getMock();
  private BltAdaptorManager classUnderTest = null;

  protected void setUp() throws Exception {
    adaptor1Control = MockControl.createControl(IAdaptor.class);
    adaptor1 = (IAdaptor)adaptor1Control.getMock();
    adaptor2Control = MockControl.createControl(IAdaptor.class);
    adaptor2 = (IAdaptor)adaptor2Control.getMock();
    Map<String, IAdaptor> adaptors = new HashMap<String, IAdaptor>();
    adaptors.put("A1", adaptor1);
    adaptors.put("A2", adaptor2);
    classUnderTest = new BltAdaptorManager();
    classUnderTest.setAdaptors(adaptors);
  }
  
  protected void tearDown() throws Exception {
    adaptor1Control = null;
    adaptor1 = null;
    adaptor2Control = null;
    adaptor2 = null;
    classUnderTest = null;
  }
  
  protected void replay() {
    adaptor1Control.replay();
    adaptor2Control.replay();
  }
  
  protected void verify() {
    adaptor1Control.verify();
    adaptor2Control.verify();
  }
  
  public void testHandle_MultiRouted() throws Exception {
    MockControl messageControl = MockControl.createControl(IMultiRoutedMessage.class);
    IMultiRoutedMessage message = (IMultiRoutedMessage)messageControl.getMock();
    List<String> destinations = new ArrayList<String>();
    destinations.add("A2");
    IBltMessage msg = new IBltMessage() {};
    
    // Mock setup
    message.getDestinations();
    messageControl.setReturnValue(destinations);
    message.getMessage();
    messageControl.setReturnValue(msg);

    adaptor2.handle(msg);

    replay();
    messageControl.replay();
    
    classUnderTest.handle(message);
    
    verify();
    messageControl.verify();
  }
  
  public void testHandle_MultiRouted_nullMessage() {
    MockControl messageControl = MockControl.createControl(IMultiRoutedMessage.class);
    IMultiRoutedMessage message = (IMultiRoutedMessage)messageControl.getMock();
    List<String> destinations = new ArrayList<String>();
    destinations.add("A2");
    
    // Mock setup
    message.getMessage();
    messageControl.setReturnValue(null);

    replay();
    messageControl.replay();
    
    classUnderTest.handle(message);
    
    verify();
    messageControl.verify();

  }
  
  public void testHandle_MultiRouted_noAdaptor() throws Exception {
    MockControl messageControl = MockControl.createControl(IMultiRoutedMessage.class);
    IMultiRoutedMessage message = (IMultiRoutedMessage)messageControl.getMock();
    List<String> destinations = new ArrayList<String>();
    destinations.add("A3");
    IBltMessage msg = new IBltMessage() {};
    
    // Mock setup
    message.getDestinations();
    messageControl.setReturnValue(destinations);
    message.getMessage();
    messageControl.setReturnValue(msg);

    replay();
    messageControl.replay();
    
    classUnderTest.handle(message);
    
    verify();
    messageControl.verify();
  }

  public void testHandle_MultiRouted_adaptorThrowsException() throws Exception {
    MockControl messageControl = MockControl.createControl(IMultiRoutedMessage.class);
    IMultiRoutedMessage message = (IMultiRoutedMessage)messageControl.getMock();
    List<String> destinations = new ArrayList<String>();
    destinations.add("A1");
    destinations.add("A2");
    IBltMessage msg = new IBltMessage() {};
    
    // Mock setup
    message.getDestinations();
    messageControl.setReturnValue(destinations);
    message.getMessage();
    messageControl.setReturnValue(msg);
    adaptor1.handle(msg);
    adaptor2.handle(msg);
    adaptor2Control.setThrowable(new AdaptorException());

    replay();
    messageControl.replay();
    
    classUnderTest.handle(message);
    
    verify();
    messageControl.verify();
  }

  public void testHandle() throws Exception {
    MockControl routedMessageControl = MockControl.createControl(IRoutedMessage.class);
    IRoutedMessage routedMessage = (IRoutedMessage)routedMessageControl.getMock();
    
    IBltMessage msg = new IBltMessage() {};
    
    routedMessage.getDestination();
    routedMessageControl.setReturnValue("A2");
    routedMessage.getMessage();
    routedMessageControl.setReturnValue(msg);
    adaptor2.handle(msg);

    replay();
    routedMessageControl.replay();
    
    // execute test
    classUnderTest.handle(routedMessage);
    
    // verify
    verify();
    routedMessageControl.verify();
  }
  
  public void testHandle_nullMessage() throws Exception {
    MockControl routedMessageControl = MockControl.createControl(IRoutedMessage.class);
    IRoutedMessage routedMessage = (IRoutedMessage)routedMessageControl.getMock();
    routedMessage.getDestination();
    routedMessageControl.setReturnValue("A2");
    
    routedMessage.getMessage();
    routedMessageControl.setReturnValue(null);

    replay();
    routedMessageControl.replay();
    
    // execute test
    classUnderTest.handle(routedMessage);
    
    // verify
    verify();
    routedMessageControl.verify();
  }
  
  public void testHandle_noMatchingAdaptor() throws Exception {
    MockControl routedMessageControl = MockControl.createControl(IRoutedMessage.class);
    IRoutedMessage routedMessage = (IRoutedMessage)routedMessageControl.getMock();
    routedMessage.getDestination();
    routedMessageControl.setReturnValue("A3");

    replay();
    routedMessageControl.replay();
    
    // execute test
    try {
      classUnderTest.handle(routedMessage);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
    verify();
    routedMessageControl.verify();
  }
  
  
  public void testHandle_withCallback() throws Exception {
    MockControl routedMessageControl = MockControl.createControl(IRoutedMessage.class);
    IRoutedMessage routedMessage = (IRoutedMessage)routedMessageControl.getMock();
    
    IAdaptorCallback cb = new IAdaptorCallback(){
      public void error(IBltMessage message, Throwable t) {}
      public void success(IBltMessage message, Object result) {}
      public void timeout(IBltMessage message) {}
    };

    IBltMessage msg = new IBltMessage() {};
    
    routedMessage.getDestination();
    routedMessageControl.setReturnValue("A2");
    routedMessage.getMessage();
    routedMessageControl.setReturnValue(msg);
    adaptor2.handle(msg, cb);

    replay();
    routedMessageControl.replay();
    
    // execute test
    classUnderTest.handle(routedMessage, cb);
    
    // verify
    verify();
    routedMessageControl.verify();
  }

  public void testHandle_withCallback_nullMessage() throws Exception {
    MockControl routedMessageControl = MockControl.createControl(IRoutedMessage.class);
    IRoutedMessage routedMessage = (IRoutedMessage)routedMessageControl.getMock();
    
    IAdaptorCallback cb = new IAdaptorCallback(){
      public void error(IBltMessage message, Throwable t) {}
      public void success(IBltMessage message, Object result) {}
      public void timeout(IBltMessage message) {}
    };

    routedMessage.getDestination();
    routedMessageControl.setReturnValue("A2");
    routedMessage.getMessage();
    routedMessageControl.setReturnValue(null);
   
    replay();
    routedMessageControl.replay();
    
    // execute test
    classUnderTest.handle(routedMessage, cb);
    
    // verify
    verify();
    routedMessageControl.verify();
  }
  
  public void testHandle_withCallback_noMatchingAdaptor() throws Exception {
    MockControl routedMessageControl = MockControl.createControl(IRoutedMessage.class);
    IRoutedMessage routedMessage = (IRoutedMessage)routedMessageControl.getMock();
    
    IAdaptorCallback cb = new IAdaptorCallback(){
      public void error(IBltMessage message, Throwable t) {}
      public void success(IBltMessage message, Object result) {}
      public void timeout(IBltMessage message) {}
    };

    routedMessage.getDestination();
    routedMessageControl.setReturnValue("A3");

    replay();
    routedMessageControl.replay();
    
    // execute test
    try {
      classUnderTest.handle(routedMessage, cb);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
    verify();
    routedMessageControl.verify();
  }
}
