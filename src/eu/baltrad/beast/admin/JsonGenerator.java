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

import java.util.List;

import eu.baltrad.beast.admin.objects.Adaptor;
import eu.baltrad.beast.admin.objects.Settings;
import eu.baltrad.beast.admin.objects.User;
import eu.baltrad.beast.admin.objects.routes.Route;
import eu.baltrad.beast.qc.AnomalyDetector;
import eu.baltrad.beast.scheduler.CronEntry;

/**
 * Provides functionality for generating jsonified objects.
 * @author anders
 */
public interface JsonGenerator {
  /**
   * Generates a JSON message from a command response object
   * @param response the response object
   * @return the json string
   */
  public String toJson(CommandResponse response);
  
  /**
   * Creates a JSON message from an adaptor
   * @param adaptor the adaptor
   * @return the json string
   */
  public String toJson(Adaptor adaptor);
  
  /**
   * Creates a JSON message from a list of adaptors
   * @param adaptors the adaptor
   * @return the json string
   */
  public String toJsonFromAdaptors(List<Adaptor> adaptors);
  
  /**
   * Generates a JSON message from a route object
   * @param route the route
   * @return the json string
   */
  public String toJson(Route route);
  
  /**
   * Generates a JSON message from a list of routes
   * @param routes the list of routes
   * @return the json string
   */
  public String toJsonFromRoutes(List<Route> routes);
  
  /**
   * Generates a JSON message from a cron entry
   * @param entry the entry
   * @return the json string
   */
  public String toJson(CronEntry entry);
  
  /**
   * Generates a JSON message from a list of cron entries
   * @param entries the cron entries
   * @return the json string
   */
  public String toJsonFromCronEntries(List<CronEntry> entries);
  
  /**
   * Generates a JSON message from an anomaly detector
   * @param detector the detector
   * @return the json string
   */
  public String toJson(AnomalyDetector detector);
  
  /**
   * Generates a JSON message from a list of anomaly detectors
   * @param detectors the detectors
   * @return the JSON string
   */
  public String toJsonFromAnomalyDetectorList(List<AnomalyDetector> detectors);
  
  /**
   * Generates a JSON message from a user
   * @param user the user
   * @return the JSON string
   */
  public String toJson(User user);
  
  /**
   * Generates a JSON message from a list of users
   * @param users the users
   * @return the JSON string
   */
  public String toJsonFromUsers(List<User> users);
  
  /**
   * Generates a JSON message from settings
   * @param settings the settings
   * @return the JSON string
   */
  public String toJson(Settings settings);
}
