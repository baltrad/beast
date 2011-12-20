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
package eu.baltrad.beast.router;

import java.util.ArrayList;
import java.util.List;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.rules.IRule;

/**
 * This definition is system specific and is not configurable
 * like the other definitions. It basically is here for all
 * managing messages without producing any message since there
 * are no recipients.
  *
 * @author Anders Henja
 */
public class SystemRulesDefinition {
  /**
   * The list of rules that is supported by this system route definition
   */
  private List<IRule> rules = new ArrayList<IRule>();
  
  /**
   * Constructor
   */
  public SystemRulesDefinition() {
  }
  
  /**
   * The rules this system route definition manages.
   * @param rules the rules
   */
  public void setRules(List<IRule> rules) {
    if (rules == null) {
      this.rules = new ArrayList<IRule>();
    } else {
      this.rules = rules;
    }
  }
  
  /**
   * @return the rules for this system route definition
   */
  public List<IRule> getRules() {
    return this.rules;
  }
  
  /**
   * Handles any blt message.
   * @param msg - the blt message
   */
  public void handle(IBltMessage msg) {
    if (msg != null) {
      for (IRule rule : rules) {
        try {
          rule.handle(msg);
        } catch (Exception t) {
          // We want to catch all exceptions but not errors
        }
      }
    }
  }
}
