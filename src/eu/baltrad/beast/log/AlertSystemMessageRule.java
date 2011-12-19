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

import org.apache.log4j.Logger;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.rules.IRule;

/**
 *
 * @author Anders Henja
 * @date Dec 19, 2011
 */
public class AlertSystemMessageRule implements IRule {
  /**
   * The type of rule
   */
  public static final String TYPE = "system_alert";
  
  /**
   * The repository
   */
  private ILogMessageRepository repository = null;
  
  /**
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    if (message instanceof BltAlertMessage) {
      BltAlertMessage bltmsg = (BltAlertMessage)message;
      String severity = bltmsg.getSeverity();
      String msg = getMessage(bltmsg.getModule(), bltmsg.getCode(), bltmsg.getMessage());
      Logger logger = getLogger(bltmsg.getModule());
      if (severity == null) {
        severity = BltAlertMessage.INFO;
      }
      if (severity.equals(BltAlertMessage.INFO)) {
        logger.info(String.format("I%s: %s", bltmsg.getCode(), msg));
      } else if (severity.equals(BltAlertMessage.WARNING)) {
        logger.warn(String.format("W%s: %s", bltmsg.getCode(), msg));
      } else if (severity.equals(BltAlertMessage.ERROR)) {
        logger.error(String.format("E%s: %s", bltmsg.getCode(), msg));
      } else if (severity.equals(BltAlertMessage.FATAL)) {
        logger.fatal(String.format("F%s: %s", bltmsg.getCode(), msg));
      } else {
        logger.fatal(String.format("X%s: %s", bltmsg.getCode(), msg));
      }
    }
    return null;
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#getType()
   */
  @Override
  public String getType() {
    return TYPE;
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#isValid()
   */
  @Override
  public boolean isValid() {
    return true;
  }


  /**
   * @param repository the repository to set
   */
  public void setRepository(ILogMessageRepository repository) {
    this.repository = repository;
  }

  /**
   * @return the repository
   */
  public ILogMessageRepository getRepository() {
    return repository;
  }
  
  /**
   * Returns the logger with specified logname
   * @param logname the name of the logger
   * @return the logger
   */
  protected Logger getLogger(String logname) {
    return Logger.getLogger(logname);
  }
  
  /**
   * Returns the localized message if any could be found.
   * @param[in] module - the specific module
   * @param[in] code - the error code
   * @param[in] message - the error message
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
