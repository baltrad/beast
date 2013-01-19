/* --------------------------------------------------------------------
Copyright (C) 2009-2013 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.system;

import java.util.Date;

import eu.baltrad.beast.log.message.MessageSeverity;

/**
 * System message. 
 * @author Anders Henja
 */
public class SystemMessage {
  private Date reportDate;
  private String module;
  private String code;
  private String message;
  private MessageSeverity severity;
  private String solution;
  
  /**
   * Default constructor
   */
  public SystemMessage() {
    reportDate = new Date();
  }
  
  /**
   * @return the report date
   */
  public Date getReportDate() {
    return reportDate;
  }
  
  /**
   * @param reportDate the report date to set
   * @throws IllegalArgumentException if reportDate == null
   */
  public void setReportDate(Date reportDate) {
    if (reportDate == null) {
      throw new IllegalArgumentException();
    }
    this.reportDate = reportDate;
  }

  /**
   * @return the module name
   */
  public String getModule() {
    return module;
  }

  /**
   * @param module the module name to set
   */
  public void setModule(String module) {
    this.module = module;
  }
  
  
  /**
   * @return the error code
   */
  public String getCode() {
    return code;
  }
  
  /**
   * @param code the error code to set
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
   * @param message the message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * @return the severity
   */
  public MessageSeverity getSeverity() {
    return severity;
  }

  /**
   * @param severity the severity to set
   */
  public void setSeverity(MessageSeverity severity) {
    this.severity = severity;
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
}
