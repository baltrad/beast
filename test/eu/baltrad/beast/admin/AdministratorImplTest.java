/**
 * 
 */
package eu.baltrad.beast.admin;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.adaptor.IAdaptor;
import eu.baltrad.beast.adaptor.IBltAdaptorManager;
import eu.baltrad.beast.adaptor.xmlrpc.XmlRpcAdaptorConfiguration;
import eu.baltrad.beast.admin.command.AdaptorCommand;
import eu.baltrad.beast.admin.command.AnomalyDetectorCommand;
import eu.baltrad.beast.admin.command.HelpCommand;
import eu.baltrad.beast.admin.command.RouteCommand;
import eu.baltrad.beast.admin.command.ScheduleCommand;
import eu.baltrad.beast.admin.command.UserCommand;
import eu.baltrad.beast.admin.command_response.CommandResponseJsonObject;
import eu.baltrad.beast.admin.command_response.CommandResponseStatus;
import eu.baltrad.beast.admin.objects.Adaptor;
import eu.baltrad.beast.admin.objects.routes.Route;
import eu.baltrad.beast.router.IRouterManager;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.RuleException;

/**
 * @author anders
 *
 */
public class AdministratorImplTest extends EasyMockSupport {
  /**
   * Class under test
   */
  private AdministratorImpl classUnderTest = null;

  /**
   * The adaptor manager
   */
  private IBltAdaptorManager adaptorManager = null;
  
  /**
   * The router manager
   */
  private IRouterManager routerManager = null;
  
  /**
   * Utilities when working with routes
   */
  private RouteCommandHelper routeCommandHelper = null;
  
  /**
   * The json generator
   */
  private JsonGenerator jsonGenerator = null;
  
  /**
   * Provides help information when required.
   */
  private JsonCommandParser jsonCommandParser = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new AdministratorImpl();
    adaptorManager = createMock(IBltAdaptorManager.class);
    routerManager = createMock(IRouterManager.class);
    routeCommandHelper = createMock(RouteCommandHelper.class);
    jsonCommandParser = createMock(JsonCommandParser.class);
    jsonGenerator = createMock(JsonGenerator.class);
    classUnderTest.setAdaptorManager(adaptorManager);
    classUnderTest.setRouterManager(routerManager);
    classUnderTest.setRouteCommandHelper(routeCommandHelper);
    classUnderTest.setJsonCommandParser(jsonCommandParser);
    classUnderTest.setJsonGenerator(jsonGenerator);
  }
  
  @After
  public void tearDown() throws Exception {
    adaptorManager = null;
    routerManager = null;
    routeCommandHelper = null;
    jsonCommandParser = null;
    jsonGenerator = null;
    classUnderTest = null;
  }
    
  @Test
  public void handle_AdaptorCommand() {
    AdaptorCommand command = createMock(AdaptorCommand.class);

    CommandResponseStatus response = new CommandResponseStatus(true);
    
    classUnderTest = createMockBuilder(AdministratorImpl.class)
        .addMockedMethod("handleCommand", AdaptorCommand.class)
        .createMock();
    
    expect(command.validate()).andReturn(true);
    expect(classUnderTest.handleCommand(command)).andReturn(response);

    replayAll();
    
    CommandResponse result = classUnderTest.handle(command);
    
    verifyAll();
    assertSame(result, response);
  }
  
  @Test
  public void handle_AdaptorCommand_invalid() {
    AdaptorCommand command = createMock(AdaptorCommand.class);
    
    classUnderTest = createMockBuilder(AdministratorImpl.class)
        .addMockedMethod("handleCommand", AdaptorCommand.class)
        .createMock();

    expect(command.validate()).andReturn(false);

    replayAll();
    
    CommandResponse result = classUnderTest.handle(command);
    
    verifyAll();
    assertEquals(false, result.wasSuccessful());
  }
  
  @Test
  public void handle_AnomalyDetectorCommand() {
    AnomalyDetectorCommand command = createMock(AnomalyDetectorCommand.class);

    CommandResponseStatus response = new CommandResponseStatus(true);
    
    classUnderTest = createMockBuilder(AdministratorImpl.class)
        .addMockedMethod("handleCommand", AnomalyDetectorCommand.class)
        .createMock();
    
    expect(command.validate()).andReturn(true);
    expect(classUnderTest.handleCommand(command)).andReturn(response);

    replayAll();
    
    CommandResponse result = classUnderTest.handle(command);
    
    verifyAll();
    assertSame(result, response);
  }

  @Test
  public void handle_AnomalyDetectorCommand_invalid() {
    AnomalyDetectorCommand command = createMock(AnomalyDetectorCommand.class);

    classUnderTest = createMockBuilder(AdministratorImpl.class)
        .addMockedMethod("handleCommand", AnomalyDetectorCommand.class)
        .createMock();
    
    expect(command.validate()).andReturn(false);

    replayAll();
    
    CommandResponse result = classUnderTest.handle(command);
    
    verifyAll();
    assertEquals(false, result.wasSuccessful());
  }

  @Test
  public void handle_RouteCommand() {
    RouteCommand command = createMock(RouteCommand.class);
//    RouteCommand command = new RouteCommand(RouteCommand.ADD) {
//      public boolean validate() { return true; }
//    };

    CommandResponseStatus response = new CommandResponseStatus(true);
    
    classUnderTest = createMockBuilder(AdministratorImpl.class)
        .addMockedMethod("handleCommand", RouteCommand.class)
        .createMock();
    
    expect(command.validate()).andReturn(true);
    expect(classUnderTest.handleCommand(command)).andReturn(response);

    replayAll();
    
    CommandResponse result = classUnderTest.handle(command);
    
    verifyAll();
    assertSame(result, response);
  }

  @Test
  public void handle_RouteCommand_invalid() {
    RouteCommand command = createMock(RouteCommand.class);
    
    classUnderTest = createMockBuilder(AdministratorImpl.class)
        .addMockedMethod("handleCommand", RouteCommand.class)
        .createMock();
    
    expect(command.validate()).andReturn(false);

    replayAll();
    
    CommandResponse result = classUnderTest.handle(command);
    
    verifyAll();
    assertEquals(false, result.wasSuccessful());
  }

  @Test
  public void handle_ScheduleCommand() {
    ScheduleCommand command = createMock(ScheduleCommand.class);
    
    CommandResponseStatus response = new CommandResponseStatus(true);
    
    classUnderTest = createMockBuilder(AdministratorImpl.class)
        .addMockedMethod("handleCommand", ScheduleCommand.class)
        .createMock();
    
    expect(command.validate()).andReturn(true);
    expect(classUnderTest.handleCommand(command)).andReturn(response);

    replayAll();
    
    CommandResponse result = classUnderTest.handle(command);
    
    verifyAll();
    assertSame(result, response);
  }

  @Test
  public void handle_ScheduleCommand_invalid() {
    ScheduleCommand command = createMock(ScheduleCommand.class);
    
    classUnderTest = createMockBuilder(AdministratorImpl.class)
        .addMockedMethod("handleCommand", ScheduleCommand.class)
        .createMock();
    
    expect(command.validate()).andReturn(false);

    replayAll();
    
    CommandResponse result = classUnderTest.handle(command);
    
    verifyAll();
    assertSame(false, result.wasSuccessful());
  }
  
  @Test
  public void handle_HelpCommand() {
    HelpCommand command = createMock(HelpCommand.class);
    
    CommandResponseStatus response = new CommandResponseStatus(true);
    
    classUnderTest = createMockBuilder(AdministratorImpl.class)
        .addMockedMethod("handleCommand", HelpCommand.class)
        .createMock();
    
    expect(command.validate()).andReturn(true);
    expect(classUnderTest.handleCommand(command)).andReturn(response);

    replayAll();
    
    CommandResponse result = classUnderTest.handle(command);
    
    verifyAll();
    assertSame(result, response);
  }

  @Test
  public void handle_HelpCommand_invalid() {
    HelpCommand command = createMock(HelpCommand.class);
    
    classUnderTest = createMockBuilder(AdministratorImpl.class)
        .addMockedMethod("handleCommand", HelpCommand.class)
        .createMock();
    
    expect(command.validate()).andReturn(false);

    replayAll();
    
    CommandResponse result = classUnderTest.handle(command);
    
    verifyAll();
    assertSame(false, result.wasSuccessful());
  }

  @Test
  public void handle_UserCommand() {
    UserCommand command = createMock(UserCommand.class);
    
    expect(command.validate()).andReturn(true);

    replayAll();
    
    CommandResponse result = classUnderTest.handle(command);
    
    verifyAll();
    assertEquals(false, result.wasSuccessful());
  }

  @Test
  public void handle_UserCommand_invalid() {
    UserCommand command = createMock(UserCommand.class);
    
    expect(command.validate()).andReturn(false);

    replayAll();
    
    CommandResponse result = classUnderTest.handle(command);
    
    verifyAll();
    assertEquals(false, result.wasSuccessful());
  }

  
  @Test
  public void handle_invalid() {
    Command command = createMock(Command.class);
    
    expect(command.validate()).andReturn(false);
    
    replayAll();
    
    CommandResponse result = classUnderTest.handle(command);
    
    verifyAll();
    assertEquals(false, result.wasSuccessful());
  }
  
  @Test
  public void handleCommand_HelpCommand() {
    HelpCommand command = new HelpCommand(HelpCommand.HELP);
    
    expect(jsonCommandParser.getHelp(null)).andReturn("HELP");
    
    replayAll();
    
    CommandResponse result = classUnderTest.handleCommand(command);
    
    verifyAll();
    assertTrue(result instanceof CommandResponseJsonObject);
    assertEquals("HELP", ((CommandResponseJsonObject)result).getJsonString());
  }

  @Test
  public void handleCommand_HelpCommand_withMethod() {
    HelpCommand command = new HelpCommand(HelpCommand.HELP);
    command.setCommand("help_me");
    expect(jsonCommandParser.getHelp("help_me")).andReturn("HELP");
    
    replayAll();
    
    CommandResponse result = classUnderTest.handleCommand(command);
    
    verifyAll();
    assertTrue(result instanceof CommandResponseJsonObject);
    assertEquals("HELP", ((CommandResponseJsonObject)result).getJsonString());
  }

  @Test
  public void handleAdaptorCommand_ADD() {
    AdaptorCommand command = new AdaptorCommand(AdaptorCommand.ADD);
    XmlRpcAdaptorConfiguration conf = createMock(XmlRpcAdaptorConfiguration.class);
    IAdaptor adaptor = createMock(IAdaptor.class);
    command.setAdaptor(new Adaptor("nisse", "XYZ", "http://localhost", 11));
    
    expect(adaptorManager.getAdaptor("nisse")).andReturn(null);
    expect(adaptorManager.createConfiguration("XYZ", "nisse")).andReturn(conf);
    conf.setURL("http://localhost");
    conf.setTimeout(11);
    expect(adaptorManager.register(conf)).andReturn(adaptor);
    
    replayAll();
    
    CommandResponse result = classUnderTest.handleCommand(command);
    
    verifyAll();
    assertEquals(true, result.wasSuccessful());
  }

  @Test
  public void handleAdaptorCommand_UPDATE() {
    AdaptorCommand command = new AdaptorCommand(AdaptorCommand.UPDATE);
    XmlRpcAdaptorConfiguration conf = createMock(XmlRpcAdaptorConfiguration.class);
    IAdaptor adaptor = createMock(IAdaptor.class);
    IAdaptor newadaptor = createMock(IAdaptor.class);
    command.setAdaptor(new Adaptor("nisse", "XYZ", "http://localhost", 11));

    expect(adaptorManager.getAdaptor("nisse")).andReturn(adaptor);
    
    expect(adaptorManager.createConfiguration("XYZ", "nisse")).andReturn(conf);
    conf.setURL("http://localhost");
    conf.setTimeout(11);
    expect(adaptorManager.reregister(conf)).andReturn(newadaptor);
    
    replayAll();
    
    CommandResponse result = classUnderTest.handleCommand(command);
    
    verifyAll();
    assertEquals(true, result.wasSuccessful());
  }

  @Test
  public void handleAdaptorCommand_REMOVE() {
    AdaptorCommand command = new AdaptorCommand(AdaptorCommand.REMOVE);
    
    command.setAdaptor(new Adaptor("nisse"));
    
    expect(adaptorManager.getAdaptorNames()).andReturn(Arrays.asList("pelle", "nisse"));
    
    adaptorManager.unregister("nisse");
    
    replayAll();
    
    CommandResponse result = classUnderTest.handleCommand(command);
    
    verifyAll();
    assertEquals(true, result.wasSuccessful());
  }

  @Test
  public void handleRouteCommand_ADD() {
    Route route = createMock(Route.class);
    IRule rule = createMock(IRule.class);
    List<String> recipients = Arrays.asList(new String[] {"R1","R2"});
    RouteCommand command = new RouteCommand(RouteCommand.ADD, route);
    RouteDefinition definition = new RouteDefinition();
    
    expect(route.toRule(routerManager)).andReturn(rule);
    expect(route.getName()).andReturn("RULE").anyTimes();
    expect(route.getAuthor()).andReturn("AUTHOR");
    expect(route.isActive()).andReturn(true);
    expect(route.getDescription()).andReturn("DESCRIPTION");
    expect(route.getRecipients()).andReturn(recipients);
    expect(routerManager.create("RULE", "AUTHOR", true, "DESCRIPTION", recipients, rule)).andReturn(definition);
    expect(routerManager.getDefinition("RULE")).andReturn(null);

    routerManager.storeDefinition(definition);
    
    replayAll();
    
    CommandResponse result = classUnderTest.handleCommand(command);
    
    verifyAll();
    assertEquals(true, result.wasSuccessful());
  }
  
  @Test
  public void handleRouteCommand_UPDATE() {
    Route route = createMock(Route.class);
    IRule rule = createMock(IRule.class);
    List<String> recipients = Arrays.asList(new String[] {"R1","R2"});
    RouteCommand command = new RouteCommand(RouteCommand.UPDATE, route);
    RouteDefinition definition = new RouteDefinition();
    RouteDefinition storedDefinition = new RouteDefinition();
    
    expect(route.toRule(routerManager)).andReturn(rule);
    expect(route.getName()).andReturn("RULE").anyTimes();
    expect(route.getAuthor()).andReturn("AUTHOR");
    expect(route.isActive()).andReturn(true);
    expect(route.getDescription()).andReturn("DESCRIPTION");
    expect(route.getRecipients()).andReturn(recipients);
    expect(routerManager.create("RULE", "AUTHOR", true, "DESCRIPTION", recipients, rule)).andReturn(definition);
    expect(routerManager.getDefinition("RULE")).andReturn(storedDefinition);

    routerManager.updateDefinition(definition);
    
    replayAll();
    
    CommandResponse result = classUnderTest.handleCommand(command);
    
    verifyAll();
    assertEquals(true, result.wasSuccessful());
  }

  @Test
  public void handleRouteCommand_REMOVE() {
    Route route = createMock(Route.class);
    RouteCommand command = new RouteCommand(RouteCommand.REMOVE, route);
    
    expect(route.getName()).andReturn("RULE").anyTimes();
    routerManager.deleteDefinition("RULE");
    
    replayAll();
    
    CommandResponse result = classUnderTest.handleCommand(command);
    
    verifyAll();
    assertEquals(true, result.wasSuccessful());
  }

  @Test
  public void handleRouteCommand_REMOVE_nonExisting() {
    Route route = createMock(Route.class);
    RouteCommand command = new RouteCommand(RouteCommand.REMOVE, route);
    
    expect(route.getName()).andReturn("RULE").anyTimes();
    routerManager.deleteDefinition("RULE");
    EasyMock.expectLastCall().andThrow(new RuleException());
    
    replayAll();
    
    CommandResponse result = classUnderTest.handleCommand(command);
    
    verifyAll();
    assertEquals(false, result.wasSuccessful());
  }

  @Test
  public void handleRouteCommand_GET() {
    Route route = createMock(Route.class);
    RouteCommand command = new RouteCommand(RouteCommand.GET, route);
    
    RouteDefinition definition = new RouteDefinition();

    expect(route.getName()).andReturn("RULE").anyTimes();
    expect(routerManager.getDefinition("RULE")).andReturn(definition);
    expect(routeCommandHelper.createRouteFromDefinition(definition)).andReturn(route);
    expect(jsonGenerator.toJson(route)).andReturn("NISSE");
    
    replayAll();
    
    CommandResponse result = classUnderTest.handleCommand(command);
    
    verifyAll();
    assertEquals(true, result.wasSuccessful());
    assertEquals("NISSE", ((CommandResponseJsonObject)result).getJsonString());
  }

  @Test
  public void handleRouteCommand_GET_nothingFound() {
    Route route = createMock(Route.class);
    RouteCommand command = new RouteCommand(RouteCommand.GET, route);
    
    expect(route.getName()).andReturn("RULE").anyTimes();
    expect(routerManager.getDefinition("RULE")).andReturn(null);
    
    replayAll();
    
    CommandResponse result = classUnderTest.handleCommand(command);
    
    verifyAll();
    assertEquals(false, result.wasSuccessful());
  }

  @Test
  public void handleRouteCommand_LIST() {
    RouteCommand command = new RouteCommand(RouteCommand.LIST, null);
    Route route1 = createMock(Route.class);
    Route route2 = createMock(Route.class);
    List<Route> routes = new ArrayList<Route>();
    routes.add(route1);
    routes.add(route2);

    List<String> listRoutesTypes = new ArrayList<String>();
    command.setListRoutesTypes(listRoutesTypes);
    List<String> types = new ArrayList<String>();
    
    List<RouteDefinition> definitions = new ArrayList<RouteDefinition>();
    RouteDefinition definition1 = new RouteDefinition();
    RouteDefinition definition2 = new RouteDefinition();
    definitions.add(definition1);
    definitions.add(definition2);
    
    expect(routeCommandHelper.translateRouteTypesToRuleNames(listRoutesTypes)).andReturn(types);
    expect(routerManager.getDefinitions(types)).andReturn(definitions);
    expect(routeCommandHelper.createRouteFromDefinition(definition1)).andReturn(route1);
    expect(routeCommandHelper.createRouteFromDefinition(definition2)).andReturn(route2);
    expect(jsonGenerator.toJsonFromRoutes(routes)).andReturn("NISSE");
    
    replayAll();
    
    CommandResponse result = classUnderTest.handleCommand(command);
    
    verifyAll();
    assertEquals(true, result.wasSuccessful());
    assertEquals("NISSE", ((CommandResponseJsonObject)result).getJsonString());
  }

  @Test
  public void handleRouteCommand_LIST_TYPES() {
    RouteCommand command = new RouteCommand(RouteCommand.LIST_TYPES, null);
    List<String> types = new ArrayList<String>();
    types.add("A");
    types.add("B");
    
    expect(routeCommandHelper.getRouteTypes()).andReturn(types);
    
    replayAll();
    
    CommandResponse result = classUnderTest.handleCommand(command);
    
    verifyAll();
    assertEquals(true, result.wasSuccessful());
    assertEquals("[\"A\",\"B\"]", ((CommandResponseJsonObject)result).getJsonString());
  }
}
