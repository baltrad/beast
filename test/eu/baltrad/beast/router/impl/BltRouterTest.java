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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltMultiRoutedMessage;
import eu.baltrad.beast.message.mo.BltRoutedMessage;
import eu.baltrad.beast.router.IMultiRoutedMessage;
import eu.baltrad.beast.router.IRoutedMessage;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.RuleException;


/**
 * @author Anders Henja
 */
public class BltRouterTest extends TestCase {
  private MockControl ruleControl = null;
  private IRule rule = null;
  private MockControl rule2Control = null;
  private IRule rule2 = null;
  private MockControl rule3Control = null;
  private IRule rule3 = null;
  
  private List<RouteDefinition> definitions = null;
  
  protected void setUp() throws Exception {
    ruleControl = MockControl.createControl(IRule.class);
    rule = (IRule)ruleControl.getMock();
    rule2Control = MockControl.createControl(IRule.class);
    rule2 = (IRule)rule2Control.getMock();
    rule3Control = MockControl.createControl(IRule.class);
    rule3 = (IRule)rule3Control.getMock();
    
    definitions = new ArrayList<RouteDefinition>();
    
    RouteDefinition def = new RouteDefinition();
    def.setRule(rule);
    definitions.add(def);
    
    def = new RouteDefinition();
    def.setRule(rule2);
    List<String> d2recipients = new ArrayList<String>();
    d2recipients.add("Adaptor1");
    def.setRecipients(d2recipients);
    definitions.add(def);
    
    def = new RouteDefinition();
    def.setRule(rule3);
    List<String> d3recipients = new ArrayList<String>();
    d3recipients.add("Adaptor2");
    d3recipients.add("Adaptor3");
    def.setRecipients(d3recipients);
    definitions.add(def);
  }
  
  protected void tearDown() throws Exception {
    ruleControl = null;
    rule = null;
    rule2Control = null;
    rule2 = null;
    rule3Control = null;
    rule3 = null;
  }
  
  protected void replay() {
    ruleControl.replay();
    rule2Control.replay();
    rule3Control.replay();
  }

  protected void verify() {
    ruleControl.verify();
    rule2Control.verify();
    rule3Control.verify();
  }

  public void testGetMultiRoutedMessages() throws Exception {
    IBltMessage message = new IBltMessage() {};
    IBltMessage r2message = new IBltMessage() {};
    IBltMessage r3message = new IBltMessage() {};
    
    rule.handle(message);
    ruleControl.setReturnValue(null);
    rule2.handle(message);
    rule2Control.setReturnValue(r2message);
    rule3.handle(message);
    rule3Control.setReturnValue(r3message);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replay();
    
    List<IMultiRoutedMessage> result = classUnderTest.getMultiRoutedMessages(message);
    
    verify();
    
    assertEquals(2, result.size());
    assertSame(r2message, result.get(0).getMessage());
    assertEquals(1, result.get(0).getDestinations().size());
    assertEquals("Adaptor1", result.get(0).getDestinations().get(0));
    assertSame(r3message, result.get(1).getMessage());
    assertEquals(2, result.get(1).getDestinations().size());
    assertEquals("Adaptor2", result.get(1).getDestinations().get(0));
    assertEquals("Adaptor3", result.get(1).getDestinations().get(1));
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
  
  public void testGetMultiRoutedMessages_alreadyMultiRouted() throws Exception {
    MRM message = new MRM();
    
    BltRouter classUnderTest = new BltRouter();
    
    replay();
    
    List<IMultiRoutedMessage> result = classUnderTest.getMultiRoutedMessages(message);
    
    verify();
    
    assertEquals(1, result.size());
    assertSame(message, result.get(0));
  }

  public void testGetMultiRoutedMessages_routed() throws Exception {
    IBltMessage msg = new IBltMessage() {};
    RM message = new RM();
    message.setDestination("ABC");
    message.setMessage(msg);
    
    BltRouter classUnderTest = new BltRouter();
    
    replay();
    
    List<IMultiRoutedMessage> result = classUnderTest.getMultiRoutedMessages(message);
    
    verify();
    
    assertEquals(1, result.size());
    assertSame(msg, result.get(0).getMessage());
    assertEquals(1, result.get(0).getDestinations().size());
    assertEquals("ABC", result.get(0).getDestinations().get(0));
  }
  
  
  public void testGetMultiRoutedMessages_noHits() throws Exception {
    IBltMessage message = new IBltMessage() {};
    
    rule.handle(message);
    ruleControl.setReturnValue(null);
    rule2.handle(message);
    rule2Control.setReturnValue(null);
    rule3.handle(message);
    rule3Control.setReturnValue(null);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replay();
    
    List<IMultiRoutedMessage> result = classUnderTest.getMultiRoutedMessages(message);
    
    verify();
    
    assertEquals(0, result.size());
  }

  public void testGetMultiRoutedMessages_handleReturnsMultiRoutedMessage() throws Exception {
    IBltMessage message = new IBltMessage() {};
    IBltMessage r2m = new IBltMessage() {};
    List<String> r2destinations = new ArrayList<String>();
    BltMultiRoutedMessage r2message = new BltMultiRoutedMessage();
    r2message.setMessage(r2m);
    r2message.setDestinations(r2destinations);
    
    rule.handle(message);
    ruleControl.setReturnValue(null);
    rule2.handle(message);
    rule2Control.setReturnValue(r2message);
    rule3.handle(message);
    rule3Control.setReturnValue(null);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replay();
    
    List<IMultiRoutedMessage> result = classUnderTest.getMultiRoutedMessages(message);
    
    verify();
    
    assertEquals(1, result.size());
    assertSame(r2message, result.get(0));
    assertSame(r2m, result.get(0).getMessage());
    assertSame(r2destinations, result.get(0).getDestinations());
  }

  public void testGetMultiRoutedMessages_handleReturnsRoutedMessage() throws Exception {
    IBltMessage message = new IBltMessage() {};
    IBltMessage r2m = new IBltMessage() {};
    BltRoutedMessage r2message = new BltRoutedMessage();
    r2message.setMessage(r2m);
    r2message.setDestination("ABC");
    
    rule.handle(message);
    ruleControl.setReturnValue(null);
    rule2.handle(message);
    rule2Control.setReturnValue(r2message);
    rule3.handle(message);
    rule3Control.setReturnValue(null);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replay();
    
    List<IMultiRoutedMessage> result = classUnderTest.getMultiRoutedMessages(message);
    
    verify();
    
    assertEquals(1, result.size());
    assertSame(r2m, result.get(0).getMessage());
    assertEquals("ABC", result.get(0).getDestinations().get(0));
  }
  
  public void testGetRoutedMessages() throws Exception {
    IBltMessage message = new IBltMessage() {};
    IBltMessage r2message = new IBltMessage() {};
    IBltMessage r3message = new IBltMessage() {};
    
    rule.handle(message);
    ruleControl.setReturnValue(null);
    rule2.handle(message);
    rule2Control.setReturnValue(r2message);
    rule3.handle(message);
    rule3Control.setReturnValue(r3message);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replay();
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(message);
    
    verify();
    
    assertEquals(3, result.size());
    assertSame(r2message, result.get(0).getMessage());
    assertEquals("Adaptor1", result.get(0).getDestination());
    assertSame(r3message, result.get(1).getMessage());
    assertEquals("Adaptor2", result.get(1).getDestination());
    assertSame(r3message, result.get(2).getMessage());
    assertEquals("Adaptor3", result.get(2).getDestination());
  }

  public void testGetRoutedMessages_alreadyRouted() throws Exception {
    RM message = new RM();
    
    BltRouter classUnderTest = new BltRouter();
    
    replay();
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(message);
    
    verify();
    
    assertEquals(1, result.size());
    assertSame(message, result.get(0));
  }

  public void testGetRoutedMessages_multiRouted() throws Exception {
    IBltMessage m = new IBltMessage() {};
    MRM message = new MRM();
    message.setMessage(m);
    List<String> destinations = new ArrayList<String>();
    destinations.add("ABC");
    destinations.add("DEF");
    message.setDestinations(destinations);
    
    BltRouter classUnderTest = new BltRouter();
    
    replay();
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(message);
    
    verify();
    
    assertEquals(2, result.size());
    assertSame(m, result.get(0).getMessage());
    assertSame(m, result.get(1).getMessage());
    assertEquals("ABC", result.get(0).getDestination());
    assertEquals("DEF", result.get(1).getDestination());
  }
  
  
  public void testGetRoutedMessages_noHits() throws Exception {
    IBltMessage message = new IBltMessage() {};
    
    rule.handle(message);
    ruleControl.setReturnValue(null);
    rule2.handle(message);
    rule2Control.setReturnValue(null);
    rule3.handle(message);
    rule3Control.setReturnValue(null);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replay();
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(message);
    
    verify();
    
    assertEquals(0, result.size());
  }

  public void testGetRoutedMessages_handleReturnsRoutedMessage() throws Exception {
    IBltMessage message = new IBltMessage() {};
    IBltMessage r2message = new IBltMessage() {};
    IBltMessage r3message = new IBltMessage() {};
    BltRoutedMessage routedr2message = new BltRoutedMessage();
    routedr2message.setDestination("ABC");
    routedr2message.setMessage(r2message);
    
    rule.handle(message);
    ruleControl.setReturnValue(null);
    rule2.handle(message);
    rule2Control.setReturnValue(routedr2message);
    rule3.handle(message);
    rule3Control.setReturnValue(r3message);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replay();
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(message);
    
    verify();
    
    assertEquals(3, result.size());
    assertSame(routedr2message, result.get(0));
    assertEquals("ABC", result.get(0).getDestination());
    assertSame(r2message, result.get(0).getMessage());
    
    assertSame(r3message, result.get(1).getMessage());
    assertEquals("Adaptor2", result.get(1).getDestination());
    assertSame(r3message, result.get(2).getMessage());
    assertEquals("Adaptor3", result.get(2).getDestination());
  }

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
    
    rule.handle(message);
    ruleControl.setReturnValue(null);
    rule2.handle(message);
    rule2Control.setReturnValue(routedr2message);
    rule3.handle(message);
    rule3Control.setReturnValue(r3message);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replay();
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(message);
    
    verify();
    
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

  public void testGetRoutedMessages_handleReturnsMultiRoutedMessage_withNullDest() throws Exception {
    IBltMessage message = new IBltMessage() {};
    IBltMessage r2message = new IBltMessage() {};
    IBltMessage r3message = new IBltMessage() {};
    BltMultiRoutedMessage routedr2message = new BltMultiRoutedMessage();
    routedr2message.setMessage(r2message);
    
    rule.handle(message);
    ruleControl.setReturnValue(null);
    rule2.handle(message);
    rule2Control.setReturnValue(routedr2message);
    rule3.handle(message);
    rule3Control.setReturnValue(r3message);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(definitions);
    
    replay();
    
    List<IRoutedMessage> result = classUnderTest.getRoutedMessages(message);
    
    verify();
    
    assertEquals(2, result.size());
    assertSame(r3message, result.get(0).getMessage());
    assertEquals("Adaptor2", result.get(0).getDestination());
    assertSame(r3message, result.get(1).getMessage());
    assertEquals("Adaptor3", result.get(1).getDestination());
  }
  
  
  public void testDeleteDefinition() {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    
    jdbc.update(null, new Object[]{});
    jdbcControl.setMatcher(MockControl.ALWAYS_MATCHER);
    jdbcControl.setReturnValue(0);

    jdbc.update(null, new Object[]{});
    jdbcControl.setMatcher(MockControl.ALWAYS_MATCHER);
    jdbcControl.setReturnValue(0);

    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setDefinitions(new ArrayList<RouteDefinition>());
    jdbcControl.replay();

    // execute test
    classUnderTest.deleteDefinition("X");
    
    // verify
    jdbcControl.verify();
  }
  
  public void testDeleteDefinition_throwsException() {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    
    jdbc.update(null, new Object[]{});
    jdbcControl.setMatcher(MockControl.ALWAYS_MATCHER);
    jdbcControl.setReturnValue(0);

    jdbc.update(null, new Object[]{});
    jdbcControl.setMatcher(MockControl.ALWAYS_MATCHER);
    jdbcControl.setThrowable(new RuntimeException());

    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setJdbcTemplate(jdbc);
    jdbcControl.replay();

    // execute test
    try {
      classUnderTest.deleteDefinition("X");
      fail("Expected RuleException");
    } catch (RuleException e) {
      // pass
    }
    
    // verify
    jdbcControl.verify();
  }  
  
  public void testGetDefinitions() throws Exception {
    List<RouteDefinition> defs = new ArrayList<RouteDefinition>();
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(defs);
    
    assertSame(defs, classUnderTest.getDefinitions());
  }
  
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
  
  
  
  public void testStoreRecipients() throws Exception {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    
    List<String> recipients = new ArrayList<String>();
    recipients.add("X1");
    recipients.add("X2");
    
    jdbc.update(null, new Object[]{});
    jdbcControl.setMatcher(MockControl.ALWAYS_MATCHER);
    jdbcControl.setReturnValue(0);
    jdbc.update(null, new Object[]{});
    jdbcControl.setMatcher(MockControl.ALWAYS_MATCHER);
    jdbcControl.setReturnValue(0);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setJdbcTemplate(jdbc);

    jdbcControl.replay();
    
    // execute
    classUnderTest.storeRecipients("D1", recipients);
    
    // verify
    jdbcControl.verify();
  }

  public void testStoreRecipients_nullList() throws Exception {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setJdbcTemplate(jdbc);

    jdbcControl.replay();
    
    // execute
    classUnderTest.storeRecipients("D1", null);
    
    // verify
    jdbcControl.verify();
  }

  
  public void testCreate() throws Exception {
    String name = "MyName";
    String author = "nisse";
    boolean active = true;
    String description = "some description";
    List<String> recipients = new ArrayList<String>();
    IRule rule = new IRule() {
      public IBltMessage handle(IBltMessage message) {return null;}
      public String getType() {return null;}
      public String getDefinition() {return null;}
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
}
