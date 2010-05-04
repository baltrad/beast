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
package eu.baltrad.beast.rules.composite;

import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleCreator;

/**
 * @author Anders Henja
 */
public class CompositeCreator implements IRuleCreator {
  public final static String TYPE = "blt_composite";

  /**
   * @see eu.baltrad.beast.rules.IRuleCreator#create(java.lang.String)
   */
  @Override
  public IRule create(String definition) {
    return null;
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleCreator#getType()
   */
  @Override
  public String getType() {
    return TYPE;
  }
}
