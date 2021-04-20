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
package eu.baltrad.beast.admin;

/**
 * Interface for all command objects.
 * @author anders
 */
public abstract class Command {
  private String rawMessage = null;
  
  /**
   * @return the name of the operation affecting this command
   */
  public abstract String getOperation();
  
  /**
   * Validates if the current command is valid according to operation and content and should be accepted 
   * @return true if valid
   */
  public abstract boolean validate();
  
  /**
   * Sets the RAW message used to populate the command
   * @param s the raw message
   */
  public void setRawMessage(String s) {
    rawMessage = s;
  }
  
  /**
   * @return the RAW message used to populate the command
   */
  public String getRawMessage() {
    return rawMessage;
  }
}
