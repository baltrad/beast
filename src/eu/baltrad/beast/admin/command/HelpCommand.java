/* --------------------------------------------------------------------
Copyright (C) 2009-2021 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.admin.command;

import eu.baltrad.beast.admin.Command;

/**
 * Command providing help.
 * @author anders
 */
public class HelpCommand extends Command {
  public final static String HELP = "help";
  
  /**
   * The operation
   */
  private String operation;

  /**
   * If return help about specified command
   */
  private String command;
  
  /**
   * Constructor
   */
  public HelpCommand() {
  }

  /**
   * Constructor
   * @param operation the operation
   */
  public HelpCommand(String operation) {
    setOperation(operation);
  }
  
  /**
   * @see Command#getOperation()
   */
  @Override
  public String getOperation() {
    return operation;
  }

  /**
   * @param operation the operation to set
   */
  public void setOperation(String operation) {
    this.operation = operation;
  }

  /**
   * @return the command
   */
  public String getCommand() {
    return command;
  }

  /**
   * @param command the command to set
   */
  public void setCommand(String command) {
    this.command = command;
  }
}
