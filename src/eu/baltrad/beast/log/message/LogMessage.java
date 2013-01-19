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

/**
 * @author Anders Henja
 */
public class LogMessage {
  /**
   * The module this message should exist for
   */
  private String module;
  
  /**
   * Message code
   */
  private String code;
  
  /**
   * The error message
   */
  private String message;
  
  /**
   * If it is possible to resolve this error, this might be the solution
   */
  private String solution;

  /**
   * The severity of this message
   */
  private MessageSeverity severity = MessageSeverity.UNDEFINED;
  
  /**
   * Default constructor
   */
  public LogMessage() {
    this(null, null);
  }

  /**
   * Constructor
   * @param module
   * @param code
   */
  public LogMessage(String module, String code) {
    this(module, code, null, MessageSeverity.UNDEFINED, null);
  }

  /**
   * Constructor
   * @param module the module
   * @param code the error code
   * @param message the error message
   */
  public LogMessage(String module, String code, String message) {
    this(module, code, message, MessageSeverity.UNDEFINED, null);
  }

  /**
   * Constructor
   * @param module the module
   * @param code the error code
   * @param message the error message
   * @param severity the severity
   */
  public LogMessage(String module, String code, String message, MessageSeverity severity) {
    this(module, code, message, severity, null);
  }  
  
  /**
   * Constructor
   * @param module the module
   * @param code the error code
   * @param message the error message
   * @param severity the severity
   * @param solution the solution for this error
   */
  public LogMessage(String module, String code, String message, MessageSeverity severity, String solution) {
    this.module = module;
    this.code = code;
    this.message = message;
    setSeverity(severity);
    this.solution = solution;
  }
  
  /**
   * @return the code
   */
  public String getCode() {
    return code;
  }

  /**
   * @param code the code to set
   */
  public void setCode(String code) {
    this.code = code;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Returns the formatted message string with the provided arguments
   * @param args the arguments
   * @return the formatted message
   */
  public String getFormattedMessage(Object... args) {
    return String.format(getMessage(), args);
  }
  
  /**
   * @param message the message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }
  
  /**
   * @return the solution
   */
  public String getSolution() {
    return solution;
  }

  /**
   * @param solution the solution to set
   */
  public void setSolution(String solution) {
    this.solution = solution;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  public MessageSeverity getSeverity() {
    return severity;
  }

  public void setSeverity(MessageSeverity severity) {
    if (severity == null) {
      throw new IllegalArgumentException("Must be a valid severity");
    }
    this.severity = severity;
  }
}
