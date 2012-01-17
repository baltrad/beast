/* --------------------------------------------------------------------
Copyright (C) 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.log;

import eu.baltrad.beast.manager.IBltMessageManager;
import eu.baltrad.beast.message.mo.BltAlertMessage;

/**
 * @author Anders Henja
 */
public class AlertMessageReporter implements ISystemReporter {
  /**
   * The blt message manager
   */
  private IBltMessageManager messageManager = null;

  /**
   * The log message repository
   */
  private ILogMessageRepository repository = null;
  
  /**
   * This module name
   */
  private String module = null;
  
  /**
   * Default constructor. Will set module id to BEAST
   */
  public AlertMessageReporter() {
    module = "BEAST";
  }
  
  /**
   * Constructor
   * @param module
   */
  public AlertMessageReporter(String module) {
    this.module = module;
  }
  
  /**
   * @param manager the manager to set
   */
  public void setMessageManager(IBltMessageManager manager) {
    this.messageManager = manager;
  }
  
  /**
   * @param repository the log message repository to set
   */
  public void setMessageRepository(ILogMessageRepository repository) {
    this.repository = repository;
  }
  
  /**
   * @see eu.baltrad.beast.log.ISystemReporter#info(java.lang.String)
   */
  @Override
  public void info(String message) {
    BltAlertMessage bltmsg = createAlert(module, BltAlertMessage.INFO, "XXXXX", message);
    messageManager.manage(bltmsg);
  }

  /**
   * @see eu.baltrad.beast.log.ISystemReporter#info(java.lang.String, java.lang.String, java.lang.Object[])
   */
  @Override
  public void info(String code, String message, Object... args) {
    String strmsg = getMessage(module, code, message, args);
    BltAlertMessage bltmsg = createAlert(module, BltAlertMessage.INFO, code, strmsg);
    messageManager.manage(bltmsg);
  }

  /**
   * @see eu.baltrad.beast.log.ISystemReporter#warn(java.lang.String, java.lang.String, java.lang.Object[])
   */
  @Override
  public void warn(String code, String message, Object... args) {
    String strmsg = getMessage(module, code, message, args);
    BltAlertMessage bltmsg = createAlert(module, BltAlertMessage.WARNING, code, strmsg);
    messageManager.manage(bltmsg);
  }

  /**
   * @see eu.baltrad.beast.log.ISystemReporter#error(java.lang.String, java.lang.String, java.lang.Object[])
   */
  @Override
  public void error(String code, String message, Object... args) {
    String strmsg = getMessage(module, code, message, args);
    BltAlertMessage bltmsg = createAlert(module, BltAlertMessage.ERROR, code, strmsg);
    messageManager.manage(bltmsg);
  }

  /**
   * @see eu.baltrad.beast.log.ISystemReporter#fatal(java.lang.String, java.lang.String, java.lang.Object[])
   */
  @Override
  public void fatal(String code, String message, Object... args) {
    String strmsg = getMessage(module, code, message, args);
    BltAlertMessage bltmsg = createAlert(module, BltAlertMessage.FATAL, code, strmsg);
    messageManager.manage(bltmsg);
  }
  
  /**
   * Creates a blt alert message
   * @param module the module
   * @param severity the severity
   * @param code the error code
   * @param message the error message
   * @return the alert
   */
  protected BltAlertMessage createAlert(String module, String severity, String code, String message) {
    BltAlertMessage result = new BltAlertMessage();
    result.setModule(module);
    result.setSeverity(severity);
    result.setCode(code);
    result.setMessage(message);
    return result;
  }
  
  /**
   * Returns the localized message if any could be found.
   * @param module - the specific module
   * @param code - the error code
   * @param message - the error message
   */
  protected String getMessage(String module, String code, String message, Object... args) {
    String msg = null;
    if (repository != null) {
      msg = repository.getMessage(module, code, message, args);
    } else {
      msg = String.format(message, args);
    }
    return msg;
  }
}
