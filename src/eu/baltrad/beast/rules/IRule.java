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
 * All rules that are used for routing purpose should implement this
 * interface regardless if it is a scripted rule or a static rule.
 * @author Anders Henja
 */
public interface IRule {
  /**
   * Handle this message. If returning a message it means that this
   * rule has evaluated to true and the message should be managed.
   * Otherwise, if null is returned, the rule has either managed
   * the message or it can not handle the message.
   * @param message
   * @return a message or null
   */
  public IBltMessage handle(IBltMessage message);

  /**
   * A description on what type of rule this is, e.g. static, groovy, etc...
   * @return the type
   */
  public String getType();
}
