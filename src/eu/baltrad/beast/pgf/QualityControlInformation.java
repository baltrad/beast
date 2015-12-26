/* --------------------------------------------------------------------
Copyright (C) 2009-2013 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.pgf;

/**
 * @author Anders Henja
 *
 */
public class QualityControlInformation {
  /**
   * The name of the adaptor
   */
  private String adaptorName;
  
  /**
   * The name of the quality control
   */
  private String name;
  
  /**
   * Description of the quality control
   */
  private String description;

  /**
   * Constructor 
   * @param adaptorName name of the adaptor
   * @param name name of quality control
   * @param description description of the quality control
   */
  public QualityControlInformation(String adaptorName, String name, String description) {
    setAdaptorName(adaptorName);
    setName(name);
    setDescription(description);
  }
  
  /**
   * @return the name of the adaptor
   */
  public String getAdaptorName() {
    return adaptorName;
  }

  /**
   * @param adaptorName the adaptor name
   */
  public void setAdaptorName(String adaptorName) {
    this.adaptorName = adaptorName;
  }

  /**
   * @return the name of the quality control
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name of the quality control
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the description of the quality control
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description of the quality control
   */
  public void setDescription(String description) {
    this.description = description;
  }
  
}
