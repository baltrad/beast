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
package eu.baltrad.beast.log.message;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ResourceUtils;

/**
 * @author Anders Henja
 */
public class LogMessageRepository implements ILogMessageRepository, InitializingBean {
  /**
   * The filename containing this repository.
   */
  private String[] filenames = new String[0];
  
  /**
   * The actual messages and modules.
   * First key points out module. /log-messages/module.
   * Second key points at the error code.
   */
  private Map<String, LogMessage> messages = null;

  /**
   * Default constructor
   */
  public LogMessageRepository() {
    messages = new HashMap<String,LogMessage>();
  }
  
  /**
   * Constructor
   * @param filenames a list of filenames containing log messages
   */
  public LogMessageRepository(String[] filenames) {
    this.filenames = filenames;
  }
  
  /**
   * Constructor
   * @param messages the log messages
   */
  public LogMessageRepository(Map<String, LogMessage> messages) {
    this.messages = messages;
  }
  
  /**
   * Returns the message for this error code
   * @param module the module
   * @param ecode the error code
   * @return the log message
   */
  @Override
  public synchronized LogMessage getMessage(String ecode) {
    if (messages.containsKey(ecode)) {
      return messages.get(ecode);
    }
    return null;
  }
  
  @Override
  public synchronized String getModule(String ecode) {
    if (messages.containsKey(ecode)) {
      return messages.get(ecode).getModule();
    }
    return null;
  }
  
  /**
   * Returns all messages belonging to a specific module type
   * @param module the module name
   * @return the messages
   */
  @Override
  public synchronized List<LogMessage> getModuleMessages() {
    List<LogMessage> result = new ArrayList<LogMessage>();
    result.addAll(messages.values());
    Collections.sort(result, new Comparator<LogMessage>() {
      @Override
      public int compare(LogMessage o1, LogMessage o2) {
        String code1 = o1.getCode();
        String code2 = o2.getCode();
        return code1.compareTo(code2);
      }
    });
    return result;
  }
  
  /**
   * @see eu.baltrad.beast.log.ILogMessageRepository#getMessage(String, String, String, Object...)
   */
  @Override
  public String getMessage(String code, String message, Object... args) {
    String msg = null;
    LogMessage logmsg = getMessage(code);
    if (logmsg != null) {
      try {
        msg = logmsg.getFormattedMessage(args);
      } catch (Exception e) {
        // let default message be used instead and that one should not fail
      }
    }
   
    if (msg == null) {
      msg = String.format(message, args);
    }
    
    return msg;
  }  
  
  /**
   * Adds a log message to this repository
   * @param message the message to add
   */
  public synchronized void add(LogMessage message) {
    if (message.getModule() != null && message.getCode() != null) {
      messages.put(message.getCode(), message);
    } else {
      throw new IllegalArgumentException("No module or error code specified for logmessage");
    }
  }
  
  /**
   * Removes the log message with specified error code
   * @param ecode the error code
   */
  public synchronized void remove(String ecode) {
    if (messages.containsKey(ecode)) {
      messages.remove(ecode);
    }
  }

  /**
   * @return the filename
   */
  public String[] getFilenames() {
    return filenames;
  }

  /**
   * @param filenames the filenames to set
   */
  public void setFilenames(String[] filenames) {
    if (filenames != null) {
      this.filenames = filenames;
    } else {
      this.filenames = new String[0];
    }
  }

  /**
   * Loads a message file
   * @param filename the filename
   */
  protected synchronized void load(String filename) {
    try {
      File f = ResourceUtils.getFile(filename);
      SAXReader reader = new SAXReader();
      Document doc = reader.read(f);
      
      XPath xpathSelector = DocumentHelper.createXPath("//log-messages/message");
      @SuppressWarnings("unchecked")
      List<Element> nodes = xpathSelector.selectNodes(doc);
      for (Element e : nodes) {
        String module = e.attribute("module").getText();
        String code = e.attribute("id").getText();
        MessageSeverity severity = MessageSeverity.UNDEFINED;
        if (e.attribute("severity") != null) {
          severity = strToSeverity(e.attribute("severity").getText());
        }
        String txt = e.elementText("text");
        String solution = e.elementText("solution");
        LogMessage msg = new LogMessage(module, code, txt, severity, solution);
        messages.put(code, msg);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Loads several message files
   * @param filenames the filenames
   */
  protected synchronized void load(String[] filenames) {
    if (filenames != null) {
      for (String f : filenames) {
        load(f);
      }
    }
  }

  protected MessageSeverity strToSeverity(String str) {
    return MessageSeverity.valueOf(str);
  }
  
  /**
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public synchronized void afterPropertiesSet() throws Exception {
    load(filenames);
  }
}
