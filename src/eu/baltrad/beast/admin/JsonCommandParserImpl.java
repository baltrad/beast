/* --------------------------------------------------------------------
Copyright (C) 2009-2021 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;

import eu.baltrad.beast.admin.command.AdaptorCommand;
import eu.baltrad.beast.admin.command.AnomalyDetectorCommand;
import eu.baltrad.beast.admin.command.HelpCommand;
import eu.baltrad.beast.admin.command.RouteCommand;
import eu.baltrad.beast.admin.command.ScheduleCommand;
import eu.baltrad.beast.admin.command.UserCommand;
import eu.baltrad.beast.admin.objects.Adaptor;
import eu.baltrad.beast.admin.objects.User;
import eu.baltrad.beast.admin.objects.routes.BasicRoute;
import eu.baltrad.beast.admin.objects.routes.Route;
import eu.baltrad.beast.admin.objects.routes.Site2DRoute;
import eu.baltrad.beast.qc.AnomalyDetector;
import eu.baltrad.beast.scheduler.CronEntry;

/**
 * The implementation of all commands that we currently can parse
 * @author anders
 */
public class JsonCommandParserImpl implements JsonCommandParser {
  /**
   * The object mapper
   */
  private ObjectMapper jsonMapper = new ObjectMapper();
  
  /**
   * Logger
   */
  private static Logger logger = LogManager.getLogger(JsonCommandParserImpl.class);
  
  /**
   * The route command helper
   */
  private RouteCommandHelper routeCommandHelper = null;

  /**
   * Add all supported commands here so that we can provide help about them.
   */
  private final static String AVAILABLE_COMMANDS[] = {
    /* HELP */
    HelpCommand.HELP,
    
    /* Adaptor */
    AdaptorCommand.ADD,
    AdaptorCommand.UPDATE,
    AdaptorCommand.GET,
    AdaptorCommand.REMOVE,
    AdaptorCommand.LIST,
    
    /* Anomaly detector */
    AnomalyDetectorCommand.ADD,
    AnomalyDetectorCommand.UPDATE,
    AnomalyDetectorCommand.GET,
    AnomalyDetectorCommand.REMOVE,
    AnomalyDetectorCommand.LIST,

    /* Route */
    RouteCommand.CREATE_ROUTE_TEMPLATE,
    RouteCommand.ADD,
    RouteCommand.UPDATE,
    RouteCommand.GET,
    RouteCommand.REMOVE,
    RouteCommand.LIST,
    RouteCommand.LIST_TYPES,
    
    /* Schedule */
    ScheduleCommand.ADD,
    ScheduleCommand.UPDATE,
    ScheduleCommand.GET,
    ScheduleCommand.REMOVE,
    ScheduleCommand.LIST,
    
    /* User admin commands */
    UserCommand.CHANGE_PASSWORD,
    UserCommand.LIST
  };
  
  /**
   * Parses an input stream into a command object
   * @return the parsed command or null
   * @throws {@link AdministratorException} when a problem occurs.
   */
  @Override
  public Command parse(InputStream inputStream) {
    logger.info("manage(inputStream)");
    try {
      return parse(IOUtils.toString(inputStream, StandardCharsets.UTF_8.name()));
    } catch (IOException e) {
      throw new AdministratorException(e);
    }
  }

  @Override
  public Command parse(String s) {
    logger.info("manage(String)");
    try {
      JsonNode node = jsonMapper.readTree(s);
      Command command = parse(node);
      if (command != null) {
        command.setRawMessage(s);
      }
      return command;
    } catch (JsonProcessingException e) {
      throw new AdministratorException(e);
    } catch (IOException e) {
      throw new AdministratorException(e);
    }
  }

  /**
   * Processes a JsonNode according to administrator command conventions.
   * @param node the json node
   * @return the associated command
   * @throws {@link AdministratorException} when a problem occurs.
   */
  public Command parse(JsonNode node) {
    Command result = null;
    
    logger.info("parse(node): " + node.asText());
    
    if (node.has("command")) {
      String command = node.get("command").asText().toLowerCase();
      logger.info("Command is: " + command);
      JsonNode arguments = null;
      if (node.has("arguments")) {
        arguments = node.get("arguments");
      }
      result = parseCommand(command, arguments);
    } else {
      logger.info("Could not identify any command");
    }
    return result;
  }
  
  /**
   * Takes care of and maps the command to the appropriate method
   * @param command the command
   * @param arguments the arguments as a json node if any
   * @return the command
   */
  public Command parseCommand(String command, JsonNode arguments) {
    if (command.equals(HelpCommand.HELP)) {
      return parseHelpCommand(command, arguments);
    } else if (command.equals(AdaptorCommand.ADD) || command.equals(AdaptorCommand.UPDATE) || 
        command.equals(AdaptorCommand.REMOVE) || command.equals(AdaptorCommand.GET) || 
        command.equals(AdaptorCommand.LIST)) {
      return parseAdaptorCommand(command, arguments);
    } else if ( command.equals(AnomalyDetectorCommand.ADD) ||
        command.equals(AnomalyDetectorCommand.UPDATE) || command.equals(AnomalyDetectorCommand.GET) ||
        command.equals(AnomalyDetectorCommand.REMOVE) || command.equals(AnomalyDetectorCommand.LIST)) {
      return parseAnomalyDetectorCommand(command, arguments);
    } else if (command.equals(RouteCommand.ADD) ||
        command.equals(RouteCommand.UPDATE) || command.equals(RouteCommand.GET) ||
        command.equals(RouteCommand.REMOVE) || command.equals(RouteCommand.LIST) ||
        command.equals(RouteCommand.LIST_TYPES) || command.equals(RouteCommand.CREATE_ROUTE_TEMPLATE)) {
      return parseRouteCommand(command, arguments);
    } else if (command.equals(ScheduleCommand.ADD) ||
        command.equals(ScheduleCommand.UPDATE) || command.equals(ScheduleCommand.GET) ||
        command.equals(ScheduleCommand.REMOVE) || command.equals(ScheduleCommand.LIST)) {
      return parseScheduleCommand(command, arguments);
    } else if (command.equals(UserCommand.CHANGE_PASSWORD) ||
        command.equals(UserCommand.LIST)) {
      return parseUserCommand(command, arguments);
    } else {
      logger.info("Command not supported: " + command);
      return null;
    }
  }
  
  /**
   * Provides the help command to get information about available commands
   * @param operation the operation
   * @param arguments the arguments
   * @return the command
   */
  public Command parseHelpCommand(String operation, JsonNode arguments) {
    HelpCommand command = new HelpCommand(operation);
    if (arguments != null && arguments.has("command")) {
      command.setCommand(arguments.get("command").asText());
    }
    return command;
  }

  /**
   * Manages an adaptor, either ADD, UPDATE or REMOVE
   * @param operation the operation
   * @param node the json node containing an adaptor definition
   * @throws {@link AdministratorException} when a problem occurs.
   */
  public Command parseAdaptorCommand(String operation, JsonNode node) {
    AdaptorCommand result = null;
    
    if (operation.equals(AdaptorCommand.ADD) || operation.equals(AdaptorCommand.UPDATE)) {
      try {
        result = new AdaptorCommand(operation);
        result.setAdaptor(jsonMapper.readValue(node.get("adaptor"), Adaptor.class));
        return result;
      } catch (Exception e) {
        throw new AdministratorException(e);
      }
    } else if (operation.equals(AdaptorCommand.REMOVE) || operation.equals(AdaptorCommand.GET)) {
      result = new AdaptorCommand(operation);
      String adaptorName = null;
      if (node.has("adaptor")) {
        if (node.get("adaptor").has("name")) {
          adaptorName = node.get("adaptor").get("name").asText();
        }
      } else if (node.has("name")) {
        adaptorName = node.get("name").asText();
      }
      if (adaptorName == null) {
        throw new AdministratorException("Could not find adaptor with name or name in the arguments");
      }
      result.setAdaptor(new Adaptor(adaptorName));
      return result;
    } else if (operation.equals(AdaptorCommand.LIST)) {
      return new AdaptorCommand(operation);
    }
    return null;
  }

  /**
   * Manages an anomaly detector
   * @param operation the operation
   * @param node the anomaly detector
   * @return the command
   * @throws {@link AdministratorException} when a problem occurs.
   */
  public Command parseAnomalyDetectorCommand(String operation, JsonNode node) {
    if (operation.equals(AnomalyDetectorCommand.ADD) || operation.equals(AnomalyDetectorCommand.UPDATE) ||
        operation.equals(AnomalyDetectorCommand.GET) || operation.equals(AnomalyDetectorCommand.REMOVE)) {
      JsonNode detectorNode = node.get("anomaly-detector");
      if (operation.equals(AnomalyDetectorCommand.ADD) || operation.equals(AnomalyDetectorCommand.UPDATE)) {
        String name = detectorNode.get("name").asText();
        String description = detectorNode.get("description").asText();
        return new AnomalyDetectorCommand(operation, name, description);
      } else if (operation.equals(AnomalyDetectorCommand.GET) || operation.equals(AnomalyDetectorCommand.REMOVE)) {
        String name = null;
        if (detectorNode == null) {
          detectorNode = node;
        }
        name = detectorNode.get("name").asText();
        return new AnomalyDetectorCommand(operation, name);
      }
    } else if (operation.equals(AnomalyDetectorCommand.LIST)) {
      return new AnomalyDetectorCommand(operation);
    }
    return null;
  }
  
  /**
   * Manages an anomaly detector
   * @param operation the operation
   * @param node the anomaly detector
   * @return the command
   * @throws {@link AdministratorException} when a problem occurs.
   */
  @SuppressWarnings("unchecked")
  public Command parseRouteCommand(String operation, JsonNode node) {
    if (operation.equals(RouteCommand.ADD) || operation.equals(RouteCommand.UPDATE) ||
        operation.equals(RouteCommand.GET) || operation.equals(RouteCommand.REMOVE)) {
      logger.info("Parsing route object: " + node.asText());
      try {
        RouteCommand routeCommand = new RouteCommand(operation);
        for (String key : routeCommandHelper.getRouteTypes()) {
          if (node.has(key)) {
            routeCommand.setRoute((Route)jsonMapper.readValue(node.get(key), routeCommandHelper.getRouteClass(key)));
            break;
          }
        }
        if (routeCommand.getRoute() == null) {
          if (node.has("name")) {
            routeCommand.setRoute(new BasicRoute(node.get("name").asText()));
          } else {
            throw new AdministratorException("No object to handle");
          }
        }
        if (routeCommand.getRoute() != null) {
          logger.info("Route command name: " + routeCommand.getRoute().getName());
        } else {
          logger.info("Route command got no name");
        }
        return routeCommand;
      } catch (JsonParseException e) {
        throw new AdministratorException("Failed to parse route", e);
      } catch (JsonMappingException e) {
        throw new AdministratorException("Failed to parse route", e);
      } catch (IOException e) {
        throw new AdministratorException("Failed to parse route", e);
      }
    } else if (operation.equals(RouteCommand.LIST)) {
      try {
        RouteCommand routeCommand = new RouteCommand(operation);
        if (node.has("types")) {
          List<String> data = jsonMapper.readValue(node.get("types"), new TypeReference<List<String>>() {});
          routeCommand.setListRoutesTypes(data);
        }
        if (node.has("active")) {
          routeCommand.setUseActiveFilter(true);
          routeCommand.setActiveFilter(node.get("active").asBoolean());
        }
        return routeCommand;
      } catch (Exception e) {
        throw new AdministratorException("Failed to parse list command", e);
      }
    } else if (operation.equals(RouteCommand.LIST_TYPES)) {
      RouteCommand routeCommand = new RouteCommand(operation);
      return routeCommand;
    } else if (operation.equals(RouteCommand.CREATE_ROUTE_TEMPLATE)) {
      RouteCommand routeCommand = new RouteCommand(operation);
      if (node.has("route-type")) {
        routeCommand.setTemplateRouteType(node.get("route-type").asText());
        return routeCommand;
      } else {
        throw new AdministratorException("No object to handle");
      }
    }
    return null;
  }
  
  /**
   * Parses a schedule command
   * @param operation the operation
   * @param node the node
   * @return the parsed scheduled command
   * @throws {@link AdministratorException} when a problem occurs.
   */
  public Command parseScheduleCommand(String operation, JsonNode node) {
    ScheduleCommand command = null;
    if (operation.equals(ScheduleCommand.ADD) || operation.equals(ScheduleCommand.UPDATE)) {
      JsonNode scheduleNode = node.get("schedule");
      int identifier = 0;
      String routeName = null;
      String expression = null;
      if (scheduleNode == null) {
        throw new AdministratorException("Add & Update of schedule requires schedule object");
      }
      if (operation.equals(ScheduleCommand.UPDATE) && (!scheduleNode.has("identifier") || scheduleNode.get("identifier").asInt() <= 0)) {
        throw new AdministratorException("Update of schedule requires identifier");
      }
      if (!scheduleNode.has("route-name") || !scheduleNode.has("expression")) {
        throw new AdministratorException("Add or update of schedule requires route-name and expression");
      }
      if (scheduleNode.has("identifier")) {
        identifier = scheduleNode.get("identifier").asInt();
      }
      routeName = scheduleNode.get("route-name").asText();
      expression = scheduleNode.get("expression").asText();
      
      command = new ScheduleCommand(operation);
      command.setExpression(expression);
      command.setIdentfier(identifier);
      command.setRouteName(routeName);
      
    } else if (operation.equals(ScheduleCommand.GET) || operation.equals(ScheduleCommand.REMOVE)) {
      int identifier = 0;
      String routeName = null;
      JsonNode tmpNode = node.get("schedule");
      if (tmpNode == null) {
        tmpNode = node;
      }
      
      if (tmpNode.has("identifier")) {
        identifier = tmpNode.get("identifier").asInt();
      }
      if (tmpNode.has("route-name")) {
        routeName = tmpNode.get("route-name").asText();
      }
      if (operation.equals(ScheduleCommand.REMOVE) && identifier <= 0) {
        throw new AdministratorException("Removal of schedule requires at least identifier");
      }
      command = new ScheduleCommand(operation);
      command.setIdentfier(identifier);
      command.setRouteName(routeName);
    } else if (operation.equals(ScheduleCommand.LIST)) {
      command = new ScheduleCommand(operation);
    }
    
    return command;
  }
  
  /**
   * Parses a user manipulation command
   * @param operation the operation
   * @param node the arguments
   * @return the parsed user command
   * @throws {@link AdministratorException} when a problem occurs.
   */
  public Command parseUserCommand(String operation, JsonNode arguments) {
    UserCommand command = null;
    if (operation.equals(UserCommand.CHANGE_PASSWORD)) {
      JsonNode userNode = arguments.get("user");
      try {
        if (userNode != null) {
          command = new UserCommand(operation, jsonMapper.readValue(userNode, User.class));
        }
      } catch (Exception e) {
        throw new AdministratorException(e);
      }
    } else if (operation.equals(UserCommand.LIST)) {
      logger.info("Operation: " + operation);
      User user = new User();
      if (arguments != null && arguments.has("role")) {
        user.setRole(arguments.get("role").asText());
      }
      command = new UserCommand(operation, user);
    }
    return command;
  }
  
  /**
   * The route command helper
   * @param helper
   */
  @Autowired
  public void setRouteCommandHelper(RouteCommandHelper helper) {
    this.routeCommandHelper = helper;
  }
  
  /**
   * @return the route command helper
   */
  public RouteCommandHelper getRouteCommandHelper() {
    return this.routeCommandHelper;
  }

  /**
   * @return the help for the command parsing
   */
  @Override
  public String getHelp() {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      JsonGenerator jsonGenerator = new JsonFactory().createJsonGenerator(baos);
      jsonGenerator.writeStartObject();
      jsonGenerator.writeObjectFieldStart("help");
      jsonGenerator.writeStringField("description", 
          "All commands have the format {\"command\":\"<command>\"[,\"arguments\":{...})\n" +
          "The arguments are then depending on the command and will be more explained when issuing the command help <command>\n" +
          "All supported commands are as of now described in the field \"commands\"");
      jsonGenerator.writeArrayFieldStart("commands");
      for (String s : AVAILABLE_COMMANDS) {
        jsonGenerator.writeString(s);
      }
      jsonGenerator.writeEndArray();
      jsonGenerator.writeEndObject();
      jsonGenerator.writeEndObject();
      jsonGenerator.close();
      return new String(baos.toByteArray());
    } catch (Exception e) {
      throw new AdministratorException(e);
    }
  }

  /**
   * Generates help text about a method. If method == null it will be the same as calling {@link #getHelp()}.
   * @param method - the method for which help text should be produced
   * @return the help text
   */
  @Override
  public String getHelp(String method) {
    if (method == null) {
      return getHelp();
    }
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      JsonGenerator jsonGenerator = new JsonFactory().createJsonGenerator(baos);
      jsonGenerator.writeStartObject();
      jsonGenerator.writeObjectFieldStart("help");
      jsonGenerator.writeStringField("description", getMethodHelp(method));
      jsonGenerator.writeEndObject();
      jsonGenerator.writeEndObject();
      jsonGenerator.close();
      return new String(baos.toByteArray());
    } catch (Exception e) {
      throw new AdministratorException(e);
    }
  }
  
  /**
   * Returns help about the specified method
   * @param method - the method for which help should be generated
   * @return the help text
   */
  public String getMethodHelp(String method) throws Exception {
    StringBuffer buffer = new StringBuffer();

    if (method.equals(HelpCommand.HELP)) {
      return getHelp();
    } else if (method.equals(AdaptorCommand.ADD)) {
      buffer.append("Adds an adaptor to the system. Format is:\n");
      buffer.append(createFullCommand(method, new Adaptor("RAVE","XMLRPC","http://localhost:8085/RAVE",5000)));
    } else if (method.equals(AdaptorCommand.UPDATE)) {
      buffer.append("Updates an adaptor in the system. Format of full command is\n");
      buffer.append(createFullCommand(method, new Adaptor("RAVE","XMLRPC","http://localhost:8085/RAVE",5000)));
    } else if (method.equals(AdaptorCommand.REMOVE)) {
      buffer.append("Removes an adaptor from the system. Format of full command is\n");
      buffer.append(createFullCommand(method, new Adaptor("RAVE",null,null,0)));
      buffer.append("\n\nIt is also possible to use the abbreviated form and only specify adaptor-name like this\n\n");
      buffer.append(createSimpleCommand(method, new String[] {"adaptor-name","RAVE"}));
    } else if (method.equals(AdaptorCommand.GET)) {
      buffer.append("Returns information about an adaptor in the system. Format of full command is\n");
      buffer.append(createFullCommand(method, new Adaptor("RAVE",null,null,0)));
      buffer.append("\n\nIt is also possible to use the abbreviated form and only specify adaptor-name like this\n\n");
      buffer.append(createSimpleCommand(method, new String[] {"adaptor-name","RAVE"}));
    } else if (method.equals(AdaptorCommand.LIST)) {
      buffer.append("Returns a list of currently registered adaptors\n");
    } else if (method.equals(AnomalyDetectorCommand.ADD)) {
      buffer.append("Adds an anomaly detector to the system. Format is:\n");
      buffer.append(createFullCommand(method, new AnomalyDetector("qcfunction", "This is the name of the QC function")));
    } else if (method.equals(AnomalyDetectorCommand.UPDATE)) {
      buffer.append("Updates an anomaly detector in the system. Format is:\n");
      buffer.append(createFullCommand(method, new AnomalyDetector("qcfunction", "This is the name of the QC function")));
    } else if (method.equals(AnomalyDetectorCommand.GET)) {
      buffer.append("Returns information about an anomaly detector in the system. Format is:\n");
      buffer.append(createFullCommand(method, new AnomalyDetector("qcfunction", null)));
      buffer.append("\n\nIt is also possible to use the abbreviated form and only specify name like this\n\n");
      buffer.append(createSimpleCommand(method, new String[] {"name","qcfunction"}));
    } else if (method.equals(AnomalyDetectorCommand.REMOVE)) {
      buffer.append("Remove an anomaly detector from the system. Format is:\n");
      buffer.append(createFullCommand(method, new AnomalyDetector("qcfunction", null)));
      buffer.append("\n\nIt is also possible to use the abbreviated form and only specify name like this\n\n");
      buffer.append(createSimpleCommand(method, new String[] {"name","qcfunction"}));
    } else if (method.equals(AnomalyDetectorCommand.LIST)) {
      buffer.append("Returns a list of currently registered anomaly detectors\n");
    } else if (method.equals(RouteCommand.CREATE_ROUTE_TEMPLATE)) {
      buffer.append("Used to create a template that can be used for add and update of a route of specified type. Which is available by using list_types command\n");
      buffer.append(createSimpleCommand(method, new String[] {"route-type", "site2d-route"}));
    } else if (method.equals(RouteCommand.ADD) || method.equals(RouteCommand.UPDATE)) {
      Site2DRoute route = new Site2DRoute();
      route.setName("MySite2dRoute");
      route.setPcsId("swegmaps_2000");
      route.setAuthor("Author");
      route.setDetectors(Arrays.asList(new String[] {"bropo","beamb"}));
      route.setMethod("PPI");
      route.setXscale(2000.0);
      route.setYscale(2000.0);
      route.setSources(Arrays.asList(new String[] {"sella", "seosu"}));
      
      if (method.equals(RouteCommand.ADD)) { 
        buffer.append("Adds the provided route. There are a number of different route types that can be specified. Check " + RouteCommand.LIST_TYPES + " for available types. Format is: \n");
      } else {
        buffer.append("Updates the provided route. There are a number of different route types that can be specified. Check " + RouteCommand.LIST_TYPES + " for available types.\n");
      }
      buffer.append(createFullCommand(method, route));
    } else if (method.equals(RouteCommand.GET)) {
      buffer.append("Returns the specified route. Format is:\n");
      buffer.append(createFullCommand(method, new Site2DRoute("theroute")));
      buffer.append("\n\nIt is also possible to use the abbreviated form and only specify name like this\n\n");
      buffer.append(createSimpleCommand(method, new String[] {"name","theroute"}));
    } else if (method.equals(RouteCommand.GET)) {
      buffer.append("Removes the specified route. Format is:\n");
      buffer.append(createFullCommand(method, new Site2DRoute("theroute")));
      buffer.append("\n\nIt is also possible to use the abbreviated form and only specify name like this\n\n");
      buffer.append(createSimpleCommand(method, new String[] {"name","theroute"}));
    } else if (method.equals(RouteCommand.LIST)) {
      buffer.append("Lists the registered routes. Without arguments all registered routes are returned. It is also possible to specify type and/or active state.\n");
      buffer.append(createSimpleCommand(method, new Object[] {"types", new String[] {"site2d-route","composite-route"}, "active", true}));
    } else if (method.equals(RouteCommand.LIST_TYPES)) {
      buffer.append("Returns a list of possible route types.\n");
    } else if (method.equals(ScheduleCommand.ADD) || method.equals(ScheduleCommand.UPDATE)) {
      if (method.equals(ScheduleCommand.ADD)) {
        buffer.append("Adds a entry to the scheduler. Note, identifier is not needed here. Format is:\n");
      } else {
        buffer.append("Updates a scheduled entry. Note, identifier must be specified and > 0 here. Format is:\n");
      }
      buffer.append(createFullCommand(method, new CronEntry("<expression>", "<name of route>")));
      buffer.append("\n\n");
      buffer.append("Note, identifier is only needed when updating the schedule.\n");
      buffer.append("The expression follows the standard cron-syntax which is:\n\n");
      buffer.append("<second> <minute> <hour> <day of month> <month> <day of week> (<year>)\n");
      buffer.append("\n");
      buffer.append("field           allowed           special character\n");
      buffer.append("second          0 - 59            , - * / \n");
      buffer.append("minute          0 - 59            , - * / \n");
      buffer.append("hour            0 - 23            , - * / \n");
      buffer.append("day of month    0 - 31            , - * ? / L W \n");
      buffer.append("month           1 - 12            , - * ? / \n");
      buffer.append("day of week     1 - 7 or SUN-SAT  , - * ? / L # \n");
      buffer.append("year (optional) empty, 1970-2199  , - * ? / \n");
      buffer.append("\n");
      buffer.append("If you want to specify all allowed values you can use asterisk (*) which\n");
      buffer.append("will represent all values between first - last.\n");
      buffer.append("\n");
      buffer.append("It is also possible to define both ranges and lists and a combination of\n");
      buffer.append("both. Ranges are represented by a hyphen (-) and lists by a comma (,).\n");
      buffer.append("This means that you could define all minutes between 0 and 10 with 0-10\n");
      buffer.append("and hour 0 - 6 and 18-23 with 0-6,18-23 but remember to not use spaces\n");
      buffer.append("when defining a field since that will flow over to the next field.\n");
      buffer.append("\n");
      buffer.append("You can also define step values by using <range> or * / <value> to \n");
      buffer.append("indicate specifi"
          + "c values. So if you want to execute a cron every second hour\n");
      buffer.append("between 0 - 6 in the morning you could write 0-6/2 or if you want to specify every\n");
      buffer.append("second hour the whole day you define it with */2.\n");
      buffer.append("\n");
      buffer.append("L is defined as last which means that in day of month L would have the effect to specify\n");
      buffer.append("last day in the current month. You can also specify a specific day which means that\n");
      buffer.append("6L would represent last friday of the month.\n");
      buffer.append("\n");
      buffer.append("W represents weekdays (2-6) and can be used in conjunction with L which gives that LW\n");
      buffer.append("represents the last weekday of the month.\n");
      buffer.append("\n");
      buffer.append("# is another special character that represents the n-th y-th day in the month, which\n");
      buffer.append("gives that if you for example specify 6#3 it would mean the 3rd friday of the month.\n");
      buffer.append("\n");
      buffer.append("Cron entries can also have a seventh field that defines the year but that is optional.\n");
      buffer.append("\n");
      buffer.append("It is not possible to specify both day of week and day of month simultaneously so one\n");
      buffer.append("of those two entries must be specified with a ? at any time.\n");
    } else if (method.equals(ScheduleCommand.GET)) {
      buffer.append("Returns one or several scheduled entries depending on filter. If identifier is specified,\n");
      buffer.append("only affected entry will be returned. If the route-name is specified, all entries triggering\n");
      buffer.append("specified route-name will be returned");
      buffer.append("Full command is: \n");
      buffer.append(createFullCommand(method, new CronEntry(1, null, "<name of route>")));
      buffer.append("\n\nIt is also possible to use the abbreviated form and only specify route-name or identifier like this.\n");
      buffer.append("Identifier will always take preceedence over route-name.\n\n");
      buffer.append(createSimpleCommand(method, new Object[] {"route-name","routename", "identifier", 1}));
    } else if (method.equals(ScheduleCommand.REMOVE)) {
      buffer.append("Removes the specified schedule entry,\n");
      buffer.append("Full command is: \n");
      buffer.append(createFullCommand(method, new CronEntry(1, null, null)));
      buffer.append("\n\nIt is also possible to use the abbreviated form and only specify identifier like this\n\n");
      buffer.append(createSimpleCommand(method, new Object[] {"identifier", 1}));
    } else if (method.equals(ScheduleCommand.LIST)) {
      buffer.append("Returns all scheduled entries.\n");
    } else if (method.equals(UserCommand.CHANGE_PASSWORD)) {
      User user = new User();
      user.setName("<username>");
      user.setPassword("<password>");
      user.setNewpassword("<new password>");
      buffer.append("Changes the users password. Required attributes are password, new-password and name.\n");
      buffer.append("Full command is: \n");
      buffer.append(createFullCommand(method, user));
    } else if (method.equals(UserCommand.LIST)) {
      buffer.append("List all registered users of roles (ADMIN, OPERATOR or USER). Can also be specified as type: Format is:\n");
      buffer.append(createSimpleCommand(method, new Object[] {"role","<role>"}));
    }
    
    return buffer.toString();
  }
  
  /**
   * Creates a full command object for usage in the help text
   * @param method the method
   * @param object the object that should be jsonified
   * @return the json representation of the object
   * @throws Exception when an error occurs
   */
  public String createFullCommand(String method, Object object) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JsonGenerator jsonGenerator = new JsonFactory().createJsonGenerator(baos);
    ObjectMapper jMapper = new ObjectMapper();
    jMapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);
    jsonGenerator.setCodec(jMapper);
    jsonGenerator.useDefaultPrettyPrinter();
    jsonGenerator.writeStartObject();
    jsonGenerator.writeStringField("command", method);
    jsonGenerator.writeObjectField("arguments", object);
    jsonGenerator.writeEndObject();
    jsonGenerator.close();
    return new String(baos.toByteArray());
  }
  
  /**
   * Creates a json representation when the arguments are just fields and not complete objects.
   * @param method the method
   * @param objs an array of of N*2 items where N = name of field, N+1 = field value
   * @return the json representation of the object
   * @throws Exception when an error occurs
   */
  public String createSimpleCommand(String method, Object[] objs) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JsonGenerator jsonGenerator = new JsonFactory().createJsonGenerator(baos);
    jsonGenerator.useDefaultPrettyPrinter();
    jsonGenerator.writeStartObject();
    jsonGenerator.writeStringField("command", method);
    jsonGenerator.writeObjectFieldStart("arguments");
    for (int i = 0; i < objs.length; i+=2) {
      if (objs[i+1] instanceof String) {
        jsonGenerator.writeStringField((String)objs[i], (String)objs[i+1]);
      } else if (objs[i+1] instanceof Integer) {
        jsonGenerator.writeNumberField((String)objs[i], (Integer)objs[i+1]);
      } else if (objs[i+1] instanceof Double) {
        jsonGenerator.writeNumberField((String)objs[i], (Double)objs[i+1]);
      } else if (objs[i+1] instanceof Boolean) {
        jsonGenerator.writeBooleanField((String)objs[i], (Boolean)objs[i+1]);
      } else if (objs[i+1] instanceof String[]) {
        jsonGenerator.writeArrayFieldStart((String)objs[i]);
        String[] arr = (String[])objs[i+1];
        for (String s: arr) {
          jsonGenerator.writeString(s);
        }
        jsonGenerator.writeEndArray();
      }
    }
    jsonGenerator.writeEndObject();
    jsonGenerator.writeEndObject();
    jsonGenerator.close();
    return new String(baos.toByteArray());
  }
  
  /**
   * Used for testing
   * @param jsonMapper the mapper
   */
  void setObjectMapper(ObjectMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }
}
