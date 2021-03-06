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

import java.util.List;

/**
 * @author Anders Henja
 *
 */
public interface IPgfClientHelper {
  /**
   * @return A list of all available quality controls from all registered adaptors.
   */
  public List<QualityControlInformation> getQualityControls();
  
  /**
   * @return a list of all available areas from all registered adaptors
   */
  public List<AreaInformation> getAreas();
  
  /**
   * @param adaptorName the name of the adaptor
   * @return a list of all available areas for the specified adaptor
   */
  public List<AreaInformation> getAreas(String adaptorName);
  
  /**
   * @return the unique area ids that are available in the PGFs.
   */
  public List<String> getUniqueAreaIds();
  
  /**
   * @return the pcs definitions for all available adaptors
   */
  public List<PcsDefinition> getPcsDefinitions();
  
  /**
   * @param adaptorName the name of the adaptor
   * @return the pcs definitions for all the specified adaptor
   */
  public List<PcsDefinition> getPcsDefinitions(String adaptorName);
  
  /**
   * @return the unique pcs definition ids that are available in the PGFs.
   */
  public List<String> getUniquePcsIds();
}
