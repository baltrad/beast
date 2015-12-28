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
public class PcsDefinition {
  private String id;
  private String description;
  private String definition;

  /**
   * Constructor
   * @param id the id of this pcs definition
   */
  public PcsDefinition(String id) {
    this.id = id;
  }
  
  /**
   * @return the id of this definition
   */
  public String getId() {
    return id;
  }
  
  /**
   * @return the description of this definition
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
  
  /**
   * @return the definition
   */
  public String getDefinition() {
    return definition;
  }
  
  /**
   * @param definition the pcs definition
   */
  public void setDefinition(String definition) {
    this.definition = definition;
  }
}
