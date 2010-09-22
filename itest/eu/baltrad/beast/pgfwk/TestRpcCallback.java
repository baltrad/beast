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
package eu.baltrad.beast.pgfwk;

import eu.baltrad.beast.adaptor.IAdaptorCallback;
import eu.baltrad.beast.message.IBltMessage;

/**
 * @author Anders Henja
 */
public class TestRpcCallback implements IAdaptorCallback {
  private volatile IBltMessage message = null;
  private Object result = null;
  private Throwable t = null;
  private boolean tout = false;
  
  /**
   * Resets the variables for next test.
   */
  public void reset() {
    message = null;
    result = null;
    t = null;
    tout = false;
  }
  
  /**
   * @return the message
   */
  public IBltMessage getMessage() {
    return message;
  }
  
  /**
   * @return the result object (on success)
   */
  public Object getResult() {
    return result;
  }
  
  /**
   * @return the throwable on error
   */
  public Throwable getThrowable() {
    return t;
  }
  
  /**
   * @return if a timeout occured or not
   */
  public boolean isTimeout() {
    return tout;
  }
  
  /**
   * Waits some time for a message to arrive
   * @param timeout
   */
  public synchronized IBltMessage waitForResponse(long timeout) {
    long currtime = System.currentTimeMillis();
    long endtime = currtime + timeout;
    while (message == null && (currtime < endtime)) {
      try {
        notifyAll();
        wait(endtime - currtime);
      } catch (Throwable t) {
      }
      currtime = System.currentTimeMillis();
    }
    notifyAll();
    return this.message;
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.xmlrpc.IXmlRpcCallback#error(eu.baltrad.beast.message.IBltMessage, java.lang.Throwable)
   */
  @Override
  public synchronized void error(IBltMessage message, Throwable t) {
    this.message = message;
    this.t = t;
    notifyAll();
  }

  /**
   * @see eu.baltrad.beast.adaptor.xmlrpc.IXmlRpcCallback#success(eu.baltrad.beast.message.IBltMessage, java.lang.Object)
   */
  @Override
  public synchronized void success(IBltMessage message, Object result) {
    this.message = message;
    this.result = result;
    notifyAll();
  }

  /**
   * @see eu.baltrad.beast.adaptor.xmlrpc.IXmlRpcCallback#timeout(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public synchronized void timeout(IBltMessage message) {
    this.message = message;
    this.tout = true; 
    notifyAll();
  }
}
