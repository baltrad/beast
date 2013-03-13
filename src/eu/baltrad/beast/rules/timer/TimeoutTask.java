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

import java.util.TimerTask;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Anders Henja
 */
public class TimeoutTask extends TimerTask {
  /**
   * The unique id
   */
  private long id = 0;
  
  /**
   * The rule
   */
  private ITimeoutRule rule = null;

  /**
   * The object data
   */
  private Object data = null;
  
  /**
   * A listener for listening on events
   */
  private ITimeoutTaskListener listener = null;
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(TimeoutTask.class);

  /**
   * Default constructor
   */
  public TimeoutTask() {
  }
  
  /**
   * @see java.util.TimerTask#run()
   */
  @Override
  public void run() {
    logger.debug("run()");
    try {
      listener.timeoutNotification(id, rule, data);
    } catch (Throwable t) {
      logger.error(t);
    }
  }

  /**
   * @see java.util.TimerTask#cancel()
   */
  @Override
  public boolean cancel() {
    logger.debug("cancel()");
    boolean result = super.cancel();
    try { 
      listener.cancelNotification(id, rule, data);
    } catch (Throwable t) {
      // Do not allow an exception out of here.
      logger.error(t);
    }
    return result;
  }

  /**
   * Stops the task, @see java.util.TimerTask#cancel().
   */
  public boolean stop() {
    logger.debug("stop()");
    return super.cancel();
  }

  /**
   * @param id the id to set
   */
  public void setId(long id) {
    logger.debug("setId("+id+")");
    this.id = id;
  }

  /**
   * @return the id
   */
  public long getId() {
    return id;
  }

  /**
   * @param rule the rule to set
   */
  public void setRule(ITimeoutRule rule) {
    this.rule = rule;
  }

  /**
   * @return the rule
   */
  public ITimeoutRule getRule() {
    return rule;
  }

  /**
   * @param listener the listener to set
   */
  public void setListener(ITimeoutTaskListener listener) {
    this.listener = listener;
  }

  /**
   * @return the listener
   */
  public ITimeoutTaskListener getListener() {
    return listener;
  }

  /**
   * @param data the data to set
   */
  public void setData(Object data) {
    this.data = data;
  }

  /**
   * @return the data
   */
  public Object getData() {
    return data;
  }
}
