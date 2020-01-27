/**
 * 
 */
package eu.baltrad.beast.exchange;

/**
 * Task used when posting asynchronous send file requests
 * @author anders
 */
public class PooledFileRequestTask implements Runnable {
  private SendFileRequest request;
  private SendFileRequestCallback callback;
  private IExchangeManager exchangeManager;
  
  /**
   * Constructor
   * @param request request
   * @param callback callback
   */
  public PooledFileRequestTask(SendFileRequest request, SendFileRequestCallback callback, IExchangeManager exchangeManager) {
    this.setRequest(request);
    this.setCallback(callback);
    this.setExchangeManager(exchangeManager);
  }
  
  /**
   * Executes the send
   */
  @Override
  public void run() {
    try {
      exchangeManager.send(request, callback);
    } catch (ExchangeStatusException e) {
    }
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

  /**
   * @return the callback
   */
  public SendFileRequestCallback getCallback() {
    return callback;
  }

  /**
   * @param callback the callback
   */
  public void setCallback(SendFileRequestCallback callback) {
    this.callback = callback;
  }

  /**
   * @return the request
   */
  public SendFileRequest getRequest() {
    return request;
  }

  /**
   * @param request the request
   */
  public void setRequest(SendFileRequest request) {
    this.request = request;
  }
}
