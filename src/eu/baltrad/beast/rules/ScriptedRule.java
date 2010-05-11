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

import eu.baltrad.beast.message.IBltMessage;

/**
 * @author Anders Henja
 */
public class ScriptedRule implements IRule {
  /**
   * The script rule.
   */
  private IScriptableRule rule = null;
  
  /**
   * The script
   */
  private String definition = null;
  
  /**
   * What type of script this rule is defined by
   */
  private String type = null;
  
  /**
   * Constructor 
   * @param srule - the scripted rule
   * @param definition - the script
   */
  public ScriptedRule(IScriptableRule srule, String type, String definition) {
    this.rule = srule;
    this.type = type;
    this.definition = definition;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    return this.rule.handle(message);
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#getDefinition()
   */
  //@Override
  public String getDefinition() {
    return this.definition;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRule#getType()
   */
  @Override
  public String getType() {
    return this.type;
  }
}
