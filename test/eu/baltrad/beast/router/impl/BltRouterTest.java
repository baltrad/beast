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
package eu.baltrad.beast.router.impl;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.db.rest.RestfulFileEntry;
import eu.baltrad.bdb.oh5.Attribute;
import eu.baltrad.bdb.oh5.Group;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.oh5.Source;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.message.mo.BltCommandMessage;
import eu.baltrad.beast.message.mo.BltDataFrameMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.message.mo.BltGetAreasMessage;
import eu.baltrad.beast.message.mo.BltGetPcsDefinitionsMessage;
import eu.baltrad.beast.message.mo.BltGetQualityControlsMessage;
import eu.baltrad.beast.message.mo.BltMultiRoutedMessage;
import eu.baltrad.beast.message.mo.BltRoutedMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;
import eu.baltrad.beast.router.IMultiRoutedMessage;
import eu.baltrad.beast.router.IRoutedMessage;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.router.SystemRulesDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleManager;
import eu.baltrad.beast.rules.wrwp.WrwpRule;

/**
 * @author Anders Henja
 */
public class BltRouterTest extends EasyMockSupport {
  private static interface MockMethods {
    public List<IMultiRoutedMessage> getMultiRoutedMessages(IBltMessage msg, RouteDefinition def);
    public RouteDefinition getDefinition(String name);
    public String logRuleFailure(IBltMessage msg, RouteDefinition def, Exception e);
  };
  
  private IRule rule = null;
  private IRule rule2 = null;
  private IRule rule3 = null;
  
  private SystemRulesDefinition systemrules = null;
  
  private List<RouteDefinition> definitions = null;
  
  @Before
  public void setUp() throws Exception {
    rule = createMock(IRule.class);
    rule2 = createMock(IRule.class);
    rule3 = createMock(IRule.class);
    
    systemrules = createMock(SystemRulesDefinition.class);
    
    definitions = new ArrayList<RouteDefinition>();
    
    RouteDefinition def = new RouteDefinition();
    def.setName("R1");
    def.setRule(rule);
    definitions.add(def);
    
    def = new RouteDefinition();
    def.setName("R2");
    def.setRule(rule2);
    List<String> d2recipients = new ArrayList<String>();
    d2recipients.add("Adaptor1");
    def.setRecipients(d2recipients);
    definitions.add(def);
    
    def = new RouteDefinition();
    def.setName("R3");
    def.setRule(rule3);
    List<String> d3recipients = new ArrayList<String>();
    d3recipients.add("Adaptor2");
    d3recipients.add("Adaptor3");
    def.setRecipients(d3recipients);
    definitions.add(def);
  }
  
  @After
  public void tearDown() throws Exception {
    rule = null;
    rule2 = null;
    rule3 = null;
  }

  @Test
  public void testGetMultiRoutedMessages() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    
    IBltMessage message = new IBltMessage() {};
    BltMultiRoutedMessage m1 = new BltMultiRoutedMessage();
    BltMultiRoutedMessage m2 = new BltMultiRoutedMessage();
    BltMultiRoutedMessage m3 = new BltMultiRoutedMessage();
    List<IMultiRoutedMessage> l1 = new ArrayList<IMultiRoutedMessage>();
    l1.add(m1);
    l1.add(m2);
    List<IMultiRoutedMessage> l2 = new ArrayList<IMultiRoutedMessage>();
    l1.add(m3);

    systemrules.handle(message);
    
    expect(methods.getMultiRoutedMessages(message, definitions.get(0)))
      .andReturn(new ArrayList<IMultiRoutedMessage>());
    
    expect(methods.getMultiRoutedMessages(message, definitions.get(1))).andReturn(l1);
    expect(methods.getMultiRoutedMessages(message, definitions.get(2))).andReturn(l2);
    
    BltRouter classUnderTest = new BltRouter() {
      protected List<IMultiRoutedMessage> getMultiRoutedMessages(IBltMessage msg, RouteDefinition def) {
        return methods.getMultiRoutedMessages(msg, def);
      }
    };
    classUnderTest.setDefinitions(definitions);
    classUnderTest.setSystemRules(systemrules);
    
    replayAll();
    
    List<IMultiRoutedMessage> result = classUnderTest.getMultiRoutedMessages(message);
    
    verifyAll();
    
    assertEquals(3, result.size());
    assertSame(m1, result.get(0));
    assertSame(m2, result.get(1));
    assertSame(m3, result.get(2));
  }

  @Test
  public void testGetMultiRoutedMessage_triggerJob() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    BltTriggerJobMessage msg = new BltTriggerJobMessage();
    msg.setId("1");
    msg.setName("R2");

    RouteDefinition d = new RouteDefinition();
    List<IMultiRoutedMessage> l1 = new ArrayList<IMultiRoutedMessage>();
    BltMultiRoutedMessage m1 = new BltMultiRoutedMessage();
    BltMultiRoutedMessage m2 = new BltMultiRoutedMessage();
    l1.add(m1);
    l1.add(m2);
    
    expect(methods.getDefinition("R2")).andReturn(d);
    expect(methods.getMultiRoutedMessages(msg, d)).andReturn(l1);
    
    BltRouter classUnderTest = new BltRouter() {
      public RouteDefinition getDefinition(String name) {
        return methods.getDefinition(name);
      }
      protected List<IMultiRoutedMessage> getMultiRoutedMessages(IBltMessage msg, RouteDefinition def) {
        return methods.getMultiRoutedMessages(msg, def);
      }
    };
    classUnderTest.setDefinitions(definitions);
    
    replayAll();
    
    List<IMultiRoutedMessage> result = classUnderTest.getMultiRoutedMessages(msg);
    
    verifyAll();
    assertEquals(2, result.size());
    assertSame(m1, result.get(0));
    assertSame(m2, result.get(1));
  }

  @Test
  public void testGetMultiRoutedMessagesFromDefinition() throws Exception {
    RouteDefinition d = new RouteDefinition();
    List<String> r = new ArrayList<String>();
    r.add("A");
    r.add("B");
    d.setRule(rule);
    d.setRecipients(r);
    
    IBltMessage msg = new IBltMessage() {};
    IBltMessage msg2 = new IBltMessage() {};
    expect(rule.handle(msg)).andReturn(msg2);
    
    replayAll();
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    List<IMultiRoutedMessage> result = classUnderTest.getMultiRoutedMessages(msg, d);
    
    verifyAll();
    assertEquals(1, result.size());
    assertSame(msg2, result.get(0).getMessage());
    assertSame(r, result.get(0).getDestinations());
  }

  @Test
  public void testGetMultiRoutedMessagesFromDefinition_handleThrowsException() throws Exception {
    RouteDefinition d = new RouteDefinition();
    List<String> r = new ArrayList<String>();
    r.add("A");
    r.add("B");
    d.setRule(rule);
    d.setRecipients(r);
    
    IBltMessage msg = new IBltMessage() {};
    expect(rule.handle(msg)).andThrow(new RuntimeException());
    
    replayAll();
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    List<IMultiRoutedMessage> result = classUnderTest.getMultiRoutedMessages(msg, d);
    
    verifyAll();
    assertEquals(0, result.size());
  }
  
  @Test
  public void testGetMultiRoutedMessagesFromDefinition_notActive() throws Exception {
    RouteDefinition d = new RouteDefinition();
    d.setActive(false);
    d.setRule(rule);
    
    IBltMessage msg = new IBltMessage() {};
    replayAll();
    
    BltRouter classUnderTest = new BltRouter();
    List<IMultiRoutedMessage> result = classUnderTest.getMultiRoutedMessages(msg, d);
    
    verifyAll();
    assertEquals(0, result.size());
  }
  
  private static class MRM implements IBltMessage, IMultiRoutedMessage {
    private IBltMessage msg = null;
    private List<String> destinations = null;
    
    @Override
    public List<String> getDestinations() {
      return this.destinations;
    }
    @Override
    public IBltMessage getMessage() {
      return this.msg;
    }
    
    public void setDestinations(List<String> destinations) {
      this.destinations = destinations;
    }
    
    public void setMessage(IBltMessage msg) {
      this.msg = msg;
    }
  }
  
  private static class RM implements IBltMessage, IRoutedMessage {
    private IBltMessage msg = null;
    private String destination = null;
    
    @Override
    public String getDestination() {
      return this.destination;
    }
    @Override
    public IBltMessage getMessage() {
      return this.msg;
    }

    public void setDestination(String destination) {
      this.destination = destination;
    }
    
    public void setMessage(IBltMessage msg) {
      this.msg = msg;
    }
    
  }
  
  @Test
  public void testGetMultiRoutedMessages_alreadyMultiRouted() throws Exception {
    MRM message = new MRM();
    
    BltRouter classUnderTest = new BltRouter();
    
    replayAll();
    
    List<IMultiRoutedMessage> result = classUnderTest.getMultiRoutedMessages(message);
    
    verifyAll();
    
    assertEquals(1, result.size());
    assertSame(message, result.get(0));
  }

  @Test
  public void testGetMultiRoutedMessages_routed() throws Exception {
    IBltMessage msg = new IBltMessage() {};
    RM message = new RM();
    message.setDestination("ABC");
    message.setMessage(msg);
    
    BltRouter classUnderTest = new BltRouter();
    
    replayAll();
    
    List<IMultiRoutedMessage> result = classUnderTest.getMultiRoutedMessages(message);
    
    verifyAll();
    
    assertEquals(1, result.size());
    assertSame(msg, result.get(0).getMessage());
    assertEquals(1, result.get(0).getDestinations().size());
    assertEquals("ABC", result.get(0).getDestinations().get(0));
  }
  
  
  @Test
  public void testGetMultiRoutedMessages_noHits() throws Exception {
    IBltMessage message = new IBltMessage() {};
    
    expect(rule.handle(message)).andReturn(null);
    expect(rule2.handle(message)).andReturn(null);
    expect(rule3.handle(message)).andReturn(null);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replayAll();
    
    List<IMultiRoutedMessage> result = classUnderTest.getMultiRoutedMessages(message);
    
    verifyAll();
    
    assertEquals(0, result.size());
  }

  @Test
  public void testGetMultiRoutedMessages_handleReturnsMultiRoutedMessage() throws Exception {
    IBltMessage message = new IBltMessage() {};
    IBltMessage r2m = new IBltMessage() {};
    List<String> r2destinations = new ArrayList<String>();
    BltMultiRoutedMessage r2message = new BltMultiRoutedMessage();
    r2message.setMessage(r2m);
    r2message.setDestinations(r2destinations);
    
    expect(rule.handle(message)).andReturn(null);
    expect(rule2.handle(message)).andReturn(r2message);
    expect(rule3.handle(message)).andReturn(null);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replayAll();
    
    List<IMultiRoutedMessage> result = classUnderTest.getMultiRoutedMessages(message);
    
    verifyAll();
    
    assertEquals(1, result.size());
    assertSame(r2message, result.get(0));
    assertSame(r2m, result.get(0).getMessage());
    assertSame(r2destinations, result.get(0).getDestinations());
  }

  @Test
  public void testGetMultiRoutedMessages_handleReturnsRoutedMessage() throws Exception {
    IBltMessage message = new IBltMessage() {};
    IBltMessage r2m = new IBltMessage() {};
    BltRoutedMessage r2message = new BltRoutedMessage();
    r2message.setMessage(r2m);
    r2message.setDestination("ABC");
    
    expect(rule.handle(message)).andReturn(null);
    expect(rule2.handle(message)).andReturn(r2message);
    expect(rule3.handle(message)).andReturn(null);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replayAll();
    
    List<IMultiRoutedMessage> result = classUnderTest.getMultiRoutedMessages(message);
    
    verifyAll();
    
    assertEquals(1, result.size());
    assertSame(r2m, result.get(0).getMessage());
    assertEquals("ABC", result.get(0).getDestinations().get(0));
  }
  
  @Test
  public void testGetRoutedMessages() throws Exception {
    IBltMessage message = new IBltMessage() {};
    IBltMessage r2message = new IBltMessage() {};
    IBltMessage r3message = new IBltMessage() {};
    
    systemrules.handle(message);
    expect(rule.handle(message)).andReturn(null);
    expect(rule2.handle(message)).andReturn(r2message);
    expect(rule3.handle(message)).andReturn(r3message);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    classUnderTest.setSystemRules(systemrules);
    
    replayAll();
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(message);
    
    verifyAll();
    
    assertEquals(3, result.size());
    assertSame(r2message, result.get(0).getMessage());
    assertEquals("Adaptor1", result.get(0).getDestination());
    assertSame(r3message, result.get(1).getMessage());
    assertEquals("Adaptor2", result.get(1).getDestination());
    assertSame(r3message, result.get(2).getMessage());
    assertEquals("Adaptor3", result.get(2).getDestination());
  }

  @Test
  public void testGetRoutedMessages_noSystemdef() throws Exception {
    IBltMessage message = new IBltMessage() {};

    expect(rule.handle(message)).andReturn(null);
    expect(rule2.handle(message)).andReturn(null);
    expect(rule3.handle(message)).andReturn(null);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replayAll();
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(message);
    
    verifyAll();
    
    assertEquals(0, result.size());
  }

  @Test
  public void testGetRoutedMessages_alreadyRouted() throws Exception {
    RM message = new RM();
    
    BltRouter classUnderTest = new BltRouter();
    
    replayAll();
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(message);
    
    verifyAll();
    
    assertEquals(1, result.size());
    assertSame(message, result.get(0));
  }

  @Test
  public void testGetRoutedMessages_multiRouted() throws Exception {
    IBltMessage m = new IBltMessage() {};
    MRM message = new MRM();
    message.setMessage(m);
    List<String> destinations = new ArrayList<String>();
    destinations.add("ABC");
    destinations.add("DEF");
    message.setDestinations(destinations);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setSystemRules(systemrules);
    
    systemrules.handle(message);
    
    replayAll();
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(message);
    
    verifyAll();
    
    assertEquals(2, result.size());
    assertSame(m, result.get(0).getMessage());
    assertSame(m, result.get(1).getMessage());
    assertEquals("ABC", result.get(0).getDestination());
    assertEquals("DEF", result.get(1).getDestination());
  }
  
  @Test
  public void testGetRoutedMessages_multiRouted_noSystemdef() throws Exception {
    IBltMessage m = new IBltMessage() {};
    MRM message = new MRM();
    message.setMessage(m);
    List<String> destinations = new ArrayList<String>();
    destinations.add("ABC");
    destinations.add("DEF");
    message.setDestinations(destinations);
    
    BltRouter classUnderTest = new BltRouter();
    
    replayAll();
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(message);
    
    verifyAll();
    
    assertEquals(2, result.size());
    assertSame(m, result.get(0).getMessage());
    assertSame(m, result.get(1).getMessage());
    assertEquals("ABC", result.get(0).getDestination());
    assertEquals("DEF", result.get(1).getDestination());
  }
  
  @Test
  public void testGetRoutedMessages_noHits() throws Exception {
    IBltMessage message = new IBltMessage() {};
    
    expect(rule.handle(message)).andReturn(null);
    expect(rule2.handle(message)).andReturn(null);
    expect(rule3.handle(message)).andReturn(null);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replayAll();
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(message);
    
    verifyAll();
    
    assertEquals(0, result.size());
  }

  @Test
  public void testGetRoutedMessages_handleReturnsRoutedMessage() throws Exception {
    IBltMessage message = new IBltMessage() {};
    IBltMessage r2message = new IBltMessage() {};
    IBltMessage r3message = new IBltMessage() {};
    BltRoutedMessage routedr2message = new BltRoutedMessage();
    routedr2message.setDestination("ABC");
    routedr2message.setMessage(r2message);
    
    expect(rule.handle(message)).andReturn(null);
    expect(rule2.handle(message)).andReturn(routedr2message);
    expect(rule3.handle(message)).andReturn(r3message);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replayAll();
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(message);
    
    verifyAll();
    
    assertEquals(3, result.size());
    assertSame(routedr2message, result.get(0));
    assertEquals("ABC", result.get(0).getDestination());
    assertSame(r2message, result.get(0).getMessage());
    
    assertSame(r3message, result.get(1).getMessage());
    assertEquals("Adaptor2", result.get(1).getDestination());
    assertSame(r3message, result.get(2).getMessage());
    assertEquals("Adaptor3", result.get(2).getDestination());
  }

  @Test
  public void testGetRoutedMessages_handleReturnsMultiRoutedMessage() throws Exception {
    IBltMessage message = new IBltMessage() {};
    IBltMessage r2message = new IBltMessage() {};
    IBltMessage r3message = new IBltMessage() {};
    BltMultiRoutedMessage routedr2message = new BltMultiRoutedMessage();
    List<String> mmdest = new ArrayList<String>();
    mmdest.add("QWE");
    mmdest.add("RTY");
    routedr2message.setDestinations(mmdest);
    routedr2message.setMessage(r2message);
    
    expect(rule.handle(message)).andReturn(null);
    expect(rule2.handle(message)).andReturn(routedr2message);
    expect(rule3.handle(message)).andReturn(r3message);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replayAll();
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(message);
    
    verifyAll();
    
    assertEquals(4, result.size());
    assertEquals("QWE", result.get(0).getDestination());
    assertSame(r2message, result.get(0).getMessage());
    assertEquals("RTY", result.get(1).getDestination());
    assertSame(r2message, result.get(1).getMessage());
    assertSame(r3message, result.get(2).getMessage());
    assertEquals("Adaptor2", result.get(2).getDestination());
    assertSame(r3message, result.get(3).getMessage());
    assertEquals("Adaptor3", result.get(3).getDestination());
  }

  @Test
  public void testGetRoutedMessages_handleReturnsMultiRoutedMessage_withNullDest() throws Exception {
    IBltMessage message = new IBltMessage() {};
    IBltMessage r2message = new IBltMessage() {};
    IBltMessage r3message = new IBltMessage() {};
    BltMultiRoutedMessage routedr2message = new BltMultiRoutedMessage();
    routedr2message.setMessage(r2message);
    
    expect(rule.handle(message)).andReturn(null);
    expect(rule2.handle(message)).andReturn(routedr2message);
    expect(rule3.handle(message)).andReturn(r3message);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replayAll();
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(message);
    
    verifyAll();
    
    assertEquals(2, result.size());
    assertSame(r3message, result.get(0).getMessage());
    assertEquals("Adaptor2", result.get(0).getDestination());
    assertSame(r3message, result.get(1).getMessage());
    assertEquals("Adaptor3", result.get(1).getDestination());
  }
  
  @Test
  public void testGetRoutedMessages_withDefinition() throws Exception {
    RouteDefinition d = new RouteDefinition();
    List<String> r = new ArrayList<String>();
    r.add("A");
    r.add("B");
    d.setRule(rule);
    d.setRecipients(r);
    
    IBltMessage msg = new IBltMessage() {};
    IBltMessage msg2 = new IBltMessage() {};
    expect(rule.handle(msg)).andReturn(msg2);
    
    replayAll();
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(msg, d);
    
    verifyAll();
    assertEquals(2, result.size());
    assertSame(msg2, result.get(0).getMessage());
    assertEquals("A", result.get(0).getDestination());
    assertSame(msg2, result.get(1).getMessage());
    assertEquals("B", result.get(1).getDestination());
  }

  @Test
  public void testGetRoutedMessages_withDefinition_throwsException() throws Exception {
    RouteDefinition d = new RouteDefinition();
    List<String> r = new ArrayList<String>();
    r.add("A");
    r.add("B");
    d.setRule(rule);
    d.setRecipients(r);
    
    IBltMessage msg = new IBltMessage() {};
    expect(rule.handle(msg)).andThrow(new RuntimeException());
    
    replayAll();
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(msg, d);
    
    verifyAll();
    assertEquals(0, result.size());
  }

  @Test
  public void testGetRoutedMessages_withDefinition_notActive() throws Exception {
    RouteDefinition d = new RouteDefinition();
    d.setActive(false);
    d.setRule(rule);
    
    IBltMessage msg = new IBltMessage() {};
    replayAll();
    
    BltRouter classUnderTest = new BltRouter();
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(msg, d);
    
    verifyAll();
    assertEquals(0, result.size());
  }

  @Test
  public void testGetNames() throws Exception {
    List<RouteDefinition> defs = new ArrayList<RouteDefinition>();
    RouteDefinition d1 = new RouteDefinition();
    d1.setName("ABC");
    RouteDefinition d2 = new RouteDefinition();
    d2.setName("DEF");
    RouteDefinition d3 = new RouteDefinition();
    d3.setName("GHI");
    
    defs.add(d1);
    defs.add(d2);
    defs.add(d3);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(defs);

    List<String> result = classUnderTest.getNames();
    assertEquals(3, result.size());
    assertEquals("ABC", result.get(0));
    assertEquals("DEF", result.get(1));
    assertEquals("GHI", result.get(2));
  }

  @Test
  public void testGetNames_nothingRegistered() throws Exception {
    List<RouteDefinition> defs = new ArrayList<RouteDefinition>();
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(defs);

    List<String> result = classUnderTest.getNames();
    assertEquals(0, result.size());
  }
  
  @Test
  public void testGetDefinitions() throws Exception {
    List<RouteDefinition> defs = new ArrayList<RouteDefinition>();
    defs.add(new RouteDefinition());
    defs.get(0).setAuthor("X");
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(defs);
    
    assertEquals(1, classUnderTest.getDefinitions().size());
    assertEquals("X", classUnderTest.getDefinitions().get(0).getAuthor());
  }
  
  @Test
  public void testGetDefinition() throws Exception {
    List<RouteDefinition> defs = new ArrayList<RouteDefinition>();
    RouteDefinition d1 = new RouteDefinition();
    d1.setName("D1");
    RouteDefinition d2 = new RouteDefinition();
    d2.setName("D2");
    defs.add(d1);
    defs.add(d2);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(defs);
    
    RouteDefinition result = classUnderTest.getDefinition("D2");
    assertSame(d2, result);
  }
  
  @Test
  public void testCreate() throws Exception {
    String name = "MyName";
    String author = "nisse";
    boolean active = true;
    String description = "some description";
    List<String> recipients = new ArrayList<String>();
    IRule rule = new IRule() {
      public IBltMessage handle(IBltMessage message) {return null;}
      public String getType() {return null;}
      public boolean isValid() {return true;}
    };
    BltRouter classUnderTest = new BltRouter();
    RouteDefinition result = classUnderTest.create(name, author, active, description, recipients, rule);
    assertEquals(name, result.getName());
    assertEquals(author, result.getAuthor());
    assertEquals(active, result.isActive());
    assertEquals(description, result.getDescription());
    assertSame(recipients, result.getRecipients());
    assertSame(rule, result.getRule());
  }
  
  @Test
  public void testCreateRule() {
    IRuleManager r1 = createMock(IRuleManager.class);
    Map<String, IRuleManager> managers = new HashMap<String, IRuleManager>();
    managers.put("R1", r1);
    IRule arule = new IRule() {
      public IBltMessage handle(IBltMessage message) {return null;}
      public String getType() {return null;}
      public boolean isValid() {return true;}
    };

    expect(r1.createRule()).andReturn(arule);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setRuleManagers(managers);
    
    replayAll();
    
    IRule result = classUnderTest.createRule("R1");
    
    verifyAll();
    assertSame(arule, result);
  }

  @Test
  public void testGetMultiRoutedMessages_logsRuleFailure() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    RouteDefinition def = definitions.get(0);
    IBltMessage msg = new BltDataMessage();
    NullPointerException failure = new NullPointerException();
    expect(rule.handle(msg)).andThrow(failure);
    expect(methods.logRuleFailure(msg, def, failure)).andReturn("Rule caused exception");
    BltRouter classUnderTest = new BltRouter() {
      @Override
      String logRuleFailure(IBltMessage msg, RouteDefinition def, Exception e) {
        return methods.logRuleFailure(msg, def, e);
      }
    };

    replayAll();
    classUnderTest.getMultiRoutedMessages(msg, def);

    verifyAll();
  }

  @Test
  public void testGetRoutedMessages_logsRuleFailure() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    RouteDefinition def = definitions.get(0);
    IBltMessage msg = new BltDataMessage();
    NullPointerException failure = new NullPointerException();
    expect(rule.handle(msg)).andThrow(failure);
    expect(methods.logRuleFailure(msg, def, failure)).andReturn("Rule caused exception");
    BltRouter classUnderTest = new BltRouter() {
      @Override
      String logRuleFailure(IBltMessage msg, RouteDefinition def, Exception e) {
        return methods.logRuleFailure(msg, def, e);
      }
    };

    replayAll();
    classUnderTest.getRoutedMessages(msg, def);

    verifyAll();
  }

  private String loggedMessage(IBltMessage msg, RouteDefinition def) {
    Logger logger = Logger.getLogger(BltRouter.class);
    Level previous = logger.getLevel();
    try {
      logger.setLevel(Level.INFO);
      return new BltRouter().logRuleFailure(msg, def, null);
    } finally {
      logger.setLevel(previous);
    }
  }

  private void assertFields(String message, String... fields) {
    assertTrue(message, message.startsWith("Rule caused exception "));
    for (String field : fields) {
      assertTrue("Missing " + field + " in " + message, (" " + message + " ").contains(" " + field + " "));
    }
  }

  @Test
  public void testLogRuleFailure_jobMetadata() {
    RouteDefinition def = definitions.get(0);
    def.setRule(new WrwpRule());

    BltTriggerJobMessage job = new BltTriggerJobMessage();
    job.setName("scheduled");
    job.setId("17");
    assertFields(loggedMessage(job, def), "message=BltTriggerJobMessage", "job=scheduled", "jobId=17");
  }

  @Test
  public void testLogRuleFailure_frameMetadata() {
    RouteDefinition def = definitions.get(0);
    def.setRule(new WrwpRule());
    BltDataFrameMessage frame = new BltDataFrameMessage();
    frame.setFilename("/incoming/scan.h5");
    assertFields(loggedMessage(frame, def), "message=BltDataFrameMessage", "file=scan.h5");
  }

  @Test
  public void testLogRuleFailure_generateMetadata() {
    RouteDefinition def = definitions.get(0);
    def.setRule(new WrwpRule());
    BltGenerateMessage generate = new BltGenerateMessage();
    generate.setAlgorithm("generatecomposite");
    assertFields(loggedMessage(generate, def), "message=BltGenerateMessage", "algorithm=generatecomposite");
  }

  @Test
  public void testLogRuleFailure_commandMetadata() {
    RouteDefinition def = definitions.get(0);
    def.setRule(new WrwpRule());
    BltCommandMessage command = new BltCommandMessage();
    command.setCommand("refresh");
    assertFields(loggedMessage(command, def), "message=BltCommandMessage", "command=refresh");
  }

  @Test
  public void testLogRuleFailure_alertMetadata() {
    RouteDefinition def = definitions.get(0);
    def.setRule(new WrwpRule());
    BltAlertMessage alert = new BltAlertMessage();
    alert.setModule("receiver");
    alert.setCode("00201");
    alert.setSeverity("WARNING");
    assertFields(loggedMessage(alert, def), "message=BltAlertMessage", "module=receiver", "code=00201", "severity=WARNING");
  }

  @Test
  public void testLogRuleFailure_dataMessage() {
    RouteDefinition def = definitions.get(0);
    def.setRule(new WrwpRule());

    UUID uuid = UUID.fromString("b39a906a-13fc-4151-b6b6-04c1c429e1bd");
    FileEntry entry = createMock(FileEntry.class);
    expect(entry.getSource()).andStubReturn(new Source("seang"));
    expect(entry.getContentSize()).andStubThrow(new IllegalStateException("size unavailable"));
    expect(entry.getUuid()).andReturn(null);
    expect(entry.getUuid()).andReturn(uuid);
    expect(entry.getUuid()).andThrow(new IllegalArgumentException("invalid UUID"));
    replayAll();

    BltDataMessage msg = new BltDataMessage();
    String withoutEntry = loggedMessage(msg, def);
    assertFields(withoutEntry, "message=BltDataMessage");
    assertFalse(withoutEntry, withoutEntry.contains(" fileUuid="));
    msg.setFileEntry(entry);
    assertFalse(loggedMessage(msg, def).contains(" fileUuid="));
    assertFields(loggedMessage(msg, def), "message=BltDataMessage", "fileUuid=" + uuid,
        "source=seang", "fileBytes=<unavailable>");
    assertFields(loggedMessage(msg, def), "message=BltDataMessage", "fileUuid=<unavailable>", "source=seang");
    verifyAll();
  }

  @Test
  public void testLogRuleFailure_unavailableRuleKeepsMessageDetails() {
    RouteDefinition def = new RouteDefinition() {
      @Override
      public IRule getRule() {
        throw new IllegalStateException("Rule unavailable");
      }
    };
    def.setName("R1");
    BltCommandMessage msg = new BltCommandMessage();
    msg.setCommand("refresh");

    assertFields(loggedMessage(msg, def), "route='R1'", "rule=<unavailable>",
        "message=BltCommandMessage", "command=refresh");
  }

  @Test
  public void testLogRuleFailure_routeDescription() {
    RouteDefinition def = definitions.get(0);
    def.setRule(new WrwpRule());
    def.setDescription("Send scans\nto processor");
    String message = loggedMessage(new BltDataMessage(), def);
    assertFields(message, "route='R1'", "rule=WrwpRule", "description='Send scans to processor'");
    def.setDescription(" ");
    assertFalse(loggedMessage(null, def).contains(" description="));
  }

  @Test
  public void testLogRuleFailure_unreadableDescriptionKeepsJobId() {
    RouteDefinition def = new RouteDefinition() {
      @Override
      public String getDescription() {
        throw new IllegalStateException("description unavailable");
      }
    };
    BltTriggerJobMessage job = new BltTriggerJobMessage();
    job.setId("17");
    assertFields(loggedMessage(job, def), "description=<unavailable>", "jobId=17");
  }

  @Test
  public void testLogRuleFailure_infoDisabledSkipsFileLookup() {
    FileEntry entry = createMock(FileEntry.class);
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(entry);
    replayAll();

    Logger logger = Logger.getLogger(BltRouter.class);
    Level previous = logger.getLevel();
    try {
      logger.setLevel(Level.WARN);
      assertNull(new BltRouter().logRuleFailure(msg, definitions.get(0), new NullPointerException()));
    } finally {
      logger.setLevel(previous);
    }
    verifyAll();
  }

  @Test
  public void testLogRuleFailure_messagesWithoutMetadata() {
    RouteDefinition def = definitions.get(0);
    def.setRule(null);

    assertFields(loggedMessage(null, def), "rule=null", "message=null");
    assertFields(loggedMessage(new BltGetAreasMessage(), def), "rule=null", "message=BltGetAreasMessage");
    assertFields(loggedMessage(new BltGetPcsDefinitionsMessage(), def), "rule=null", "message=BltGetPcsDefinitionsMessage");
    assertFields(loggedMessage(new BltGetQualityControlsMessage(), def), "rule=null", "message=BltGetQualityControlsMessage");
  }
  
  @Test
  public void testLogRuleFailure_malformedBdbUuidKeepsRadarSource() {
    Metadata metadata = new Metadata();
    metadata.addNode("/", new Group("_bdb"));
    metadata.addNode("/_bdb", new Attribute("uuid", "invalid-uuid"));
    metadata.addNode("/_bdb", new Attribute("source", "NOD:seang"));
    metadata.addNode("/_bdb", new Attribute("source_name", "seang"));
    metadata.addNode("/_bdb", new Attribute("file_size", 4096L));
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(new RestfulFileEntry(null, metadata));

    String message = loggedMessage(msg, new RouteDefinition());
    assertFields(message, "message=BltDataMessage", "fileUuid=<unavailable>", "source=seang", "fileBytes=4096");
    assertFalse(message, message.contains(" route="));
  }

  @Test
  public void testLogRuleFailure_unreadableFileEntryKeepsRoute() {
    BltDataMessage msg = new BltDataMessage() {
      @Override
      public FileEntry getFileEntry() {
        throw new IllegalStateException("entry unavailable");
      }
    };
    RouteDefinition def = definitions.get(0);
    def.setRule(new WrwpRule());
    assertFields(loggedMessage(msg, def), "route='R1'", "fileEntry=<unavailable>");
  }

  @Test
  public void testLogRuleFailure_unreadableAlertCodeKeepsSeverity() {
    BltAlertMessage alert = new BltAlertMessage() {
      @Override
      public String getCode() {
        throw new IllegalArgumentException("bad code");
      }
    };
    alert.setSeverity("WARNING");
    assertFields(loggedMessage(alert, null), "code=<unavailable>", "severity=WARNING");
  }

  @Test
  public void testGetMultiRoutedMessages_twoMissingRulesStillDeliverThirdResult() {
    definitions.get(0).setRule(null);
    definitions.get(1).setRule(null);
    BltDataMessage input = new BltDataMessage();
    BltGenerateMessage output = new BltGenerateMessage();
    expect(rule3.handle(input)).andReturn(output);
    replayAll();

    final List<String> failures = new ArrayList<String>();
    BltRouter router = new BltRouter() {
      @Override
      String logRuleFailure(IBltMessage msg, RouteDefinition def, Exception e) {
        assertTrue(e instanceof NullPointerException);
        String message = super.logRuleFailure(msg, def, e);
        failures.add(message);
        return message;
      }
    };
    router.setDefinitions(definitions);
    Logger logger = Logger.getLogger(BltRouter.class);
    Level previous = logger.getLevel();
    List<IMultiRoutedMessage> result;
    try {
      logger.setLevel(Level.INFO);
      result = router.getMultiRoutedMessages(input);
    } finally {
      logger.setLevel(previous);
    }

    verifyAll();
    assertEquals(2, failures.size());
    assertFields(failures.get(0), "route='R1'", "rule=null", "message=BltDataMessage");
    assertFields(failures.get(1), "route='R2'", "rule=null", "message=BltDataMessage");
    assertEquals(1, result.size());
    assertSame(output, result.get(0).getMessage());
    assertEquals(definitions.get(2).getRecipients(), result.get(0).getDestinations());
  }

  @Test
  public void testLogRuleFailure_missingBdbSourceKeepsUuidAndZeroSize() {
    String uuid = "b39a906a-13fc-4151-b6b6-04c1c429e1bd";
    Metadata metadata = new Metadata();
    metadata.addNode("/", new Group("_bdb"));
    metadata.addNode("/_bdb", new Attribute("uuid", uuid));
    metadata.addNode("/_bdb", new Attribute("file_size", 0L));
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(new RestfulFileEntry(null, metadata));

    assertFields(loggedMessage(msg, definitions.get(0)), "fileUuid=" + uuid, "source=<unavailable>", "fileBytes=0");
  }

  @Test
  public void testLogRuleFailure_multilineRouteAndCommandStayOnOneLine() {
    RouteDefinition def = definitions.get(0);
    def.setName("incoming\rscans");
    BltCommandMessage msg = new BltCommandMessage();
    msg.setCommand("refresh\r\nnow");

    String message = loggedMessage(msg, def);
    assertFields(message, "route='incoming scans'", "command=refresh  now");
    assertFalse(message, message.contains("\r"));
    assertFalse(message, message.contains("\n"));
  }

}

