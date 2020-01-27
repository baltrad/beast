/**
 * 
 */
package eu.baltrad.beast.exchange;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * The publisher (manager) that keeps track of publishing file requests to different nodes. There is one
 * thread pool executor / node so that a slowly responding node doesn't lock everything for the other
 * nodes.
 * @author anders
 */
public class PooledFileRequestPublisher implements IPooledFileRequestPublisher, InitializingBean {
  /**
   * The keep alive time of threads that are idling
   */
  private static final int DEFAULT_KEEP_ALIVE_TIME = 60;

  /**
   * Logger
   */
  private final static Logger logger = LogManager.getLogger(PooledFileRequestPublisher.class);
      
  /**
   * The thread executor
   */
  private ThreadPoolExecutor executor;
  
  /**
   * Number of available slots that can be published before they are beeing rejected
   */
  private int queueSize;
  
  /**
   * Number of idling threads (1 is usually a good idea)
   */
  private int corePoolSize;
  
  /**
   * Max number of threads that will be created during high load
   */
  private int maxPoolSize;
  
  /**
   * Exchange manager
   */
  private IExchangeManager exchangeManager = null;
  
  /**
   * When a job is rejected (switched out). This handler will be incoked with that job
   */
  protected class RejectedExecutionHandler extends ThreadPoolExecutor.DiscardOldestPolicy {
    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
      super.rejectedExecution(runnable, executor);
      logger.error("Discarded PostFile-task due to full work queue for thread pool. " + 
                   "Data will not be sent to subscriber. No of queued tasks: " + executor.getQueue().size() + 
                   ". No of active threads: " + executor.getActiveCount());   
    }
  }
  
  /**
   * Default construcor
   */
  public PooledFileRequestPublisher() {
    this(100, 1, 5);
  }
  
  /**
   * Constructor
   * @param queueSize
   * @param corePoolSize
   * @param maxPoolSize
   */
  public PooledFileRequestPublisher(int queueSize, int corePoolSize, int maxPoolSize) {
    this.queueSize = queueSize;
    this.corePoolSize = corePoolSize;
    this.maxPoolSize = maxPoolSize;
  }
  
  /**
   * Creates the actual executor. Can be overriden by subclasses or tests.
   */
  public void afterPropertiesSet() {
    logger.info("Creating FramePublisher - queueSize: " + queueSize + ", corePoolSize: " + corePoolSize + ", maxPoolSize: " + maxPoolSize);
    ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(queueSize);
    if (exchangeManager == null) {
      throw new RuntimeException("ExchangeManager must be set before calling afterPropertiesSet");
    }
    executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS, queue);
    executor.setThreadFactory(
        new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
        });
    executor.setRejectedExecutionHandler(new RejectedExecutionHandler());
    executor.prestartCoreThread();    
  }
  
  /**
   * Publishes a send file request on the thread pool
   * @param request the request
   * @param callback the callback that should be updated with progress information
   */
  @Override
  public void publish(SendFileRequest request, SendFileRequestCallback callback) {
    executor.execute(createRunnable(request, callback));
  }
  
  /**
   * Creates the runnable instance that should be placed in the executor queue
   * @param request the request
   * @param callback the callback that will get updates
   * @return the runnable
   */
  protected Runnable createRunnable(SendFileRequest request, SendFileRequestCallback callback) {
    PooledFileRequestTask task = new PooledFileRequestTask(request, callback, exchangeManager);
    return task;
  }
  
  /**
   * @return the max number of entries in the queue
   */
  public int getQueueSize() {
    return queueSize;
  }

  /**
   * @param queueSize the max number of entries in the queue
   */
  public void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
  }

  /**
   * @return the core thread executor pool size
   */
  public int getCorePoolSize() {
    return corePoolSize;
  }

  /**
   * @param corePoolSize the min thread executor pool size
   */
  public void setCorePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
  }

  /**
   * @return the max thread executor pool size
   */
  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  /**
   * @param maxPoolSize the max thread executor pool size
   */
  public void setMaxPoolSize(int maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
  }

  /**
   * @return the exchange manager
   */
  public IExchangeManager getExchangeManager() {
    return exchangeManager;
  }

  /**
   * @param exchangeManager the exchange manager
   */
  public void setExchangeManager(IExchangeManager exchangeManager) {
    this.exchangeManager = exchangeManager;
  }

}
