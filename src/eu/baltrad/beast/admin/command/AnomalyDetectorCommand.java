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
package eu.baltrad.beast.admin.command;

import eu.baltrad.beast.admin.Command;

/**
 * Commands affecting anomaly detectors
 * @author anders
 */
public class AnomalyDetectorCommand extends Command {
  public final static String ADD = "add_anomaly_detector";
  public final static String UPDATE = "update_anomaly_detector";
  public final static String REMOVE = "remove_anomaly_detector";
  public final static String GET = "get_anomaly_detector";
  public final static String LIST = "list_anomaly_detectors";

  private String operation = null;
  private String name = null;
  private String description = null;

  public AnomalyDetectorCommand() {
  }

  public AnomalyDetectorCommand(String operation) {
    setOperation(operation);
  }

  public AnomalyDetectorCommand(String operation, String name) {
    setOperation(operation);
    setName(name);
  }

  public AnomalyDetectorCommand(String operation, String name, String description) {
    setOperation(operation);
    setName(name);
    setDescription(description);
  }

  /**
   * @return the operation
   */
  public String getOperation() {
    return operation;
  }

  /**
   * @param operation the operation to set
   */
  public void setOperation(String operation) {
    this.operation = operation;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

}
