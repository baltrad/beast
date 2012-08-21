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
package eu.baltrad.beast.pgfwk.handlers;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.XmlRpcRequest;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import eu.baltrad.beast.adaptor.http.IHttpConnector;
import eu.baltrad.beast.message.IBltDataFrameMessageFactory;
import eu.baltrad.beast.message.mo.BltDataFrameMessage;
import eu.baltrad.beast.pgfwk.IGeneratorPlugin;

/**
 * @author Anders Henja
 */
public class BaltradXmlRpcGenerateHandler implements XmlRpcHandler, ApplicationContextAware {
  /**
   * The application context
   */
  private ApplicationContext context = null;
  
  /**
   * The connector
   */
  private IHttpConnector connector = null;
  
  /**
   * The factory for creating data frame messages
   */
  private IBltDataFrameMessageFactory factory = null;
  
  public BaltradXmlRpcGenerateHandler() {
  }
  
  /**
   * @see org.apache.xmlrpc.XmlRpcHandler#execute(org.apache.xmlrpc.XmlRpcRequest)
   */
  @Override
  public Object execute(XmlRpcRequest request) throws XmlRpcException {
    String algorithm = (String)request.getParameter(0);
    Object[] ofiles = (Object[])request.getParameter(1);
    Object[] oargs = (Object[])request.getParameter(2);
    
    String[] files = createStringArray(ofiles);
    String[] args = createStringArray(oargs);

    Object result = new Integer(-1);

    Object plugin = context.getBean(algorithm);

    if (plugin instanceof IGeneratorPlugin) {
      try {
        String output = ((IGeneratorPlugin)plugin).generate(algorithm, files, args);
        if (output != null) {
          BltDataFrameMessage message = factory.createMessage(output);
          connector.send(message);
        }
        result = new Integer(0);
      } catch (RuntimeException t) {
        t.printStackTrace();
      }
    }
    
    return result;
  }

  /**
   * Creates an array of strings from a Object array of strings.
   * @param arr the in array
   * @return the string array
   */
  protected String[] createStringArray(Object[] arr) {
    String[] result = new String[0];
    if (arr != null) {
      result = new String[arr.length];
      int index = 0;
      for (Object str : arr) {
        result[index++] = (String)str;
      }
    }
    return result;
  }
  
  /**
   * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
   */
  @Override
  public void setApplicationContext(ApplicationContext context)
      throws BeansException {
    this.context = context;
  }

  /**
   * @param connector the connector to set
   */
  public void setConnector(IHttpConnector connector) {
    this.connector = connector;
  }

  /**
   * @return the connector
   */
  public IHttpConnector getConnector() {
    return connector;
  }

  /**
   * @return the data frame message factory
   */
  public IBltDataFrameMessageFactory getDataFrameMessageFactory() {
    return factory;
  }

  /**
   * @param factory the data frame message factory
   */
  public void setDataFrameMessageFactory(IBltDataFrameMessageFactory factory) {
    this.factory = factory;
  }
}
