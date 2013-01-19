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

package eu.baltrad.beast.system.host;

import java.util.List;

/**
 * Host filter mechanism to verify if a specific ip address has been validated against the
 * registered ip filters.
 * @author Anders Henja
 */
public interface IHostFilterManager {
  /**
   * Registers a valid ipaddress as a filter pattern. It is up to the implementing part to make sure that
   * the registered filter is able to manage the pattern.  
   * @param ipfilter the filter
   * @throws IllegalArgumentException if the pattern not is supported.
   */
  public void add(String ipfilter);
  
  /**
   * Removes the pattern from the registry
   * @param ipfilter
   */
  public void remove(String ipfilter);
  
  /**
   * Verifies the ipaddress against the registered filters.
   * @param ip the ip address to try
   * @return if the ip address is accepted by any of the patterns
   */
  public boolean accepted(String ip);
  
  /**
   * Returns if the specified ipfilter already has been registered.
   * @param ipfilter the filter to be checked
   * @return true if it already is registered.
   */
  public boolean isRegistered(String ipfilter);
  
  /**
   * @return the registered patterns
   */
  public List<String> getPatterns();
}
