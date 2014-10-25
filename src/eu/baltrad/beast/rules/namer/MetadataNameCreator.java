/* --------------------------------------------------------------------
Copyright (C) 2009-2014 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.rules.namer;

import eu.baltrad.bdb.oh5.Metadata;

/**
 * Creates a specific name from the metadata. It is not a template namer or anything,
 * it is just for creating a very specific name from the metadata.
 * 
 * @author Anders Henja
 */
public interface MetadataNameCreator {
  /**
   * If this name creater supports the provided tag (${...})
   * @param tag the tag
   * @return if tag is supported or not
   */
  public boolean supports(String tag);
  
  /**
   * Creates a name that should replace the provided tag. In some circumstances the tag can contain additional information
   * that needs to be handled.
   * @param tag the tag
   * @param metadata the metadata
   * @return the tag
   */
  public String createName(String tag, Metadata metadata);
}
