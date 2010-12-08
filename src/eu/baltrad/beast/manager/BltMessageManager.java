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
package eu.baltrad.beast.manager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import eu.baltrad.beast.adaptor.IBltAdaptorManager;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.router.IMultiRoutedMessage;
import eu.baltrad.beast.router.IRouter;

/**
 * The message manager that will distribute the messages to
 * the available adaptors/routes.
 * @author Anders Henja
 */
public class BltMessageManager implements IBltMessageManager, InitializingBean, DisposableBean {
  /**
   * The router
   */
  private IRouter router = null;
  
  /**
   * The main adaptor
   */
  private IBltAdaptorManager manager = null;
  
  /**
   * The executor
   */
  private ExecutorService executor = null;

  /**
   * Number of threads in the default executor 
   */
  private int poolSize = 10;
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(BltMessageManager.class);

  /**
   * Default constructor
   */
  public BltMessageManager() {
  }

  /**
   * Constructor
   * @param poolSize the number of threads in the default executor pool
   */
  public BltMessageManager(int poolSize) {
    this.poolSize = poolSize;
  }
  
  /**
   * @param router the router to set
   */
  public void setRouter(IRouter router) {
    this.router = router;
  }

  /**
   * @param manager the adaptor manager to set
   */
  public void setManager(IBltAdaptorManager manager) {
    this.manager = manager;
  }

  /**
   * @param executor the executor service to set
   */
  public synchronized void setExecutor(ExecutorService executor) {
    this.executor = executor;
  }
  
  /**
   * @return the executor
   */
  public ExecutorService getExecutor() {
    return this.executor;
  }
  
  /**
   * @see IBltMessageManager#manage(IBltMessage)
   */
  public synchronized void manage(IBltMessage message) {
    Runnable r = createRunnable(message);
    try {
      executor.execute(r);
    }catch (Throwable t) {
      t.printStackTrace();
    }
  }
  
  /**
   * Creates a runnable for use with an executor.
   * @param message a message
   * @return a runnable
   */
  protected Runnable createRunnable(final IBltMessage message) {
    logger.debug("createRunnable("+message.getClass().getName()+")");
    return new Runnable() {
      @Override
      public void run() {
        try {
          List<IMultiRoutedMessage> msgs = router.getMultiRoutedMessages(message);
          for (IMultiRoutedMessage msg : msgs) {
            manager.handle(msg);
          }
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    };
  }

  /**
   * If the executor service not has been set when arriving here, the
   * default executor service will be set.
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public synchronized void afterPropertiesSet() throws Exception {
    if (executor == null) {
      executor = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
          Thread th = new Thread(r);
          th.setDaemon(true);
          return th;
        }
      });
    }
  }
  
  @Override
  public synchronized void shutdown() {
    if (executor != null) {
      executor.shutdown();
      
      try {
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
          executor.shutdownNow();
          if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
            System.out.println("Failed to terminate all pending jobs");
          }
        }
      } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
      }
      
      executor = null;
    }
  }

  /**
   * @see org.springframework.beans.factory.DisposableBean#destroy()
   */
  @Override
  public void destroy() throws Exception {
    shutdown();
  }
}
