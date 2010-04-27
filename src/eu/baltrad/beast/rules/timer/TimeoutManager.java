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

import eu.baltrad.beast.manager.IBltMessageManager;
import eu.baltrad.beast.message.IBltMessage;

/**
 * The manager keeping track on all timeouts.
 * @author Anders Henja
 */
public class TimeoutManager implements ITimeoutTaskListener {
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
   * Constructor, same as invoking {@link #TimeoutManager(true)}.
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
   * Registers a timeout that should be triggered after delay (ms).
   * @param rule the rule to be called
   * @param delay the delay in miliseconds
   * @return a unique id
   */
  public synchronized long register(ITimeoutRule rule, long delay) {
    long id = newID();
    TimeoutTask task = factory.create(rule, id, this);
    tasks.put(id, task);
    timer.schedule(task, delay);
    return id;
  }
  
  /**
   * Cancels the timeout with the specified id
   * @param id the id
   */
  public synchronized void cancel(long id) {
    TimeoutTask task = tasks.get(id);
    if (task != null) {
      task.cancel();
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
   * @see eu.baltrad.beast.rules.timer.ITimeoutListener#cancelNotification(long)
   */
  @Override
  public synchronized void cancelNotification(long id, ITimeoutRule rule) {
    tasks.remove(id);
    IBltMessage message = rule.timeout(id, ITimeoutRule.CANCELLED);
    if (message != null) {
      messageManager.manage(message);
    }
  }

  /**
   * @see eu.baltrad.beast.rules.timer.ITimeoutListener#timeoutNotification(long)
   */
  @Override
  public synchronized void timeoutNotification(long id, ITimeoutRule rule) {
    tasks.remove(id);
    IBltMessage message = rule.timeout(id, ITimeoutRule.TIMEOUT);
    if (message != null) {
      messageManager.manage(message);
    }
  }
}
