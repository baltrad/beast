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
package eu.baltrad.beast.adaptor.xmlrpc;

import java.net.URL;

import org.apache.xmlrpc.client.TimingOutCallback;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import eu.baltrad.beast.adaptor.AdaptorAddressException;
import eu.baltrad.beast.adaptor.AdaptorException;
import eu.baltrad.beast.adaptor.IAdaptor;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.router.Route;

/**
 * The XMLRPC adaptor
 * @author Anders Henja
 */
public class XmlRpcAdaptor implements IAdaptor {
  /**
   * The xml rpc client.
   */
  private XmlRpcClient client = null;
  
  /**
   * The generator for converting Baltrad messages into xml commands
   */
  private IXmlRpcCommandGenerator generator = null;
  
  /**
   * The time to wait for a response before timing out, default is 5 seconds
   */
  private long timeout = 5000;
  
  /**
   * The name of this adaptor
   */
  private String name = null;
  
  /**
   * The callback
   */
  private IXmlRpcCallback callback = null;
  
  /**
   * Default constructor
   */
  public XmlRpcAdaptor() {
    client = new XmlRpcClient();
  }
  
  /**
   * Sets the command generator
   * @param generator - the generator to set
   */
  public void setGenerator(IXmlRpcCommandGenerator generator) {
    this.generator = generator;
  }
  
  /**
   * Sets the xml rpc client to use. Used for testing.
   * @param client
   */
  void setRpcClient(XmlRpcClient client) {
    this.client = client;
  }
  
  /**
   * Sets the timeout
   * @param timeout - the timeout
   */
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }
  
  /**
   * Returns the timeout
   * @return the timeout
   */
  public long getTimeout() {
    return this.timeout;
  }
  
  /**
   * Sets the callback to use
   * @param cb the callback
   */
  public void setCallback(IXmlRpcCallback cb) {
    this.callback = cb;
  }
  
  /**
   * The destination url for the rpc server.
   * @param url - the url
   */
  public void setURL(String url) {
    try {
      XmlRpcClientConfigImpl config = createConfig();
      config.setServerURL(new URL(url));
      client.setConfig(config);
    } catch (Throwable t) {
      throw new AdaptorAddressException("Failed to set url", t);
    }
  }
  
  /**
   * Sets the name of this adaptor
   * @param name the name of this adaptor
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * @see IAdaptor#getName()
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IAdaptor#handle(eu.baltrad.beast.router.Route)
   */
  @Override
  public void handle(Route route) {
    try {
      IBltMessage message = route.getMessage();
      XmlRpcCommand command = generator.generate(message);
      TimingOutCallback tcb = createTimeout(timeout);
      client.executeAsync(command.getMethod(), command.getObjects(), tcb);
      try {
        Object result = tcb.waitForResponse();
        if (callback != null) {
          callback.success(message, result);
        }
      } catch (TimingOutCallback.TimeoutException e) {
        if (callback != null) {
          callback.timeout(message);
        }
      } catch (Throwable t) {
        if (callback != null) {
          callback.error(message, t);
        }
      }
    } catch (Throwable t) {
      throw new AdaptorException("Failed to execute command", t);
    }
  }
  
  /**
   * Creates a new instance of the XmlRpcClientConfig
   * @return the xml rpc config
   */
  protected XmlRpcClientConfigImpl createConfig() {
    return new XmlRpcClientConfigImpl();
  }
  
  /**
   * Creates an url from an url.
   * @param url the url
   * @return the url
   * @throws AdaptorAddressException if there is something wrong with the url
   */
  protected URL createUrl(String url) {
    try {
      return new URL(url);
    } catch (Throwable t) {
      throw new AdaptorAddressException("bad url", t);
    }
  }
  
  /**
   * Returns a timing callback
   * @param timeout the timeout in ms
   * @return the timing callback
   */
  protected TimingOutCallback createTimeout(long timeout) {
    return new TimingOutCallback(timeout);
  }
}
