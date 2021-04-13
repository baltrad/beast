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

import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * @author Anders Henja
 */
@JsonRootName("anomaly-detector")
public class AnomalyDetector {
  /**
   * Detector name
   */
  private String name = null;
  
  /**
   * Description
   */
  private String description = null;

  /**
   * Default constructor
   */
  public AnomalyDetector() {
  }
  
  /**
   * Constructor
   * @param name the of the detector
   * @param description of the detector
   */
  public AnomalyDetector(String name, String description) {
    setName(name);
    setDescription(description);
  }
  
  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }
}
