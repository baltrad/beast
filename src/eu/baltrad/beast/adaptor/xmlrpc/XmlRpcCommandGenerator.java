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

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.message.mo.BltCommandMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;

/**
 * @author Anders Henja
 *
 */
public class XmlRpcCommandGenerator implements IXmlRpcCommandGenerator {
  /**
   * @see IXmlRpcCommandGenerator#generate(IBltMessage)
   */
  public XmlRpcCommand generate(IBltMessage message) {
    if (message.getClass() == BltCommandMessage.class) {
      return createCommand((BltCommandMessage)message);
    } else if (message.getClass() == BltAlertMessage.class) {
      return createCommand((BltAlertMessage)message);
    } else if (message.getClass() == BltGenerateMessage.class) {
      return createCommand((BltGenerateMessage)message);
    } else if (message.getClass() == BltTriggerJobMessage.class) {
      return createCommand((BltTriggerJobMessage)message);
    }
    throw new XmlRpcCommandException("Can not handle message of type: " + message.getClass().getName());
  }
  
  /**
   * Creates an XmlRpcCommand for sending a command message.
   * @param message the message
   * @return the command
   */
  protected XmlRpcCommand createCommand(BltCommandMessage message) {
    String cmd = message.getCommand();
    Object[] objects = new Object[]{cmd};
    XmlRpcCommand command = new XmlRpcCommand();
    command.setMethod("execute");
    command.setObjects(objects);
    return command;    
  }
  
  /**
   * Creates an XmlRpcCommand for sending an alert message.
   * @param message the message
   * @return the command
   */
  protected XmlRpcCommand createCommand(BltAlertMessage message) {
    String code = message.getCode();
    String msg = message.getMessage();
    Object[] objects = new Object[]{code,msg};
    XmlRpcCommand command = new XmlRpcCommand();
    command.setMethod("alert");
    command.setObjects(objects);
    return command;    
  }
  
  /**
   * Creates an XmlRpcCommand for sending a generate message.
   * @param message the message
   * @return the command
   */
  protected XmlRpcCommand createCommand(BltGenerateMessage message) {
    String algorithm = message.getAlgorithm();
    String[] files = message.getFiles();
    String[] arguments = message.getArguments();
    Object[] objects = new Object[]{algorithm,files,arguments};
    XmlRpcCommand command = new XmlRpcCommand();
    command.setMethod("generate");
    command.setObjects(objects);
    
    return command;    
  }
  
  /**
   * Creates an XmlRpcCommand for sending a trigger job message.
   * @param message the message
   * @return the command
   */
  protected XmlRpcCommand createCommand(BltTriggerJobMessage message) {
    String id = message.getId();
    String name = message.getName();
    String[] args = message.getArgs();
    Object[] objects = new Object[]{id,name,args};
    XmlRpcCommand command = new XmlRpcCommand();
    command.setMethod("triggerjob");
    command.setObjects(objects);
    return command;    
  }
}
