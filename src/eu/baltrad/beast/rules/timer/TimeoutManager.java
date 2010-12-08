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

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import eu.baltrad.beast.manager.IBltMessageManager;
import eu.baltrad.beast.message.IBltMessage;

/**
 * The manager keeping track on all timeouts.
 * @author Anders Henja
 */
public class TimeoutManager implements ITimeoutTaskListener, DisposableBean {
  /**
   * The unique id, incremented by one.
   */
  private static volatile long UniqueID = 0;
  
  /**
   * The timer
   */
  private Timer timer = null;
  
  /**
   * The timeout task factory
   */
  private ITimeoutTaskFactory factory = null;
  
  /**
   * The message manager for initiating new messages.
   */
  private IBltMessageManager messageManager = null;
  
  /**
   * The registered tasks
   */
  protected Map<Long, TimeoutTask> tasks = null;
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(TimeoutManager.class);

  /**
   * Default constructor
   */
  public TimeoutManager() {
    this.timer = new Timer(true);
    this.tasks = new HashMap<Long, TimeoutTask>();
  }
  
  /**
   * Sets the timer instance
   * @param timer the timer
   */
  public void setTimer(Timer timer) {
    this.timer = timer;
  }
  
  /**
   * The factory for creating TimeoutTasks
   * @param factory the factory
   */
  public void setFactory(ITimeoutTaskFactory factory) {
    this.factory = factory;
  }
  
  /**
   * @param messageManager the message manager to set
   */
  public void setMessageManager(IBltMessageManager messageManager) {
    this.messageManager = messageManager;
  }
  
  /**
   * Checks if any task contains a data object equal to the provided data object.
   * @param data the data object
   * @return true if found otherwise false
   */
  public synchronized boolean isRegistered(Object data) {
    if (data != null) {
      for (TimeoutTask t : tasks.values()) {
        Object o = t.getData();
        if (o != null && o.equals(data)) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Returns the task matching the provided data if any
   * @param data the data object to match against
   * @return the object if found otherwise null
   */
  public synchronized TimeoutTask getRegisteredTask(Object data) {
    if (data != null) {
      for (TimeoutTask t : tasks.values()) {
        Object o = t.getData();
        if (o != null && o.equals(data)) {
          return t;
        }
      }
    }
    return null;
  }
  
  /**
   * Registers a timeout that should be triggered after delay (ms).
   * @param rule the rule to be called
   * @param delay the delay in miliseconds
   * @param data data to be passed on to the one registering the task
   * @return a unique id
   */
  public synchronized long register(ITimeoutRule rule, long delay, Object data) {
    long id = newID();
    TimeoutTask task = factory.create(rule, id, data, this);
    tasks.put(id, task);
    timer.schedule(task, delay);
    logger.debug("registered id: " + id);
    return id;
  }
  
  /**
   * Cancels the timeout with the specified id
   * @param id the id
   */
  public synchronized void cancel(long id) {
    logger.debug("cancel(" + id + ")");

    TimeoutTask task = tasks.get(id);
    if (task != null) {
      task.cancel();
    }
  }

  /**
   * Unregisters the task with the specified id
   * @param id the id
   */
  public synchronized void unregister(long id) {
    logger.debug("unregister(" + id + ")");
    TimeoutTask task = tasks.get(id);
    if (task != null) {
      task.stop();
      tasks.remove(id);
    }
  }
  
  /**
   * Sets the start value for the id-sequence
   * @param v
   */
  protected synchronized void setStartID(long v) {
    UniqueID = v;
  }
  
  /**
   * Returns a new unique id for each call
   * @return the unique id
   */
  protected synchronized long newID() {
    return UniqueID++;
  }

  /**
   * @see eu.baltrad.beast.rules.timer.ITimeoutTaskListener#cancelNotification(long, ITimeoutRule, java.lang.Object)
   */
  @Override
  public synchronized void cancelNotification(long id, ITimeoutRule rule, Object data) {
    logger.debug("cancelNotification(" + id + ")");
    tasks.remove(id);
    IBltMessage message = rule.timeout(id, ITimeoutRule.CANCELLED, data);
    if (message != null) {
      logger.debug("messageManager.manage(message)");
      messageManager.manage(message);
    }
  }

  /**
   * @see eu.baltrad.beast.rules.timer.ITimeoutTaskListener#timeoutNotification(long, eu.baltrad.beast.rules.timer.ITimeoutRule, java.lang.Object)
   */
  @Override
  public synchronized void timeoutNotification(long id, ITimeoutRule rule, Object data) {
    logger.debug("timeoutNotification(" + id + ")");
    tasks.remove(id);
    IBltMessage message = rule.timeout(id, ITimeoutRule.TIMEOUT, data);
    if (message != null) {
      messageManager.manage(message);
    }
  }

  /**
   * @see org.springframework.beans.factory.DisposableBean#destroy()
   */
  @Override
  public void destroy() throws Exception {
    if (this.timer != null) {
      this.timer.cancel();
    }
  }
}
