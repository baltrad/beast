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

import groovy.lang.GroovyClassLoader;

/**
 * 
 * @author Anders Henja
 */
public class GroovyRuleCreator implements IRuleCreator {
  public final static String TYPE = "groovy";
  /**
   * @see IRuleCreator#create(String)
   */
  @SuppressWarnings("unchecked")
  public IRule create(String definition) {
    GroovyClassLoader gcl = new GroovyClassLoader();
    try {
      Class c = gcl.parseClass(definition);
      IScriptableRule srule = (IScriptableRule)c.newInstance();
      return new ScriptedRule(srule, TYPE, definition);
    } catch (Throwable t) {
      throw new RuleException("Failed to generate groovy rule", t);
    }      
  }
  
  /**
   * @see IRuleCreator#getType()
   */  
  public String getType() {
    return TYPE;
  }
}
