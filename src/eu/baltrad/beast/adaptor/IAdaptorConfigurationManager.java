/* --------------------------------------------------------------------
Copyright (C) 2009-2010 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.adaptor;

/**
 * Defines what is nessecary in order to be able to configure
 * and persist adaptor configurations.
 * @author Anders Henja
 */
public interface IAdaptorConfigurationManager {
  /**
   * Returns the type that is managed by this configuration manager
   * @return the type
   */
  public String getType();
  
  /**
   * Creates a new instance of an adaptor configuration.
   * @param name the name of the adaptor that should be created
   * @return a new adaptor configuration
   */
  public IAdaptorConfiguration createConfiguration(String name);
  
  /**
   * Persists the adaptor configuration.
   * @param id the unique id that defines this adaptor
   * @param configuration the configuration
   * @return a new adaptor on success
   * @throws AdaptorException on failure
   */
  public IAdaptor store(int id, IAdaptorConfiguration configuration);
  
  /**
   * Removes the adaptor configuration.
   * @param id the configuration that should be removed
   */
  public void remove(int id);
}
