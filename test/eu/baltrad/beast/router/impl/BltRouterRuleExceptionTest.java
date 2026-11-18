/* --------------------------------------------------------------------
Copyright (C) 2009-2026 Swedish Meteorological and Hydrological Institute, SMHI,

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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.bdb.oh5.Attribute;
import eu.baltrad.bdb.oh5.Group;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.db.rest.RestfulFileEntry;
import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;
import eu.baltrad.bdb.storage.LocalStorage;
import eu.baltrad.beast.db.AttributeFilter;
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
import eu.baltrad.beast.manager.BltMessageManager;
import eu.baltrad.beast.router.IRouter;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.dist.DistributionRule;
import eu.baltrad.beast.rules.dist.DistributionRuleManager;

/**
 * @author Isac Einarsson
 */
public class BltRouterRuleExceptionTest {
  private final List<LoggingEvent> events = new ArrayList<>();
  private Logger logger;
  private final AppenderSkeleton appender = new AppenderSkeleton() {
    @Override
    protected void append(LoggingEvent event) {
      events.add(event);
    }

    @Override
    public void close() { }

    @Override
    public boolean requiresLayout() { return false; }
  };

  private static final String UUID = "b39a906a-13fc-4151-b6b6-04c1c429e1bd";

  private Level previousLevel;
  private boolean previousAdditivity;

  @Before
  public void captureLogs() {
    logger = Logger.getLogger(BltRouter.class);
    previousLevel = logger.getLevel();
    previousAdditivity = logger.getAdditivity();
    logger.addAppender(appender);
    logger.setAdditivity(false);
    logger.setLevel(Level.INFO);
  }

  @After
  public void stopCapturingLogs() {
    logger.setLevel(previousLevel);
    logger.setAdditivity(previousAdditivity);
    logger.removeAppender(appender);
  }

  private static class RecordingRoute extends RouteDefinition {
    private RuntimeException failure;

    @Override
    public IBltMessage handle(IBltMessage msg) {
      try {
        return super.handle(msg);
      } catch (RuntimeException e) {
        failure = e;
        throw e;
      }
    }
  }

  private RecordingRoute distributionRoute(LocalStorage storage) {
    DistributionRuleManager manager = new DistributionRuleManager();
    manager.setLocalStorage(storage);
    DistributionRule rule = manager.createRule();
    rule.setFilter(doubleFilter("where/elangle"));
    RecordingRoute route = new RecordingRoute();
    route.setName("deliver scan");
    route.setRule(rule);
    return route;
  }

  private AttributeFilter doubleFilter(String selector) {
    AttributeFilter filter = new AttributeFilter();
    filter.setAttribute(selector);
    filter.setOperator(AttributeFilter.Operator.EQ);
    filter.setValueType(AttributeFilter.ValueType.DOUBLE);
    filter.setValue("0.7");
    return filter;
  }

  private BltDataMessage distributionInput() {
    Metadata metadata = new Metadata();
    metadata.addNode("/", new Group("_bdb"));
    metadata.addNode("/_bdb", new Attribute("uuid", UUID));
    metadata.addNode("/", new Group("dataset1"));
    metadata.addNode("/dataset1", new Group("where"));
    metadata.addNode("/dataset1/where", new Attribute("elangle", "0.7"));
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(new RestfulFileEntry(null, metadata));
    return msg;
  }

  @Test
  public void singleRouteLogsBadAttributeAndDeliversTheNextRoute() throws Exception {
    checkDistributionFailure(false);
  }

  @Test
  public void multiRouteLogsBadAttributeAndDeliversTheNextRoute() throws Exception {
    checkDistributionFailure(true);
  }

  private void checkDistributionFailure(boolean multi) throws Exception {
    BltDataMessage message = distributionInput();
    LocalStorage storage = createMock(LocalStorage.class);
    RecordingRoute broken = distributionRoute(storage);
    IRule next = createMock(IRule.class);
    expect(next.handle(message)).andReturn(message);
    replay(storage, next);
    RouteDefinition healthy = route("process scan", next);
    healthy.setRecipients(List.of("processor"));
    BltRouter router = new BltRouter();
    router.setDefinitions(List.of(broken, healthy));
    if (multi) {
      var results = router.getMultiRoutedMessages(message);
      assertEquals(1, results.size());
      assertEquals(List.of("processor"), results.get(0).getDestinations());
      assertSame(message, results.get(0).getMessage());
    } else {
      var results = router.getRoutedMessages(message);
      assertEquals(1, results.size());
      assertEquals("processor", results.get(0).getDestination());
      assertSame(message, results.get(0).getMessage());
    }
    verify(storage, next);
    Throwable error = checkEvent("route='deliver scan'", "rule=DistributionRule",
        "message=BltDataMessage", "fileUuid=" + UUID);
    assertNotNull("Expected the distribution rule to reject the string-valued elevation", broken.failure);
    assertSame(broken.failure, error);
  }

  @Test
  public void namedTriggerLogsJobIdAndRunsOnlyItsOwnRule() {
    BltTriggerJobMessage msg = new BltTriggerJobMessage();
    msg.setName("scheduled");
    msg.setId("test-17");
    IRule broken = createMock(IRule.class);
    IRule other = createMock(IRule.class);
    Exception original = new NullPointerException();
    expect(broken.handle(msg)).andThrow(original);
    replay(broken, other);
    BltRouter router = new BltRouter();
    router.setDefinitions(List.of(route("other", other), route("scheduled", broken)));
    assertTrue(router.getRoutedMessages(msg).isEmpty());
    verify(broken, other);
    assertSame(original, checkEvent("route='scheduled'", "jobId=test-17", "message=BltTriggerJobMessage"));
  }

  @Test
  public void unreadableUuidPreservesTheOriginalException() {
    FileEntry entry = createMock(FileEntry.class);
    expect(entry.getUuid()).andThrow(new IllegalArgumentException("invalid uuid"));
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(entry);
    IRule broken = createMock(IRule.class);
    Exception original = new NullPointerException();
    expect(broken.handle(msg)).andThrow(original);
    IRule next = createMock(IRule.class);
    expect(next.handle(msg)).andReturn(msg);
    RouteDefinition healthy = route("next", next);
    healthy.setRecipients(List.of("processor"));
    replay(entry, broken, next);
    BltRouter router = new BltRouter();
    router.setDefinitions(List.of(route("broken", broken), healthy));
    var results = router.getMultiRoutedMessages(msg);
    assertEquals(1, results.size());
    assertEquals(List.of("processor"), results.get(0).getDestinations());
    assertSame(msg, results.get(0).getMessage());
    verify(entry, broken, next);
    assertSame(original, checkEvent("route='broken'", "metadata=unavailable"));
  }

  @Test
  public void nullInputThrowsBeforeTheActiveCheckOrTheRuleCall() {
    IRule rule = createMock(IRule.class);
    replay(rule);
    RouteDefinition def = route("guard", rule);
    BltRouter router = new BltRouter();
    router.setDefinitions(List.of(def));
    for (boolean active : new boolean[] {true, false}) {
      def.setActive(active);
      assertTrue(router.getRoutedMessages(null).isEmpty());
      assertNullMessageGuard();
      assertTrue(router.getMultiRoutedMessages(null).isEmpty());
      assertNullMessageGuard();
    }
    verify(rule);
  }

  private void assertNullMessageGuard() {
    Throwable failure = checkEvent("route='guard'", "message=null");
    assertEquals(NullPointerException.class, failure.getClass());
    assertEquals("msg == null", failure.getMessage());
    assertEquals("java.lang.NullPointerException: msg == null", events.get(0).getThrowableStrRep()[0]);
    events.clear();
  }

  @Test
  public void inactiveDefinitionWithValidInputSkipsTheRuleAndLogsNothing() {
    IRule rule = createMock(IRule.class);
    replay(rule);
    RouteDefinition def = route("disabled", rule);
    def.setActive(false);
    BltRouter router = new BltRouter();
    router.setDefinitions(List.of(def));
    IBltMessage msg = new BltDataMessage();
    assertTrue(router.getRoutedMessages(msg).isEmpty());
    assertTrue(router.getMultiRoutedMessages(msg).isEmpty());
    verify(rule);
    assertTrue(events.isEmpty());
  }

  @Test
  public void singleRouteLogsBothMissingRulesAndDeliversTheNextRoute() throws Exception {
    checkMissingRules(false);
  }

  @Test
  public void multiRouteLogsBothMissingRulesAndDeliversTheNextRoute() throws Exception {
    checkMissingRules(true);
  }

  @Test
  public void twoMissingRulesLogBareNullPointerExceptions() throws Exception {
    String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
    String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
    Process child = new ProcessBuilder(java, "-XX:-ShowCodeDetailsInExceptionMessages",
        "-Dbaltrad.showRuleFailure=" + Boolean.getBoolean("baltrad.showRuleFailure"),
        "-cp", classpath, BareMissingRules.class.getName()).inheritIO().start();
    try {
      assertTrue("Timed out reproducing the missing rules", child.waitFor(60, TimeUnit.SECONDS));
      assertEquals("Missing-rule reproduction failed (see child JVM output)", 0, child.exitValue());
    } finally {
      child.destroyForcibly();
    }
  }

  public static class BareMissingRules {
    public static void main(String[] args) throws Exception {
      BltRouterRuleExceptionTest test = new BltRouterRuleExceptionTest();
      test.captureLogs();
      try {
        test.checkMissingRules(true);
        for (LoggingEvent event : test.events) {
          assertEquals("java.lang.NullPointerException", event.getThrowableStrRep()[0]);
          StackTraceElement origin = event.getThrowableInformation().getThrowable().getStackTrace()[0];
          assertEquals(RouteDefinition.class.getName(), origin.getClassName());
          assertEquals("handle", origin.getMethodName());
        }
      } finally {
        test.stopCapturingLogs();
      }
    }
  }

  @Test
  public void nullInputToMessageManagerFailsBeforeRouting() {
    ExecutorService executor = createMock(ExecutorService.class);
    IRouter router = createMock(IRouter.class);
    expect(executor.isShutdown()).andReturn(false);
    replay(executor, router);
    BltMessageManager manager = new BltMessageManager();
    manager.setExecutor(executor);
    manager.setRouter(router);
    try {
      manager.manage(null);
      fail("Expected createRunnable to reject null input");
    } catch (NullPointerException e) {
      StackTraceElement origin = e.getStackTrace()[0];
      assertEquals(BltMessageManager.class.getName(), origin.getClassName());
      assertEquals("createRunnable", origin.getMethodName());
    }
    verify(executor, router);
    assertTrue(events.isEmpty());
  }

  private void checkMissingRules(boolean multi) throws Exception {
    // Use the real row mapper. Only recipient lookup and the JDBC rows are test inputs.
    BltRouter router = new BltRouter() {
      @Override
      protected List<String> getRecipients(int ruleId) {
        return Collections.emptyList();
      }
    };
    router.setRuleManagers(Collections.emptyMap());
    RouteDefinition first = unmappedRoute(router, "first scan");
    RouteDefinition second = unmappedRoute(router, "second scan");
    BltDataMessage msg = new BltDataMessage();
    IRule next = createMock(IRule.class);
    expect(next.handle(msg)).andReturn(msg);
    replay(next);
    RouteDefinition healthy = route("working", next);
    healthy.setRecipients(List.of("processor"));
    router.setDefinitions(List.of(first, second, healthy));

    if (multi) {
      var routed = router.getMultiRoutedMessages(msg);
      assertEquals(1, routed.size());
      assertEquals(List.of("processor"), routed.get(0).getDestinations());
      assertSame(msg, routed.get(0).getMessage());
    } else {
      var routed = router.getRoutedMessages(msg);
      assertEquals(1, routed.size());
      assertEquals("processor", routed.get(0).getDestination());
      assertSame(msg, routed.get(0).getMessage());
    }
    verify(next);
    assertEquals(2, events.size());
    assertMissingRuleEvent(events.get(0), "first scan");
    assertMissingRuleEvent(events.get(1), "second scan");
  }

  private RouteDefinition unmappedRoute(BltRouter router, String name) throws Exception {
    ResultSet row = createNiceMock(ResultSet.class);
    expect(row.getString("name")).andReturn(name);
    expect(row.getString("type")).andReturn("missing-manager");
    expect(row.getBoolean("active")).andReturn(true);
    replay(row);
    RouteDefinition def = router.getRouteDefinitionMapper().mapRow(row, 0);
    verify(row);
    assertTrue(def.isActive());
    assertNull(def.getRule());
    return def;
  }

  private void assertMissingRuleEvent(LoggingEvent event, String name) {
    assertEquals(Level.INFO, event.getLevel());
    assertEquals("Rule caused exception route='" + name + "' rule=null message=BltDataMessage",
        event.getRenderedMessage());
    Throwable failure = event.getThrowableInformation().getThrowable();
    assertEquals(NullPointerException.class, failure.getClass());
    assertEquals(failure.toString(), event.getThrowableStrRep()[0]);
    if (Boolean.getBoolean("baltrad.showRuleFailure")) {
      System.out.println(event.getLevel() + " " + event.getRenderedMessage());
      failure.printStackTrace(System.out);
    }
  }

  @Test
  public void exceptionAfterTheRuleReturnsStillPreventsDelivery() {
    // RouteDefinition builds its DEBUG message before returning the rule's result.
    NullPointerException original = new NullPointerException();
    RouteDefinition def = new RouteDefinition() {
      @Override
      public String getName() {
        throw original;
      }
    };
    BltDataMessage msg = new BltDataMessage();
    IRule rule = createMock(IRule.class);
    expect(rule.handle(msg)).andReturn(msg).times(2);
    replay(rule);
    def.setRule(rule);
    def.setRecipients(List.of("processor"));
    BltRouter router = new BltRouter();
    router.setDefinitions(List.of(def));
    assertTrue(router.getRoutedMessages(msg).isEmpty());
    assertSame(original, checkEvent("message=BltDataMessage", "metadata=unavailable"));
    events.clear();
    assertTrue(router.getMultiRoutedMessages(msg).isEmpty());
    assertSame(original, checkEvent("message=BltDataMessage", "metadata=unavailable"));
    verify(rule);
  }

  @Test
  public void nullResultIsQuietInBothEntryPoints() {
    IBltMessage msg = new IBltMessage() {};
    IRule rule = createMock(IRule.class);
    expect(rule.handle(msg)).andReturn(null).times(2);
    replay(rule);
    BltRouter router = new BltRouter();
    router.setDefinitions(List.of(route("no match", rule)));
    assertTrue(router.getMultiRoutedMessages(msg).isEmpty());
    assertTrue(router.getRoutedMessages(msg).isEmpty());
    verify(rule);
    assertTrue(events.isEmpty());
  }

  @Test
  public void alertIdentifiesModuleCodeAndSeverity() {
    BltAlertMessage msg = new BltAlertMessage();
    msg.setModule("receiver");
    msg.setCode("00201");
    msg.setSeverity("WARNING");
    checkBothRoutes(msg, "message=BltAlertMessage module=receiver code=00201 severity=WARNING");
  }

  @Test
  public void commandIdentifiesRequestedCommand() {
    BltCommandMessage msg = new BltCommandMessage();
    msg.setCommand("refresh");
    checkBothRoutes(msg, "message=BltCommandMessage command=refresh");
  }

  @Test
  public void frameIdentifiesInputFile() {
    BltDataFrameMessage msg = new BltDataFrameMessage();
    msg.setFilename("/incoming/scan.h5");
    checkBothRoutes(msg, "message=BltDataFrameMessage file=scan.h5");
  }

  @Test
  public void generateIdentifiesAlgorithm() {
    BltGenerateMessage msg = new BltGenerateMessage();
    msg.setAlgorithm("generatecomposite");
    checkBothRoutes(msg, "message=BltGenerateMessage algorithm=generatecomposite");
  }

  @Test
  public void metadataQueriesAreIdentifiedByType() {
    checkBothRoutes(new BltGetAreasMessage(), "message=BltGetAreasMessage");
    checkBothRoutes(new BltGetPcsDefinitionsMessage(), "message=BltGetPcsDefinitionsMessage");
    checkBothRoutes(new BltGetQualityControlsMessage(), "message=BltGetQualityControlsMessage");
  }

  @Test
  public void missingFileEntryStillIdentifiesDataMessage() {
    checkBothRoutes(new BltDataMessage(), "message=BltDataMessage");
  }

  @Test
  public void customMessageNeedsNoFormattingContract() {
    IBltMessage msg = new IBltMessage() {
      public String toString() {
        throw new AssertionError("The router must not render the message body");
      }
    };
    checkBothRoutes(msg, "message=BltRouterRuleExceptionTest$");
  }

  private void checkBothRoutes(IBltMessage msg, String context) {
    IRule rule = createMock(IRule.class);
    RuntimeException original = new NullPointerException();
    expect(rule.handle(msg)).andThrow(original).times(2);
    replay(rule);
    BltRouter router = new BltRouter();
    router.setDefinitions(List.of(route("check", rule)));
    assertTrue(router.getRoutedMessages(msg).isEmpty());
    assertSame(original, checkEvent("route='check'", context));
    events.clear();
    assertTrue(router.getMultiRoutedMessages(msg).isEmpty());
    assertSame(original, checkEvent("route='check'", context));
    events.clear();
    verify(rule);
  }

  @Test
  public void routedInputsBypassConfiguredRules() {
    BltDataMessage payload = new BltDataMessage();
    BltRoutedMessage single = new BltRoutedMessage();
    single.setMessage(payload);
    single.setDestination("first");
    BltMultiRoutedMessage multi = new BltMultiRoutedMessage();
    multi.setMessage(payload);
    multi.setDestinations(List.of("first", "second"));
    IRule rule = createMock(IRule.class);
    replay(rule);
    BltRouter router = new BltRouter();
    router.setDefinitions(List.of(route("unused", rule)));
    assertSame(single, router.getRoutedMessages(single).get(0));
    assertSame(multi, router.getMultiRoutedMessages(multi).get(0));
    var wrapped = router.getMultiRoutedMessages(single);
    assertEquals(1, wrapped.size());
    assertSame(payload, wrapped.get(0).getMessage());
    assertEquals(List.of("first"), wrapped.get(0).getDestinations());
    var split = router.getRoutedMessages(multi);
    assertEquals(2, split.size());
    assertEquals("first", split.get(0).getDestination());
    assertEquals("second", split.get(1).getDestination());
    assertSame(payload, split.get(0).getMessage());
    assertSame(payload, split.get(1).getMessage());
    assertTrue(events.isEmpty());
    verify(rule);
  }

  @Test
  public void disabledInfoSkipsMessageMetadata() {
    logger.setLevel(Level.WARN);
    BltDataMessage msg = new BltDataMessage() {
      @Override
      public FileEntry getFileEntry() {
        throw new AssertionError("Unexpected file metadata lookup");
      }
    };
    IRule rule = createMock(IRule.class);
    expect(rule.handle(msg)).andThrow(new NullPointerException());
    replay(rule);
    BltRouter router = new BltRouter();
    router.setDefinitions(List.of(route("disabled logging", rule)));
    assertTrue(router.getMultiRoutedMessages(msg).isEmpty());
    assertTrue(events.isEmpty());
    verify(rule);
  }

  @Test
  public void unavailableRuleMetadataKeepsRouteAndOriginalFailure() {
    RuntimeException original = new NullPointerException();
    RouteDefinition def = new RouteDefinition() {
      @Override
      public IBltMessage handle(IBltMessage msg) {
        throw original;
      }

      @Override
      public IRule getRule() {
        throw new IllegalStateException("metadata lookup failed");
      }
    };
    def.setName("scheduled delivery");
    BltRouter router = new BltRouter();
    router.setDefinitions(List.of(def));
    assertTrue(router.getRoutedMessages(new BltCommandMessage()).isEmpty());
    assertSame(original, checkEvent("route='scheduled delivery'", "metadata=unavailable",
        "message=BltCommandMessage"));
  }

  private RouteDefinition route(String name, IRule rule) {
    RouteDefinition route = new RouteDefinition();
    route.setName(name);
    route.setRule(rule);
    return route;
  }

  private Throwable checkEvent(String... fields) {
    assertEquals(1, events.size());
    LoggingEvent event = events.get(0);
    assertEquals(Level.INFO, event.getLevel());
    for (String field : fields) {
      assertTrue(event.getRenderedMessage(), event.getRenderedMessage().contains(field));
    }
    Throwable cause = event.getThrowableInformation().getThrowable();
    if (Boolean.getBoolean("baltrad.showRuleFailure")) {
      System.out.println(event.getLevel() + " " + event.getRenderedMessage());
      cause.printStackTrace(System.out);
    }
    return cause;
  }
}
