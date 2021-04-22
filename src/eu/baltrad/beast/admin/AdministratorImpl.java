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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.beans.factory.annotation.Autowired;

import eu.baltrad.beast.adaptor.IAdaptor;
import eu.baltrad.beast.adaptor.IBltAdaptorManager;
import eu.baltrad.beast.adaptor.xmlrpc.XmlRpcAdaptorConfiguration;
import eu.baltrad.beast.admin.command.AdaptorCommand;
import eu.baltrad.beast.admin.command.AnomalyDetectorCommand;
import eu.baltrad.beast.admin.command.HelpCommand;
import eu.baltrad.beast.admin.command.RouteCommand;
import eu.baltrad.beast.admin.command.ScheduleCommand;
import eu.baltrad.beast.admin.command_response.CommandResponseJsonObject;
import eu.baltrad.beast.admin.command_response.CommandResponseStatus;
import eu.baltrad.beast.admin.objects.Adaptor;
import eu.baltrad.beast.admin.objects.routes.Route;
import eu.baltrad.beast.qc.AnomalyDetector;
import eu.baltrad.beast.qc.AnomalyException;
import eu.baltrad.beast.qc.IAnomalyDetectorManager;
import eu.baltrad.beast.router.IRouterManager;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.RuleException;
import eu.baltrad.beast.scheduler.CronEntry;
import eu.baltrad.beast.scheduler.IBeastScheduler;

/**
 * @author anders
 *
 */
public class AdministratorImpl implements Administrator {
  /**
   * The adaptor manager
   */
  private IBltAdaptorManager adaptorManager = null;

  /**
   * The anomaly detector manager
   */
  private IAnomalyDetectorManager anomalyDetectorManager = null;

  /**
   * The router manager
   */
  private IRouterManager routerManager = null;
  
  /**
   * The object mapper
   */
  private ObjectMapper objectMapper = null;

  /**
   * The route command helper
   */
  private RouteCommandHelper routeCommandHelper = null;
  
  /**
   * The beast scheduler
   */
  private IBeastScheduler scheduler = null;
  
  /**
   * The json generator
   */
  private JsonGenerator jsonGenerator = null;
  
  /**
   * The json command parser that provides the help information
   */
  private JsonCommandParser jsonCommandParser = null;
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(AdministratorImpl.class);
  
  /**
   * 
   */
  public AdministratorImpl() {
    objectMapper = new ObjectMapper();
    objectMapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);
  }
  
  /**
   * Manages all commands
   * @param command - the command
   * @returns a \ref {@link CommandResponse}
   * @throws AdministratorException on error
   */
  @Override
  public CommandResponse handle(Command command) {
    if (command != null && command.validate()) {
      if  (command instanceof HelpCommand) {
        return handleCommand((HelpCommand)command);
      } else if (command instanceof AdaptorCommand) {
        return handleCommand((AdaptorCommand)command);
      } else if (command instanceof AnomalyDetectorCommand) {
        return handleCommand((AnomalyDetectorCommand)command);
      } else if (command instanceof RouteCommand) {
        return handleCommand((RouteCommand)command);
      } else if (command instanceof ScheduleCommand) {
        return handleCommand((ScheduleCommand)command);
      }
    }
    return new CommandResponseStatus(false);
  }

  /**
   * Returns help information
   * @param command the help command
   * @return the help response
   */
  public CommandResponse handleCommand(HelpCommand command) {
    if (command.getOperation().equals(HelpCommand.HELP)) {
      return new CommandResponseJsonObject(jsonCommandParser.getHelp(command.getCommand()));
    }
    return new CommandResponseJsonObject(jsonCommandParser.getHelp());
  }
  
  /**
   * Handles an adaptor command
   * @param command the adaptor command
   * @return the command response
   */
  public CommandResponse handleCommand(AdaptorCommand command) {
    logger.info("handleCommand(\""+command.getOperation()+"\", node)");
    if (command.getOperation().equals(AdaptorCommand.ADD) ||
        command.getOperation().equals(AdaptorCommand.UPDATE)) {
      
      IAdaptor adaptor = getAdaptorManager().getAdaptor(command.getAdaptor().getName());
      XmlRpcAdaptorConfiguration conf = (XmlRpcAdaptorConfiguration)getAdaptorManager().createConfiguration(command.getAdaptor().getType(), command.getAdaptor().getName());
      conf.setURL(command.getAdaptor().getUri());
      conf.setTimeout(command.getAdaptor().getTimeout());
      
      if (adaptor == null) {
        logger.info("Registering adaptor: " + command.getAdaptor().getName() + ", URI: '" + command.getAdaptor().getUri() + "'");
        getAdaptorManager().register(conf);
      } else {
        logger.info("Reregistering adaptor: " + command.getAdaptor().getName() + ", URI: " + command.getAdaptor().getUri());
        getAdaptorManager().reregister(conf);
      }
      return new CommandResponseStatus(true);
    } else if (command.getOperation().equals(AdaptorCommand.REMOVE)) {
      if (getAdaptorManager().getAdaptorNames().contains(command.getAdaptor().getName())) {
        logger.info("Removing adaptor: " + command.getAdaptor().getName());
        getAdaptorManager().unregister(command.getAdaptor().getName());
      }
      return new CommandResponseStatus(true);
    } else if (command.getOperation().equals(AdaptorCommand.GET)) {
      if (getAdaptorManager().getAdaptorNames().contains(command.getAdaptor().getName())) {
        Adaptor adaptorc = new Adaptor();
        adaptorc.fromAdaptor(getAdaptorManager().getAdaptor(command.getAdaptor().getName()));
        return new CommandResponseJsonObject(jsonGenerator.toJson(adaptorc));
      }        
    } else if (command.getOperation().equals(AdaptorCommand.LIST)) {
      List<IAdaptor> adaptors = getAdaptorManager().getRegisteredAdaptors();
      List<Adaptor> adaptordefs = new ArrayList<Adaptor>();
      for (IAdaptor a : adaptors) {
        Adaptor adaptorc = new Adaptor();
        adaptorc.fromAdaptor(a);
        adaptordefs.add(adaptorc);
      }
      return new CommandResponseJsonObject(jsonGenerator.toJsonFromAdaptors(adaptordefs));
    }
    return new CommandResponseStatus(false);
  }

  public CommandResponse handleCommand(AnomalyDetectorCommand command) {
    logger.info("handleCommand(\""+command.getOperation()+"\", node)");
    if (command.getOperation().equals(AnomalyDetectorCommand.ADD) || command.getOperation().equals(AnomalyDetectorCommand.UPDATE)) {
      AnomalyDetector detector = null;
      try {
        detector = getAnomalyDetectorManager().get(command.getName());
      } catch (AnomalyException e) {
        //
      }
      if (detector == null) {
        detector = new AnomalyDetector();
        detector.setName(command.getName());
        detector.setDescription(command.getDescription());
        getAnomalyDetectorManager().add(detector);
      } else {
        detector.setDescription(command.getDescription());
        getAnomalyDetectorManager().update(detector);
      }
      return new CommandResponseStatus(true);
    } else if (command.getOperation().equals(AnomalyDetectorCommand.REMOVE)) {
      getAnomalyDetectorManager().remove(command.getName());
      return new CommandResponseStatus(true);
    } else if (command.getOperation().equals(AnomalyDetectorCommand.GET)) {
      AnomalyDetector detector = getAnomalyDetectorManager().get(command.getName());
      return new CommandResponseJsonObject(jsonGenerator.toJson(detector));
    } else if (command.getOperation().equals(AnomalyDetectorCommand.LIST)) {
      return new CommandResponseJsonObject(jsonGenerator.toJsonFromAnomalyDetectorList(getAnomalyDetectorManager().list()));
    }
    return new CommandResponseStatus(false);
  }
  
  /**
   * General handling of all route commands.
   * @param command the command
   * @return the response
   */
  public CommandResponse handleCommand(RouteCommand command) {
    logger.info("handleCommand(\""+command.getOperation()+"\", node)");
    if (command.getOperation().equals(RouteCommand.ADD) || command.getOperation().equals(RouteCommand.UPDATE)) {
      Route route = command.getRoute();
      if (route != null) {
        IRule rule = command.getRoute().toRule(routerManager);
        RouteDefinition def = routerManager.create(route.getName(), route.getAuthor(), route.isActive(), route.getDescription(), route.getRecipients(), rule);
        if (routerManager.getDefinition(route.getName()) == null) {
          routerManager.storeDefinition(def);
        } else {
          routerManager.updateDefinition(def);
        }
        return new CommandResponseStatus(true);       
      }
    } else if (command.getOperation().equals(RouteCommand.REMOVE)) {
      try {
        routerManager.deleteDefinition(command.getRoute().getName());
        return new CommandResponseStatus(true);
      } catch (RuleException e) {
        return new CommandResponseStatus(false);
      }
    } else if (command.getOperation().equals(RouteCommand.GET)) {  
      RouteDefinition def = routerManager.getDefinition(command.getRoute().getName());
      if (def != null) {
        Route route = routeCommandHelper.createRouteFromDefinition(def);
        if (route != null) {
          return new CommandResponseJsonObject(jsonGenerator.toJson(route));
        }
      }
    } else if (command.getOperation().equals(RouteCommand.LIST)){
      List<String> types = routeCommandHelper.translateRouteTypesToRuleNames(command.getListRoutesTypes());
      List<RouteDefinition> definitions = routerManager.getDefinitions(types);
      List<Route> routes = new ArrayList<Route>();
      for (RouteDefinition def : definitions) {
        Route route = routeCommandHelper.createRouteFromDefinition(def);
        if (route != null) {
          if (!command.useActiveFilter() || command.isActiveFilter() == route.isActive()) {
            routes.add(route);
          }
        }
      }
      return new CommandResponseJsonObject(jsonGenerator.toJsonFromRoutes(routes));
    } else if (command.getOperation().equals(RouteCommand.LIST_TYPES)) {    
      try {
        return new CommandResponseJsonObject(new ObjectMapper().writeValueAsString(routeCommandHelper.getRouteTypes()));
      } catch (Exception e) {
        throw new AdministratorException(e);
      }
    } else if (command.getOperation().equals(RouteCommand.CREATE_ROUTE_TEMPLATE)) {
      String key = command.getTemplateRouteType();
      if (routeCommandHelper.hasRouteMapping(key)) {
        Route route = routeCommandHelper.newInstance(key);
        return new CommandResponseJsonObject(jsonGenerator.toJson(route));
      }
    }
   
    return new CommandResponseStatus(false);
  }

  /**
   * General handling of schedule commands
   * @param command the schedule command
   * @return the response to this command
   */
  public CommandResponse handleCommand(ScheduleCommand command) {
    try {
      if (command.getOperation().equals(ScheduleCommand.ADD) || command.getOperation().equals(ScheduleCommand.UPDATE)) {
        if (command.getIdentfier() != 0) {
          scheduler.reregister(command.getIdentfier(), command.getExpression(), command.getRouteName());
        } else {
          scheduler.register(command.getExpression(), command.getRouteName());
        }
        return new CommandResponseStatus(true);
      } else if (command.getOperation().equals(ScheduleCommand.REMOVE)) {
        if (command.getIdentfier() != 0) {
          scheduler.unregister(command.getIdentfier());
          return new CommandResponseStatus(true);
        }
      } else if (command.getOperation().equals(ScheduleCommand.GET)) {
        if (command.getIdentfier() != 0) {
          CronEntry entry = scheduler.getEntry(command.getIdentfier());
          return new CommandResponseJsonObject(jsonGenerator.toJson(entry));
        } else if (command.getRouteName() != null) {
          List<CronEntry> entries = scheduler.getSchedule(command.getRouteName());
          return new CommandResponseJsonObject(jsonGenerator.toJsonFromCronEntries(entries));
        }
      } else if (command.getOperation().equals(ScheduleCommand.LIST)) {
        return new CommandResponseJsonObject(jsonGenerator.toJsonFromCronEntries(scheduler.getSchedule()));
      }
    } catch (Exception e) {
      logger.warn("Failed to handle command", e);
    }
      
    return new CommandResponseStatus(false);
  }
  
  /**
   * @return the adaptorManager
   */
  public IBltAdaptorManager getAdaptorManager() {
    return adaptorManager;
  }

  /**
   * @param adaptorManager the adaptorManager to set
   */
  @Autowired
  public void setAdaptorManager(IBltAdaptorManager adaptorManager) {
    this.adaptorManager = adaptorManager;
  }

  /**
   * @return the anomalyDetectorManager
   */
  public IAnomalyDetectorManager getAnomalyDetectorManager() {
    return anomalyDetectorManager;
  }

  /**
   * @param anomalyDetectorManager the anomalyDetectorManager to set
   */
  @Autowired
  public void setAnomalyDetectorManager(IAnomalyDetectorManager anomalyDetectorManager) {
    this.anomalyDetectorManager = anomalyDetectorManager;
  }

  /**
   * @return the routerManager
   */
  public IRouterManager getRouterManager() {
    return routerManager;
  }

  /**
   * @param routerManager the routerManager to set
   */
  @Autowired
  public void setRouterManager(IRouterManager routerManager) {
    this.routerManager = routerManager;
  }

  /**
   * @return the routeCommandHelper
   */
  public RouteCommandHelper getRouteCommandHelper() {
    return routeCommandHelper;
  }

  /**
   * @param routeCommandHelper the routeCommandHelper to set
   */
  @Autowired
  public void setRouteCommandHelper(RouteCommandHelper routeCommandHelper) {
    this.routeCommandHelper = routeCommandHelper;
  }

  /**
   * @return the scheduler
   */
  public IBeastScheduler getScheduler() {
    return scheduler;
  }

  /**
   * @param scheduler the scheduler to set
   */
  @Autowired
  public void setScheduler(IBeastScheduler scheduler) {
    this.scheduler = scheduler;
  }

  /**
   * @return the jsonGenerator
   */
  public JsonGenerator getJsonGenerator() {
    return jsonGenerator;
  }

  /**
   * @param jsonGenerator the jsonGenerator to set
   */
  @Autowired
  public void setJsonGenerator(JsonGenerator jsonGenerator) {
    this.jsonGenerator = jsonGenerator;
  }

  /**
   * @return the jsonCommandParser
   */
  public JsonCommandParser getJsonCommandParser() {
    return jsonCommandParser;
  }

  /**
   * @param jsonCommandParser the jsonCommandParser to set
   */
  @Autowired
  public void setJsonCommandParser(JsonCommandParser jsonCommandParser) {
    this.jsonCommandParser = jsonCommandParser;
  }
  
}
