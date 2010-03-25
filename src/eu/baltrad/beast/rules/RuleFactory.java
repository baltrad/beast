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
package eu.baltrad.beast.rules;

import java.util.ArrayList;
import java.util.List;

/**
 * The rule factory that will create the wanted rule.
 * @author Anders Henja
 */
public class RuleFactory implements IRuleFactory {
  private List<IRuleCreator> creators = new ArrayList<IRuleCreator>();
  
  /**
   * Sets the creators.
   * @param creators a list of creators
   */
  public void setCreators(List<IRuleCreator> creators) {
    this.creators = creators;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRuleFactory#create(java.lang.String, java.lang.String)
   */
  @Override
  public IRule create(String type, String definition) {
    for (IRuleCreator creator : creators) {
      if (creator.getType().equals(type)) {
        return creator.create(definition);
      }
    }
    throw new RuleException("Type '"+type+"' is not supported by the factory");
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleFactory#getTypes()
   */
  @Override  
  public List<String> getTypes() {
    List<String> result = new ArrayList<String>();
    for (IRuleCreator creator:creators) {
      result.add(creator.getType());
    }
    return result;
  }
}
