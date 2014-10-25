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

package eu.baltrad.beast.rules.namer;

import java.util.ArrayList;
import java.util.List;

/**
 * A metadata name creator provider.
 * @author Anders Henja
 */
public class MetadataNameCreatorFactory {
  
  private List<MetadataNameCreator> creators = null;
  /**
   * Constructor
   */
  public MetadataNameCreatorFactory() {
    setCreators(new ArrayList<MetadataNameCreator>());
  }

  /**
   * @param creators the list of metadata name creators to set
   */
  public void setCreators(List<MetadataNameCreator> creators) {
    this.creators = creators;
  }

  /**
   * @return the metadata name creators
   */
  public List<MetadataNameCreator> getCreators() {
    return creators;
  }  
  
  /**
   * Returns if there are any namer that supports the provided tag
   * @param tag the tag
   * @return if there exist any name creator or not
   */
  public boolean supports(String tag) {
    for (MetadataNameCreator creator : creators) {
      if (creator.supports(tag)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Returns a metadata name creator that supports the provided tag. If more than one creator supports the provided tag, the first found is returned
   * @param tag the tag
   * @return the creator or null if none is found
   */
  public MetadataNameCreator get(String tag) {
    for (MetadataNameCreator creator : creators) {
      if (creator.supports(tag)) {
        return creator;
      }
    }
    return null;
  }
}
