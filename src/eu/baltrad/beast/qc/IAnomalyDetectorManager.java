/* --------------------------------------------------------------------
Copyright (C) 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.qc;

import java.util.List;

/**
 * Provides database access to list/add and remove anomaly detectors. 
 * @author Anders Henja
 */
public interface IAnomalyDetectorManager {
  /**
   * @param detector the detector to add
   * @throws AnomalyException if detector not could be added
   */
  public void add(AnomalyDetector detector);
  
  /**
   * @param detector the detector to update
   * @throws AnomalyException if detector not could be updated
   */
  public void update(AnomalyDetector detector);
  
  /**
   * @return the list of registered detectors
   */
  public List<AnomalyDetector> list();
  
  /**
   * @param name the detector name
   * @return the found detector
   * @throws AnomalyException if detector not could be found
   */
  public AnomalyDetector get(String name);
  
  /**
   * @param detector the detector to remove
   */
  public void remove(String detector);
}
