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

import java.util.List;

import eu.baltrad.beast.admin.Command;
import eu.baltrad.beast.qc.AnomalyDetector;

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
  public final static String EXPORT = "export_anomaly_detectors";
  public final static String IMPORT = "import_anomaly_detectors";

  private String operation = null;

  private AnomalyDetector anomalyDetector = null;
  
  /**
   * If importing detectors, this list will contain the detectors to be imported.
   */
  private List<AnomalyDetector> importedDetectors = null;
  
  /**
   * If all adaptors should be removed before importing the data.
   */
  private boolean clearAllBeforeImport = false;
  
  
  public AnomalyDetectorCommand() {
  }

  public AnomalyDetectorCommand(String operation) {
    setOperation(operation);
  }

  public AnomalyDetectorCommand(String operation, AnomalyDetector detector) {
    setOperation(operation);
    setAnomalyDetector(detector);
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
   * @see Command#validate()
   */
  @Override
  public boolean validate() {
    if (LIST.equals(operation) || EXPORT.equals(operation)) {
      return true;
    } else if (IMPORT.equals(operation)) {
      for (AnomalyDetector detector : importedDetectors) {
        if (detector.getName() == null || detector.getName().isEmpty() || detector.getDescription() == null || detector.getDescription().isEmpty()) {
          return false;
        }
      }
      return true;
    } else if (GET.equals(operation) || REMOVE.equals(operation)) {
      if (anomalyDetector != null && anomalyDetector.getName() != null && !anomalyDetector.getName().isEmpty()) {
        return true;
      }
    } else if (ADD.equals(operation) || UPDATE.equals(operation)) {
      if (anomalyDetector != null && anomalyDetector.getName() != null && 
          !anomalyDetector.getName().isEmpty() && anomalyDetector.getDescription() != null && !anomalyDetector.getDescription().isEmpty()) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return the anomalyDetector
   */
  public AnomalyDetector getAnomalyDetector() {
    return anomalyDetector;
  }

  /**
   * @param anomalyDetector the anomalyDetector to set
   */
  public void setAnomalyDetector(AnomalyDetector anomalyDetector) {
    this.anomalyDetector = anomalyDetector;
  }

  /**
   * @return the importedDetectors
   */
  public List<AnomalyDetector> getImportedDetectors() {
    return importedDetectors;
  }

  /**
   * @param importedDetectors the importedDetectors to set
   */
  public void setImportedDetectors(List<AnomalyDetector> importedDetectors) {
    this.importedDetectors = importedDetectors;
  }

  /**
   * @return the clearAllBeforeImport
   */
  public boolean isClearAllBeforeImport() {
    return clearAllBeforeImport;
  }

  /**
   * @param clearAllBeforeImport the clearAllBeforeImport to set
   */
  public void setClearAllBeforeImport(boolean clearAllBeforeImport) {
    this.clearAllBeforeImport = clearAllBeforeImport;
  }

}
