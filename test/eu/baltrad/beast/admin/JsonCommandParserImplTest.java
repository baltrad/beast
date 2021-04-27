/**
 * 
 */
package eu.baltrad.beast.admin;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.admin.command.AdaptorCommand;
import eu.baltrad.beast.admin.command.AnomalyDetectorCommand;
import eu.baltrad.beast.admin.command.HelpCommand;
import eu.baltrad.beast.admin.command.RouteCommand;
import eu.baltrad.beast.admin.command.ScheduleCommand;
import eu.baltrad.beast.admin.command.SettingCommand;
import eu.baltrad.beast.admin.command.UserCommand;
import eu.baltrad.beast.admin.objects.Adaptor;
import eu.baltrad.beast.admin.objects.User;
import eu.baltrad.beast.admin.objects.routes.AcrrRoute;
import eu.baltrad.beast.admin.objects.routes.CompositeRoute;
import eu.baltrad.beast.scheduler.CronEntry;

/**
 * Tests for verifying JsonCommandParserImplTest
 * @author anders
 */
public class JsonCommandParserImplTest  extends EasyMockSupport {
  private final static String JSON_ADAPTOR_FIXTURE = "fixtures/adaptor.json";
  private final static String JSON_ANOMALY_DETECTOR_FIXTURE = "fixtures/anomaly-detector.json";
  private final static String JSON_COMPOSITE_ROUTE_FIXTURE = "fixtures/composite_route.json";
  private final static String JSON_SCHEDULE_FIXTURE = "fixtures/schedule.json";
  private final static String JSON_USER_FIXTURE = "fixtures/user.json";

  private JsonCommandParserImpl classUnderTest = null;

  private RouteCommandHelper routeCommandHelper = null;
  
  private ObjectMapper jsonMapper = new ObjectMapper();

  @Before
  public void setUp() throws Exception {
    routeCommandHelper = createMock(RouteCommandHelper.class);
    classUnderTest = new JsonCommandParserImpl();
    classUnderTest.setRouteCommandHelper(routeCommandHelper);
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void parse_InputStream() throws Exception {
    InputStream is = new ByteArrayInputStream("ABC".getBytes());
    ObjectMapper mapper = createMock(ObjectMapper.class);
    Command command = new Command() {
      @Override
      public String getOperation() {
        return null;
      }
      @Override
      public boolean validate() {
        return true;
      }
    };
    
    classUnderTest = createMockBuilder(JsonCommandParserImpl.class)
        .addMockedMethod("parse", String.class)
        .createMock();
    classUnderTest.setObjectMapper(mapper);
    
    expect(classUnderTest.parse("ABC")).andReturn(command);
    
    replayAll();
    
    Command result = classUnderTest.parse(is);
    
    verifyAll();
    assertSame(command, result);
  }
  
  @Test
  public void parse_string() throws Exception {
    String s = "ABC";
    ObjectMapper mapper = createMock(ObjectMapper.class);
    JsonNode node = createMock(JsonNode.class);
    Command command = new Command() {
      @Override
      public String getOperation() {
        return null;
      }
      @Override
      public boolean validate() {
        return true;
      }
    };
    
    classUnderTest = createMockBuilder(JsonCommandParserImpl.class)
        .addMockedMethod("parse", JsonNode.class)
        .createMock();
    classUnderTest.setObjectMapper(mapper);
    
    expect(mapper.readTree(s)).andReturn(node);
    expect(classUnderTest.parse(node)).andReturn(command);
    
    replayAll();
    
    Command result = classUnderTest.parse(s);
    
    verifyAll();
    assertSame(command, result);
    assertEquals("ABC", result.getRawMessage());
  }

  @Test
  public void parse_JsonNode() throws Exception {
    JsonNode node = createMock(JsonNode.class);
    JsonNode commandNode = createMock(JsonNode.class);
    JsonNode argumentNode = createMock(JsonNode.class);
    AdaptorCommand command = new AdaptorCommand();
    
    classUnderTest = createMockBuilder(JsonCommandParserImpl.class)
        .addMockedMethod("parseCommand", String.class, JsonNode.class)
        .createMock();
    expect(node.asText()).andReturn("X");
    expect(node.has("command")).andReturn(true);
    expect(node.get("command")).andReturn(commandNode);
    expect(commandNode.asText()).andReturn("thecommand");
    expect(node.has("arguments")).andReturn(true);
    expect(node.get("arguments")).andReturn(argumentNode);
    expect(classUnderTest.parseCommand("thecommand", argumentNode)).andReturn(command);
    
    replayAll();
    
    Command result = classUnderTest.parse(node);
    
    verifyAll();
    assertSame(command, result);
  }
  
  @Test
  public void parseCommand_HelpCommand() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    HelpCommand command = new HelpCommand();
    
    classUnderTest = createMockBuilder(JsonCommandParserImpl.class)
        .addMockedMethod("parseHelpCommand", String.class, JsonNode.class)
        .createMock();
    
    expect(classUnderTest.parseHelpCommand(HelpCommand.HELP, arguments)).andReturn(command);
    
    replayAll();
    
    Command result = classUnderTest.parseCommand(HelpCommand.HELP, arguments);
    
    verifyAll();
    assertSame(command, result);
  }
 
  @Test
  public void parseCommand_AdaptorCommand() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    AdaptorCommand command = new AdaptorCommand();
    
    classUnderTest = createMockBuilder(JsonCommandParserImpl.class)
        .addMockedMethod("parseAdaptorCommand", String.class, JsonNode.class)
        .createMock();
    
    expect(classUnderTest.parseAdaptorCommand(AdaptorCommand.ADD, arguments)).andReturn(command);
    
    replayAll();
    
    Command result = classUnderTest.parseCommand(AdaptorCommand.ADD, arguments);
    
    verifyAll();
    assertSame(command, result);
  }

  @Test
  public void parseCommand_AnomalyDetectorCommand() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    AnomalyDetectorCommand command = new AnomalyDetectorCommand();
    
    classUnderTest = createMockBuilder(JsonCommandParserImpl.class)
        .addMockedMethod("parseAnomalyDetectorCommand", String.class, JsonNode.class)
        .createMock();
    
    expect(classUnderTest.parseAnomalyDetectorCommand(AnomalyDetectorCommand.ADD, arguments)).andReturn(command);
    
    replayAll();
    
    Command result = classUnderTest.parseCommand(AnomalyDetectorCommand.ADD, arguments);
    
    verifyAll();
    assertSame(command, result);
  }

  @Test
  public void parseCommand_RouteCommand() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    RouteCommand command = new RouteCommand(RouteCommand.ADD);
    
    classUnderTest = createMockBuilder(JsonCommandParserImpl.class)
        .addMockedMethod("parseRouteCommand", String.class, JsonNode.class)
        .createMock();
    
    expect(classUnderTest.parseRouteCommand(RouteCommand.ADD, arguments)).andReturn(command);
    
    replayAll();
    
    Command result = classUnderTest.parseCommand(RouteCommand.ADD, arguments);
    
    verifyAll();
    assertSame(command, result);
  }
  
  @Test
  public void parseCommand_SettingCommand() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    SettingCommand command = new SettingCommand(SettingCommand.UPDATE_SETTINGS);
    
    classUnderTest = createMockBuilder(JsonCommandParserImpl.class)
        .addMockedMethod("parseSettingCommand", String.class, JsonNode.class)
        .createMock();
    
    expect(classUnderTest.parseSettingCommand(SettingCommand.UPDATE_SETTINGS, arguments)).andReturn(command);
    
    replayAll();
    
    Command result = classUnderTest.parseCommand(SettingCommand.UPDATE_SETTINGS, arguments);
    
    verifyAll();
    assertSame(command, result);
  }
  
  @Test
  public void parseCommand_ScheduleCommand() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    ScheduleCommand command = new ScheduleCommand(ScheduleCommand.ADD);
    
    classUnderTest = createMockBuilder(JsonCommandParserImpl.class)
        .addMockedMethod("parseScheduleCommand", String.class, JsonNode.class)
        .createMock();
    
    expect(classUnderTest.parseScheduleCommand(ScheduleCommand.ADD, arguments)).andReturn(command);
    
    replayAll();
    
    Command result = classUnderTest.parseCommand(ScheduleCommand.ADD, arguments);
    
    verifyAll();
    assertSame(command, result);
  }

  @Test
  public void parseCommand_UserCommand() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    UserCommand command = new UserCommand(UserCommand.CHANGE_PASSWORD);
    
    classUnderTest = createMockBuilder(JsonCommandParserImpl.class)
        .addMockedMethod("parseUserCommand", String.class, JsonNode.class)
        .createMock();
    
    expect(classUnderTest.parseUserCommand(UserCommand.CHANGE_PASSWORD, arguments)).andReturn(command);
    
    replayAll();
    
    Command result = classUnderTest.parseCommand(UserCommand.CHANGE_PASSWORD, arguments);
    
    verifyAll();
    assertSame(command, result);
  }
  
  @Test
  public void parseCommand_UnknownCommand() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    
    replayAll();
    
    Command result = classUnderTest.parseCommand("unknown", arguments);
    
    verifyAll();
    assertSame(null, result);
  }
  
  @Test
  public void parseHelpCommand() throws Exception {
    replayAll();
    
    HelpCommand result = (HelpCommand)classUnderTest.parseHelpCommand(HelpCommand.HELP, null);
    
    verifyAll();
    
    assertEquals(HelpCommand.HELP, result.getOperation());
    assertEquals(null, result.getCommand());
  }
  
  @Test
  public void parseHelpCommand_withCommand() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    JsonNode commandNode = createMock(JsonNode.class);
    
    expect(arguments.has("command")).andReturn(true);
    expect(arguments.get("command")).andReturn(commandNode);
    expect(commandNode.asText()).andReturn("thisfunc");
    
    replayAll();
    
    HelpCommand result = (HelpCommand)classUnderTest.parseHelpCommand(HelpCommand.HELP, arguments);
    
    verifyAll();
    
    assertEquals(HelpCommand.HELP, result.getOperation());
    assertEquals("thisfunc", result.getCommand());
  }
  
  @Test
  public void parseAdaptorCommand_ADD_withFixture() throws Exception {
    File f = new File(this.getClass().getResource(JSON_ADAPTOR_FIXTURE).getFile());
    
    AdaptorCommand result = (AdaptorCommand)classUnderTest.parseAdaptorCommand(AdaptorCommand.ADD, jsonMapper.readTree(new String(Files.readAllBytes(f.toPath()))));
    
    assertEquals("add_adaptor", result.getOperation());
    assertEquals("RAVE", result.getAdaptor().getName());
    assertEquals(5000, result.getAdaptor().getTimeout());
    assertEquals("XMLRPC", result.getAdaptor().getType());
    assertEquals("http://localhost:8085/RAVE", result.getAdaptor().getUri());
  }
  
  @Test
  public void parseAdaptorCommand_ADD() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    Adaptor adaptor = new Adaptor("theadaptor", "XMLRPC", "http://localhost:8085/RAVE", 5000);
    JsonNode adaptorNode = jsonMapper.valueToTree(adaptor);

    expect(arguments.get("adaptor")).andReturn(adaptorNode);
    
    replayAll();
    
    AdaptorCommand result = (AdaptorCommand)classUnderTest.parseAdaptorCommand(AdaptorCommand.ADD, arguments);
    
    verifyAll();
    assertEquals("add_adaptor", result.getOperation());
    assertEquals("theadaptor", result.getAdaptor().getName());
    assertEquals("XMLRPC", result.getAdaptor().getType());
    assertEquals("http://localhost:8085/RAVE", result.getAdaptor().getUri());
    assertEquals(5000, result.getAdaptor().getTimeout());
  }

  @Test
  public void parseAdaptorCommand_UPDATE() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    Adaptor adaptor = new Adaptor("theadaptor", "XMLRPC", "http://localhost:8085/RAVE", 5000);
    JsonNode adaptorNode = jsonMapper.valueToTree(adaptor);

    expect(arguments.get("adaptor")).andReturn(adaptorNode);

    replayAll();
    
    AdaptorCommand result = (AdaptorCommand)classUnderTest.parseAdaptorCommand(AdaptorCommand.UPDATE, arguments);
    
    verifyAll();
    
    assertEquals("update_adaptor", result.getOperation());
    assertEquals("theadaptor", result.getAdaptor().getName());
    assertEquals("XMLRPC", result.getAdaptor().getType());
    assertEquals("http://localhost:8085/RAVE", result.getAdaptor().getUri());
    assertEquals(5000, result.getAdaptor().getTimeout());
  }

  @Test
  public void parseAdaptorCommand_REMOVE() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    JsonNode adaptorNode = createMock(JsonNode.class);
    JsonNode nameNode = createMock(JsonNode.class);
    
    expect(arguments.has("adaptor")).andReturn(true);
    expect(arguments.get("adaptor")).andReturn(adaptorNode).anyTimes();
    expect(adaptorNode.has("name")).andReturn(true);
    expect(adaptorNode.get("name")).andReturn(nameNode);
    expect(nameNode.asText()).andReturn("thename");

    replayAll();
    
    AdaptorCommand result = (AdaptorCommand)classUnderTest.parseAdaptorCommand(AdaptorCommand.REMOVE, arguments);
    
    verifyAll();
    
    assertEquals("remove_adaptor", result.getOperation());
    assertEquals("thename", result.getAdaptor().getName());
  }
  
  @Test
  public void parseAdaptorCommand_REMOVE_withName() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    JsonNode nameNode = createMock(JsonNode.class);

    expect(arguments.has("adaptor")).andReturn(false);
    expect(arguments.has("name")).andReturn(true);
    expect(arguments.get("name")).andReturn(nameNode);
    expect(nameNode.asText()).andReturn("theadaptor");

    replayAll();
    
    AdaptorCommand result = (AdaptorCommand)classUnderTest.parseAdaptorCommand(AdaptorCommand.REMOVE, arguments);
    
    verifyAll();
    assertEquals("remove_adaptor", result.getOperation());
    assertEquals("theadaptor", result.getAdaptor().getName());
  }
  
  
  @Test
  public void parseAdaptorCommand_REMOVE_with_args() throws Exception {
    JsonNode argumentsNode = createMock(JsonNode.class);
    JsonNode nameNode = createMock(JsonNode.class);
    
    expect(argumentsNode.has("adaptor")).andReturn(false);
    expect(argumentsNode.has("name")).andReturn(true);
    expect(argumentsNode.get("name")).andReturn(nameNode);
    expect(nameNode.asText()).andReturn("R");
    
    replayAll();
    
    AdaptorCommand result = (AdaptorCommand)classUnderTest.parseAdaptorCommand(AdaptorCommand.REMOVE, argumentsNode);
    
    verifyAll();
    assertEquals("remove_adaptor", result.getOperation());
    assertEquals("R", result.getAdaptor().getName());
  }
  
  @Test
  public void parseAdaptorCommand_GET() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    JsonNode adaptorNode = createMock(JsonNode.class);
    JsonNode nameNode = createMock(JsonNode.class);
    
    expect(arguments.has("adaptor")).andReturn(true);
    expect(arguments.get("adaptor")).andReturn(adaptorNode).anyTimes();
    expect(adaptorNode.has("name")).andReturn(true);
    expect(adaptorNode.get("name")).andReturn(nameNode);
    expect(nameNode.asText()).andReturn("thename");

    replayAll();
    
    AdaptorCommand result = (AdaptorCommand)classUnderTest.parseAdaptorCommand(AdaptorCommand.GET, arguments);
    
    verifyAll();
    
    assertEquals("get_adaptor", result.getOperation());
    assertEquals("thename", result.getAdaptor().getName());
  }

  @Test
  public void parseAdaptorCommand_GET_with_args() throws Exception {
    JsonNode argumentsNode = createMock(JsonNode.class);
    JsonNode nameNode = createMock(JsonNode.class);
    
    expect(argumentsNode.has("adaptor")).andReturn(false);
    expect(argumentsNode.has("name")).andReturn(true);
    expect(argumentsNode.get("name")).andReturn(nameNode);
    expect(nameNode.asText()).andReturn("R");
    
    replayAll();
    
    AdaptorCommand result = (AdaptorCommand)classUnderTest.parseAdaptorCommand(AdaptorCommand.GET, argumentsNode);
    
    verifyAll();
    assertEquals("get_adaptor", result.getOperation());
    assertEquals("R", result.getAdaptor().getName());
  }
  
  @Test
  public void parseAdaptorCommand_LIST() throws Exception {
    replayAll();
    
    AdaptorCommand result = (AdaptorCommand)classUnderTest.parseAdaptorCommand(AdaptorCommand.LIST, null);
    
    verifyAll();
    assertEquals("list_adaptors", result.getOperation());
  }
  
  @Test
  public void parseAnomalyDetectorCommand_ADD_withFixture() throws Exception {
    File f = new File(this.getClass().getResource(JSON_ANOMALY_DETECTOR_FIXTURE).getFile());
    
    AnomalyDetectorCommand result = (AnomalyDetectorCommand)classUnderTest.parseAnomalyDetectorCommand(AnomalyDetectorCommand.ADD, jsonMapper.readTree(new String(Files.readAllBytes(f.toPath()))));
    
    assertEquals("add_anomaly_detector", result.getOperation());
    assertEquals("beamb", result.getAnomalyDetector().getName());
    assertEquals("beamb quality control", result.getAnomalyDetector().getDescription());
  }

  @Test
  public void parseAnomalyDetectorCommand_UPDATE() throws Exception {
    File f = new File(this.getClass().getResource(JSON_ANOMALY_DETECTOR_FIXTURE).getFile());

    AnomalyDetectorCommand result = (AnomalyDetectorCommand)classUnderTest.parseAnomalyDetectorCommand(AnomalyDetectorCommand.UPDATE, jsonMapper.readTree(new String(Files.readAllBytes(f.toPath()))));
    
    assertEquals("update_anomaly_detector", result.getOperation());
    assertEquals("beamb", result.getAnomalyDetector().getName());
    assertEquals("beamb quality control", result.getAnomalyDetector().getDescription());
  }

  @Test
  public void parseAnomalyDetectorCommand_REMOVE() throws Exception {
    File f = new File(this.getClass().getResource(JSON_ANOMALY_DETECTOR_FIXTURE).getFile());
    
    AnomalyDetectorCommand result = (AnomalyDetectorCommand)classUnderTest.parseAnomalyDetectorCommand(AnomalyDetectorCommand.REMOVE, jsonMapper.readTree(new String(Files.readAllBytes(f.toPath()))));
    
    assertEquals("remove_anomaly_detector", result.getOperation());
    assertEquals("beamb", result.getAnomalyDetector().getName());
  }

  @Test
  public void parseAnomalyDetectorCommand_REMOVE_with_name() throws Exception {
    JsonNode argumentsNode = createMock(JsonNode.class);
    JsonNode nameNode = createMock(JsonNode.class);
    
    expect(argumentsNode.get("anomaly-detector")).andReturn(null);
    expect(argumentsNode.has("name")).andReturn(true).anyTimes();
    expect(argumentsNode.get("name")).andReturn(nameNode).anyTimes();
    expect(nameNode.asText()).andReturn("thename");
    
    replayAll();
    
    AnomalyDetectorCommand result = (AnomalyDetectorCommand)classUnderTest.parseAnomalyDetectorCommand(AnomalyDetectorCommand.REMOVE, argumentsNode);
    
    verifyAll();
    assertEquals("remove_anomaly_detector", result.getOperation());
    assertEquals("thename", result.getAnomalyDetector().getName());
  }

  @Test
  public void parseAnomalyDetectorCommand_GET() throws Exception {
    File f = new File(this.getClass().getResource(JSON_ANOMALY_DETECTOR_FIXTURE).getFile());
    
    replayAll();
    
    AnomalyDetectorCommand result = (AnomalyDetectorCommand)classUnderTest.parseAnomalyDetectorCommand(AnomalyDetectorCommand.GET, jsonMapper.readTree(new String(Files.readAllBytes(f.toPath()))));
    
    verifyAll();
    assertEquals("get_anomaly_detector", result.getOperation());
    assertEquals("beamb", result.getAnomalyDetector().getName());
  }

  @Test
  public void parseAnomalyDetectorCommand_GET_with_name() throws Exception {
    JsonNode argumentsNode = createMock(JsonNode.class);
    JsonNode nameNode = createMock(JsonNode.class);
    
    expect(argumentsNode.get("anomaly-detector")).andReturn(null);
    expect(argumentsNode.has("name")).andReturn(true).anyTimes();
    expect(argumentsNode.get("name")).andReturn(nameNode).anyTimes();
    expect(nameNode.asText()).andReturn("thename");
    
    replayAll();
    
    AnomalyDetectorCommand result = (AnomalyDetectorCommand)classUnderTest.parseAnomalyDetectorCommand(AnomalyDetectorCommand.GET, argumentsNode);
    
    verifyAll();
    assertEquals("get_anomaly_detector", result.getOperation());
    assertEquals("thename", result.getAnomalyDetector().getName());
  }

  @Test
  public void parseAnomalyDetectorCommand_LIST() throws Exception {
    JsonNode argumentsNode = createMock(JsonNode.class);
    
    replayAll();
    
    AnomalyDetectorCommand result = (AnomalyDetectorCommand)classUnderTest.parseAnomalyDetectorCommand(AnomalyDetectorCommand.LIST, argumentsNode);
    
    verifyAll();
    assertEquals("list_anomaly_detectors", result.getOperation());
  }

  @Test
  public void parseRouteCommand_ADD_composite_route_withFixture() throws Exception {
    File f = new File(this.getClass().getResource(JSON_COMPOSITE_ROUTE_FIXTURE).getFile());
    
    classUnderTest.setRouteCommandHelper(new RouteCommandHelper());
    
    RouteCommand result = (RouteCommand)classUnderTest.parseRouteCommand(RouteCommand.ADD, jsonMapper.readTree(new String(Files.readAllBytes(f.toPath()))));
    
    assertEquals("add_route", result.getOperation());
    CompositeRoute route = (CompositeRoute)result.getRoute();
    assertEquals("swegmap2km", route.getName());
    assertEquals("Nisse", route.getAuthor());
    assertEquals(true, route.isActive());
    assertEquals("Swedish gmap composite 2km resolution", route.getDescription());
    assertEquals(true, route.isScanBased());
    assertEquals("PPI", route.getMethod());
    assertEquals("0.5", route.getProdpar());
    assertEquals("HEIGHT_ABOVE_SEALEVEL", route.getSelectionMethod());
    assertEquals(1, route.getRecipients().size());
    assertEquals("RAVE", route.getRecipients().get(0));
    assertEquals("swegmaps2km", route.getArea());
    assertEquals("DBZH", route.getQuantity());
    assertEquals(5, route.getInterval());
    assertEquals(30, route.getTimeout());
    assertEquals(false, route.isNominalTimeout());
    assertEquals(-1, route.getMaxAgeLimit());
    assertEquals(false, route.isApplyGRA());
    assertEquals(200.0, route.getZR_A(), 4);
    assertEquals(1.6, route.getZR_b(), 4);
    assertEquals(false, route.isIgnoreMalfunc());
    assertEquals(false, route.isCtFilter());
    assertEquals("", route.getQitotalField());
    assertEquals(12, route.getSources().size());
    assertEquals("sekrn", route.getSources().get(0));
    assertEquals("sella", route.getSources().get(1));
    assertEquals("seosd", route.getSources().get(2));
    assertEquals("seoer", route.getSources().get(3));
    assertEquals("sehuv", route.getSources().get(4));
    assertEquals("selek", route.getSources().get(5));
    assertEquals("sehem", route.getSources().get(6));
    assertEquals("seatv", route.getSources().get(7));
    assertEquals("sevax", route.getSources().get(8));
    assertEquals("seang", route.getSources().get(9));
    assertEquals("sekaa", route.getSources().get(10));
    assertEquals("sebaa", route.getSources().get(11));
    assertEquals(2, route.getDetectors().size());
    assertEquals("ropo", route.getDetectors().get(0));
    assertEquals("distance", route.getDetectors().get(1));
    assertEquals("ANALYZE_AND_APPLY", route.getQualityControlMode());    
  }
  
  @Test
  public void parseRouteCommand_ADD_composite_route() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    CompositeRoute compositeRoute = new CompositeRoute("B");
    JsonNode bNode = jsonMapper.valueToTree(compositeRoute);
    
    List<String> types = Arrays.asList(new String[] {"A","B","C"});
    expect(arguments.asText()).andReturn("X");
    expect(routeCommandHelper.getRouteTypes()).andReturn(types);
    expect(arguments.has("A")).andReturn(false);
    expect(arguments.has("B")).andReturn(true);
    expect(arguments.get("B")).andReturn(bNode);
    expect(routeCommandHelper.getRouteClass("B")).andReturn(CompositeRoute.class);
    
    replayAll();
    
    
    RouteCommand result = (RouteCommand)classUnderTest.parseRouteCommand(RouteCommand.ADD, arguments);
    
    verifyAll();
    assertEquals(RouteCommand.ADD, result.getOperation());
    assertEquals("B", result.getRoute().getName());
  }

  @Test
  public void parseRouteCommand_UPDATE_acrr_route() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    AcrrRoute acrrRoute = new AcrrRoute("C");
    JsonNode cNode = jsonMapper.valueToTree(acrrRoute);
    
    List<String> types = Arrays.asList(new String[] {"A","B","C"});
    expect(arguments.asText()).andReturn("X");
    expect(routeCommandHelper.getRouteTypes()).andReturn(types);
    expect(arguments.has("A")).andReturn(false);
    expect(arguments.has("B")).andReturn(false);
    expect(arguments.has("C")).andReturn(true);
    expect(arguments.get("C")).andReturn(cNode);
    expect(routeCommandHelper.getRouteClass("C")).andReturn(AcrrRoute.class);
    
    replayAll();
    
    
    RouteCommand result = (RouteCommand)classUnderTest.parseRouteCommand(RouteCommand.UPDATE, arguments);
    
    verifyAll();
    assertEquals(RouteCommand.UPDATE, result.getOperation());
    assertEquals("C", result.getRoute().getName());
  }

  @Test
  public void parseRouteCommand_REMOVE() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    JsonNode nameNode = createMock(JsonNode.class);
    
    List<String> types = Arrays.asList(new String[] {"A"});
    expect(arguments.asText()).andReturn("X");
    expect(routeCommandHelper.getRouteTypes()).andReturn(types);
    expect(arguments.has("A")).andReturn(false);
    expect(arguments.has("name")).andReturn(true);
    expect(arguments.get("name")).andReturn(nameNode);
    expect(nameNode.asText()).andReturn("thename");
    
    replayAll();
    
    
    RouteCommand result = (RouteCommand)classUnderTest.parseRouteCommand(RouteCommand.REMOVE, arguments);
    
    verifyAll();
    assertEquals(RouteCommand.REMOVE, result.getOperation());
    assertSame("thename", result.getRoute().getName());
  }
  
  @Test
  public void parseRouteCommand_GET() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    JsonNode nameNode = createMock(JsonNode.class);
    
    List<String> types = Arrays.asList(new String[] {"A"});
    expect(arguments.asText()).andReturn("X");
    expect(routeCommandHelper.getRouteTypes()).andReturn(types);
    expect(arguments.has("A")).andReturn(false);
    expect(arguments.has("name")).andReturn(true);
    expect(arguments.get("name")).andReturn(nameNode);
    expect(nameNode.asText()).andReturn("thename");
    
    replayAll();
    
    
    RouteCommand result = (RouteCommand)classUnderTest.parseRouteCommand(RouteCommand.GET, arguments);
    
    verifyAll();
    assertEquals(RouteCommand.GET, result.getOperation());
    assertSame("thename", result.getRoute().getName());
  }
  
  @Test
  public void parseRouteCommand_LIST() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    JsonNode activeNode = createMock(JsonNode.class);
    ArrayNode typesNode = jsonMapper.createArrayNode();
    typesNode.add("A");

    expect(arguments.has("types")).andReturn(true);
    expect(arguments.get("types")).andReturn(typesNode);

    expect(arguments.has("active")).andReturn(true);
    expect(arguments.get("active")).andReturn(activeNode);
    expect(activeNode.asBoolean()).andReturn(true);
    
    replayAll();
    
    
    RouteCommand result = (RouteCommand)classUnderTest.parseRouteCommand(RouteCommand.LIST, arguments);
    
    verifyAll();
    assertEquals("list_routes", result.getOperation());
    assertEquals(1, result.getListRoutesTypes().size());
    assertEquals("A", result.getListRoutesTypes().get(0));
    assertEquals(true, result.useActiveFilter());
    assertEquals(true, result.isActiveFilter());
  }

  @Test
  public void parseRouteCommand_LIST_TYPES() throws Exception {
    
    replayAll();
    
    RouteCommand result = (RouteCommand)classUnderTest.parseRouteCommand(RouteCommand.LIST_TYPES, null);
    
    verifyAll();
    assertEquals("list_route_types", result.getOperation());
  }
  
  @Test
  public void parseRouteCommand_CREATE_ROUTE_TEMPLATE() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    JsonNode routeTypeNode = createMock(JsonNode.class);

    expect(arguments.has("route-type")).andReturn(true);
    expect(arguments.get("route-type")).andReturn(routeTypeNode);
    expect(routeTypeNode.asText()).andReturn("acrr-route");
    
    replayAll();
    
    RouteCommand result = (RouteCommand)classUnderTest.parseRouteCommand(RouteCommand.CREATE_ROUTE_TEMPLATE, arguments);
    
    verifyAll();
    assertEquals(RouteCommand.CREATE_ROUTE_TEMPLATE, result.getOperation());
    assertEquals("acrr-route", result.getTemplateRouteType());
  }
  
  @Test
  public void parseScheduleCommand_ADD_withFixture() throws Exception {
    File f = new File(this.getClass().getResource(JSON_SCHEDULE_FIXTURE).getFile());
    
    ScheduleCommand result = (ScheduleCommand)classUnderTest.parseScheduleCommand(ScheduleCommand.ADD, jsonMapper.readTree(new String(Files.readAllBytes(f.toPath()))));
    
    assertEquals("add_schedule", result.getOperation());
    assertEquals(1, result.getEntry().getId());
    assertEquals("0 * * * * ?", result.getEntry().getExpression());
    assertEquals("trimcount", result.getEntry().getName());
  }
  
  @Test
  public void parseScheduleCommand_ADD() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    CronEntry entry = new CronEntry(1, "1 * * * * *", "theroute");
    JsonNode scheduledNode = jsonMapper.valueToTree(entry);
    
    expect(arguments.get("schedule")).andReturn(scheduledNode);
    
    replayAll();
    
    ScheduleCommand result = (ScheduleCommand)classUnderTest.parseScheduleCommand(ScheduleCommand.ADD, arguments);
    
    verifyAll();
    assertEquals("add_schedule", result.getOperation());
    assertEquals("1 * * * * *", result.getEntry().getExpression());
    assertEquals(1, result.getEntry().getId());
    assertEquals("theroute", result.getEntry().getName());
  }
  
  @Test
  public void parseScheduleCommand_UPDATE() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    CronEntry entry = new CronEntry(1, "1 * * * * *", "theroute");
    JsonNode scheduledNode = jsonMapper.valueToTree(entry);
    
    expect(arguments.get("schedule")).andReturn(scheduledNode);
    
    replayAll();
    
    ScheduleCommand result = (ScheduleCommand)classUnderTest.parseScheduleCommand(ScheduleCommand.UPDATE, arguments);
    
    verifyAll();
    assertEquals("update_schedule", result.getOperation());
    assertEquals("1 * * * * *", result.getEntry().getExpression());
    assertEquals(1, result.getEntry().getId());
    assertEquals("theroute", result.getEntry().getName());
  }

  @Test
  public void parseScheduleCommand_UPDATE_badIdentifier() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    CronEntry entry = new CronEntry(0, "1 * * * * *", "theroute");
    JsonNode scheduledNode = jsonMapper.valueToTree(entry);
    
    expect(arguments.get("schedule")).andReturn(scheduledNode);
    
    replayAll();
    
    try {
      classUnderTest.parseScheduleCommand(ScheduleCommand.UPDATE, arguments);
      fail("Expected AdministratorException");
    } catch (AdministratorException e) {
      // pass
    }
    
    verifyAll();
  }

  @Test
  public void parseScheduleCommand_GET() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    CronEntry entry = new CronEntry(1, "1 * * * * *", "theroute");
    JsonNode scheduledNode = jsonMapper.valueToTree(entry);
    
    expect(arguments.get("schedule")).andReturn(scheduledNode);
    
    replayAll();
    
    ScheduleCommand result = (ScheduleCommand)classUnderTest.parseScheduleCommand(ScheduleCommand.GET, arguments);
    
    verifyAll();
    assertEquals("get_schedule", result.getOperation());
    assertEquals(1, result.getEntry().getId());
    assertEquals("theroute", result.getEntry().getName());
  }

  @Test
  public void parseScheduleCommand_GET_with_args() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    JsonNode nameNode = createMock(JsonNode.class);
    JsonNode identifierNode = createMock(JsonNode.class);
    
    expect(arguments.get("schedule")).andReturn(null);
    expect(arguments.has("identifier")).andReturn(true);
    expect(arguments.get("identifier")).andReturn(identifierNode);
    expect(identifierNode.asInt()).andReturn(2);
    expect(arguments.has("route-name")).andReturn(true);
    expect(arguments.get("route-name")).andReturn(nameNode);
    expect(nameNode.asText()).andReturn("theroute");
    
    replayAll();
    
    ScheduleCommand result = (ScheduleCommand)classUnderTest.parseScheduleCommand(ScheduleCommand.GET, arguments);
    
    verifyAll();
    assertEquals("get_schedule", result.getOperation());
    assertEquals(2, result.getEntry().getId());
    assertEquals("theroute", result.getEntry().getName());
  }

  @Test
  public void parseScheduleCommand_REMOVE() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    CronEntry entry = new CronEntry(1, "1 * * * * *", "theroute");
    JsonNode scheduledNode = jsonMapper.valueToTree(entry);
    
    expect(arguments.get("schedule")).andReturn(scheduledNode);
    
    replayAll();
    
    ScheduleCommand result = (ScheduleCommand)classUnderTest.parseScheduleCommand(ScheduleCommand.REMOVE, arguments);
    
    verifyAll();
    assertEquals("remove_schedule", result.getOperation());
    assertEquals(1, result.getEntry().getId());
    assertEquals("theroute", result.getEntry().getName());
  }

  @Test
  public void parseScheduleCommand_REMOVE_with_args() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    JsonNode nameNode = createMock(JsonNode.class);
    JsonNode identifierNode = createMock(JsonNode.class);
    
    expect(arguments.get("schedule")).andReturn(null);
    expect(arguments.has("identifier")).andReturn(true);
    expect(arguments.get("identifier")).andReturn(identifierNode);
    expect(identifierNode.asInt()).andReturn(2);
    expect(arguments.has("route-name")).andReturn(true);
    expect(arguments.get("route-name")).andReturn(nameNode);
    expect(nameNode.asText()).andReturn("theroute");
    
    replayAll();
    
    ScheduleCommand result = (ScheduleCommand)classUnderTest.parseScheduleCommand(ScheduleCommand.REMOVE, arguments);
    
    verifyAll();
    assertEquals("remove_schedule", result.getOperation());
    assertEquals(2, result.getEntry().getId());
    assertEquals("theroute", result.getEntry().getName());
  }
  
  @Test
  public void parseScheduleCommand_LIST() throws Exception {
    
    replayAll();
    
    ScheduleCommand result = (ScheduleCommand)classUnderTest.parseScheduleCommand(ScheduleCommand.LIST, null);
    
    verifyAll();
    assertEquals("list_schedule", result.getOperation());
  }

  @Test
  public void parseScheduleCommand_CHANGE_PASSWORD_withFixture() throws Exception {
    File f = new File(this.getClass().getResource(JSON_USER_FIXTURE).getFile());
    
    UserCommand result = (UserCommand)classUnderTest.parseUserCommand(UserCommand.CHANGE_PASSWORD, jsonMapper.readTree(new String(Files.readAllBytes(f.toPath()))));
    
    assertEquals("change_password", result.getOperation());
    assertEquals("kalle", result.getUser().getName());
    assertEquals("<secret>", result.getUser().getPassword());
    assertEquals("<new secret>", result.getUser().getNewpassword());
  }
  
  @Test
  public void parseUserCommand_CHANGE_PASSWORD() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    User user = new User("thename", User.ROLE_ADMIN);
    user.setPassword("oldpassword");
    user.setNewpassword("newpassword");
    JsonNode userNode = jsonMapper.valueToTree(user);
    
    expect(arguments.get("user")).andReturn(userNode);
    
    replayAll();
    
    UserCommand result = (UserCommand)classUnderTest.parseUserCommand(UserCommand.CHANGE_PASSWORD, arguments);
    
    verifyAll();
    assertEquals("change_password", result.getOperation());
    assertEquals("thename", result.getUser().getName());
    assertEquals("oldpassword", result.getUser().getPassword());
    assertEquals("newpassword", result.getUser().getNewpassword());
  }
  
  @Test
  public void parseUserCommand_LIST() throws Exception {
    
    replayAll();
    
    UserCommand result = (UserCommand)classUnderTest.parseUserCommand(UserCommand.LIST, null);
    
    verifyAll();
    assertEquals("list_users", result.getOperation());
  }
  
  @Test
  public void parseUserCommand_LIST_with_roles() throws Exception {
    JsonNode arguments = createMock(JsonNode.class);
    JsonNode typeNode = createMock(JsonNode.class);
    
    expect(arguments.has("role")).andReturn(true);
    expect(arguments.get("role")).andReturn(typeNode);
    expect(typeNode.asText()).andReturn("thetype");
    
    replayAll();
    
    UserCommand result = (UserCommand)classUnderTest.parseUserCommand(UserCommand.LIST, arguments);
    
    verifyAll();
    assertEquals("list_users", result.getOperation());
    assertEquals("thetype", result.getUser().getRole());
  }
}
