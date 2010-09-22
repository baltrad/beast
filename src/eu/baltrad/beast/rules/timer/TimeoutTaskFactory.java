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
package eu.baltrad.beast.rules.timer;

/**
 * @author Anders Henja
 */
public class TimeoutTaskFactory implements ITimeoutTaskFactory {
  /**
   * @see eu.baltrad.beast.rules.timer.ITimeoutTaskFactory#create(eu.baltrad.beast.rules.timer.ITimeoutRule, long, eu.baltrad.beast.rules.timer.ITimeoutTaskListener)
   */
  @Override
  public TimeoutTask create(ITimeoutRule rule, long id, Object data, ITimeoutTaskListener listener) {
    if (rule != null && listener != null) {
      TimeoutTask task = new TimeoutTask();
      task.setId(id);
      task.setRule(rule);
      task.setListener(listener);
      task.setData(data);
      return task;
    }
    throw new TimeoutRuleException();
  }
}
