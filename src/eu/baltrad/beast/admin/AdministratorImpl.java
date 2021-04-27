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
import eu.baltrad.beast.admin.command.SettingCommand;
import eu.baltrad.beast.admin.command_response.CommandResponseJsonObject;
import eu.baltrad.beast.admin.command_response.CommandResponseStatus;
import eu.baltrad.beast.admin.objects.Adaptor;
import eu.baltrad.beast.admin.objects.routes.DistributionRoute;
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
      } else if (command instanceof SettingCommand) {
        return handleCommand((SettingCommand)command);
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
   * Adds or updates an adaptor
   * @param adaptorToHandle the adaptor to add or update
   */
  protected void addOrUpdateAdaptor(Adaptor adaptorToHandle) {
    IAdaptor adaptor = getAdaptorManager().getAdaptor(adaptorToHandle.getName());
    XmlRpcAdaptorConfiguration conf = (XmlRpcAdaptorConfiguration)getAdaptorManager().createConfiguration(adaptorToHandle.getType(), adaptorToHandle.getName());
    conf.setURL(adaptorToHandle.getUri());
    conf.setTimeout(adaptorToHandle.getTimeout());
    
    if (adaptor == null) {
      logger.info("Registering adaptor: " + adaptorToHandle.getName() + ", URI: '" + adaptorToHandle.getUri() + "'");
      getAdaptorManager().register(conf);
    } else {
      logger.info("Reregistering adaptor: " + adaptorToHandle.getName() + ", URI: " + adaptorToHandle.getUri());
      getAdaptorManager().reregister(conf);
    }
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
      addOrUpdateAdaptor(command.getAdaptor());
      return new CommandResponseStatus(true);
    } else if (command.getOperation().equals(AdaptorCommand.IMPORT)) {
      if (command.isClearAllBeforeImport()) {
        List<IAdaptor> adaptors = getAdaptorManager().getRegisteredAdaptors();
        for (IAdaptor adaptor: adaptors) {
          try {
            getAdaptorManager().unregister(adaptor.getName());
          } catch (Exception e) {
            logger.info("Failed to unregister adaptor: " + adaptor.getName(), e);
          }
        }
      }
      for (Adaptor adaptor : command.getImportedAdaptors()) {
        addOrUpdateAdaptor(adaptor);
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
    } else if (command.getOperation().equals(AdaptorCommand.LIST) || command.getOperation().equals(AdaptorCommand.EXPORT)) {
      List<IAdaptor> adaptors = getAdaptorManager().getRegisteredAdaptors();
      List<Adaptor> adaptordefs = new ArrayList<Adaptor>();
      for (IAdaptor a : adaptors) {
        Adaptor adaptorc = new Adaptor();
        adaptorc.fromAdaptor(a);
        adaptordefs.add(adaptorc);
      }
      String adaptorsstr = jsonGenerator.toJsonFromAdaptors(adaptordefs);
      if (command.getOperation().equals(AdaptorCommand.EXPORT)) {
        adaptorsstr = "{\"clear_all_before_import\":false,\n\"adaptors\":"+adaptorsstr+"\n}";
      }
      return new CommandResponseJsonObject(adaptorsstr);
    }
    return new CommandResponseStatus(false);
  }

  /**
   * Adds or updates an anomaly detector depending if it already exists or not
   * @param indetector the detector to add/update
   */
  protected void addOrUpdateAnomalyDetector(AnomalyDetector indetector) {
    AnomalyDetector detector = null;
    try {
      detector = getAnomalyDetectorManager().get(indetector.getName());
    } catch (AnomalyException e) {
      //
    }
    if (detector == null) {
      detector = new AnomalyDetector();
      detector.setName(indetector.getName());
      detector.setDescription(indetector.getDescription());
      getAnomalyDetectorManager().add(detector);
    } else {
      detector.setDescription(indetector.getDescription());
      getAnomalyDetectorManager().update(detector);
    }
  }
  
  /**
   * Handles an anomaly detector command
   * @param command the command
   * @return the response
   */
  public CommandResponse handleCommand(AnomalyDetectorCommand command) {
    logger.info("handleCommand(\""+command.getOperation()+"\", node)");
    if (command.getOperation().equals(AnomalyDetectorCommand.ADD) || command.getOperation().equals(AnomalyDetectorCommand.UPDATE)) {
      addOrUpdateAnomalyDetector(command.getAnomalyDetector());
    } else if (command.getOperation().equals(AnomalyDetectorCommand.REMOVE)) {
      getAnomalyDetectorManager().remove(command.getAnomalyDetector().getName());
      return new CommandResponseStatus(true);
    } else if (command.getOperation().equals(AnomalyDetectorCommand.GET)) {
      AnomalyDetector detector = getAnomalyDetectorManager().get(command.getAnomalyDetector().getName());
      return new CommandResponseJsonObject(jsonGenerator.toJson(detector));
    } else if (command.getOperation().equals(AnomalyDetectorCommand.LIST) ||
               command.getOperation().equals(AnomalyDetectorCommand.EXPORT)) {
      String detectorstr = jsonGenerator.toJsonFromAnomalyDetectorList(getAnomalyDetectorManager().list());
      if (command.getOperation().equals(AnomalyDetectorCommand.EXPORT)) {
        detectorstr = "{\"clear_all_before_import\":false,\n\"anomaly-detectors\":"+detectorstr+"\n}";
      }
      return new CommandResponseJsonObject(detectorstr);
    } else if (command.getOperation().equals(AnomalyDetectorCommand.IMPORT)) {
      if (command.isClearAllBeforeImport()) {
        List<AnomalyDetector> detectors = getAnomalyDetectorManager().list();
        for (AnomalyDetector detector : detectors) {
          try {
            getAnomalyDetectorManager().remove(detector.getName());
          } catch (Exception e) {
            logger.warn("Failed to remove detector " + detector.getName(), e);
          }
        }
      }
      for (AnomalyDetector detector : command.getImportedDetectors()) {
        addOrUpdateAnomalyDetector(detector);
      }
      return new CommandResponseStatus(true);
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
    } else if (command.getOperation().equals(RouteCommand.IMPORT)) {
      if (command.isClearAllBeforeImport()) {
        List<RouteDefinition> definitions = routerManager.getDefinitions();
        for (RouteDefinition def : definitions) {
          try {
            routerManager.deleteDefinition(def.getName());
          } catch (Exception e) {
            logger.info("Failed to remove definition", e);
          }
        }
      }
      for (Route route : command.getImportedRoutes()) {
        logger.info("Importing route: " + route.getName());
        IRule rule = route.toRule(routerManager);
        RouteDefinition def = routerManager.create(route.getName(), route.getAuthor(), route.isActive(), route.getDescription(), route.getRecipients(), rule);
        if (routerManager.getDefinition(route.getName()) == null) {
          routerManager.storeDefinition(def);
        } else {
          routerManager.updateDefinition(def);
        }
      }
      return new CommandResponseStatus(true);       
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
    } else if (command.getOperation().equals(RouteCommand.LIST) ||
               command.getOperation().equals(RouteCommand.EXPORT)) {
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
      String strresult = jsonGenerator.toJsonFromRoutes(routes);
      if (command.getOperation().equals(RouteCommand.EXPORT)) {
        strresult = "{\"clear_all_before_import\":false,\n\"routes\":"+strresult+"\n}";
      }
      return new CommandResponseJsonObject(strresult);
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
   * Updates or ads a cron entry
   * @param entry the entry to update or add
   */
  protected void addOrUpdateEntry(CronEntry entry) {
    if (entry.getId() != 0) {
      scheduler.reregister(entry.getId(), entry.getExpression(), entry.getName());
    } else {
      scheduler.register(entry.getExpression(), entry.getName());
    }
  }
  
  /**
   * General handling of schedule commands
   * @param command the schedule command
   * @return the response to this command
   */
  public CommandResponse handleCommand(ScheduleCommand command) {
    try {
      if (command.getOperation().equals(ScheduleCommand.ADD) || command.getOperation().equals(ScheduleCommand.UPDATE)) {
        addOrUpdateEntry(command.getEntry());
        return new CommandResponseStatus(true);
      } else if (command.getOperation().equals(ScheduleCommand.REMOVE)) {
        if (command.getEntry().getId() != 0) {
          scheduler.unregister(command.getEntry().getId());
          return new CommandResponseStatus(true);
        }
      } else if (command.getOperation().equals(ScheduleCommand.GET)) {
        if (command.getEntry().getId() != 0) {
          CronEntry entry = scheduler.getEntry(command.getEntry().getId());
          return new CommandResponseJsonObject(jsonGenerator.toJson(entry));
        } else if (command.getEntry().getName() != null) {
          List<CronEntry> entries = scheduler.getSchedule(command.getEntry().getName());
          return new CommandResponseJsonObject(jsonGenerator.toJsonFromCronEntries(entries));
        }
      } else if (command.getOperation().equals(ScheduleCommand.LIST) ||
          command.getOperation().equals(ScheduleCommand.EXPORT)) {
        String schedulestr = jsonGenerator.toJsonFromCronEntries(scheduler.getSchedule());
        if (command.getOperation().equals(ScheduleCommand.EXPORT)) {
          schedulestr = "{\"clear_all_before_import\":false,\n\"entries\":"+schedulestr+"\n}";
        }
        return new CommandResponseJsonObject(schedulestr);
      } else if (command.getOperation().equals(ScheduleCommand.IMPORT)) {
        if (command.isClearAllBeforeImport()) {
          List<CronEntry> schedule = scheduler.getSchedule();
          for (CronEntry entry : schedule) {
            scheduler.unregister(entry.getId());
          }
        }
        
        for (CronEntry entry : command.getImportedEntries()) {
          try {
            addOrUpdateEntry(entry);
          } catch (Exception e) {
            logger.warn("Failed to add or update cron entry", e);
          }
        }
        
        return new CommandResponseStatus(true);
      }
    } catch (Exception e) {
      logger.warn("Failed to handle command", e);
    }
      
    return new CommandResponseStatus(false);
  }
  
  /**
   * General handling of schedule commands
   * @param command the schedule command
   * @return the response to this command
   */
  public CommandResponse handleCommand(SettingCommand command) {
    try {
      if (command.getOperation().equals(SettingCommand.UPDATE_SETTINGS) ||
          command.getOperation().equals(SettingCommand.LIST)) {
        return new CommandResponseStatus(true);
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
