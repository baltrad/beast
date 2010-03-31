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

import java.util.List;

import eu.baltrad.beast.router.Route;

/**
 * @author Anders Henja
 */
public interface IBltAdaptorManager {
  /**
   * Stores an adaptor of specified in the database. Note,
   * the adaptor itself will not be stored, this should be done
   * by the caller of this function.
   * @param configuration - the configuration
   * @return the created adaptor on success
   * @throws AdaptorException on failure
   */
  public IAdaptor register(IAdaptorConfiguration configuration);

  /**
   * Re-registers an existing adaptor in the database. If the
   * previous configuration type is different from the provided
   * configuration type, then the adaptor specific configuration
   * will be modifed as well. 
   * @param configuration the new configuration
   * @return the configured adaptor
   * @throws AdaptorException on error
   */
  public IAdaptor reregister(IAdaptorConfiguration configuration);
  
  /**
   * Unregisters the adaptor with the specified name (and will also
   * remove it from the database).
   * @param name the name of the adaptor to unregister
   * @throws AdaptorException on failure
   */
  public void unregister(String name);
  
  /**
   * Returns the list of registered adaptors
   * @return a list of registered adaptors
   */
  public List<IAdaptor> getRegisteredAdaptors();
  
  /**
   * Returns a sorted list of available types
   * @return a sorted list of available types.
   */
  public List<String> getAvailableTypes();
  
  /**
   * Creates an adaptor configuration of the specified type
   * @param type the type
   * @param name the name of the configuration
   * @return the configuration
   * @throws AdaptorException on failure
   */
  public IAdaptorConfiguration createConfiguration(String type, String name);
  
  /**
   * Returns a sorted list of available adaptor names
   * @return a list of available adaptors 
   */
  public List<String> getAdaptorNames();
  
  /**
   * Returns the adaptor with the specified name
   * @param name the name of the requested adaptor
   * @return the adaptor (or null if there is none)
   */
  public IAdaptor getAdaptor(String name);
  
  /**
   * Handles a route. If this adaptor could not handle the route an AdaptorException should be
   * thrown.
   * @param route the route to handle
   * @throws AdaptorException
   */
  public void handle(Route route);

  /**
   * Same as {@link #handle(Route)} but with the possibility to get the result
   * to the callback
   * @param route the route
   * @param callback the callback
   * @throws AdaptorException
   */
  public void handle(Route route, IAdaptorCallback callback);
}