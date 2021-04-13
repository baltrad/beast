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
package eu.baltrad.beast.admin.command_response;

import eu.baltrad.beast.admin.CommandResponse;

/**
 * Used when returning a command response containing a json object
 * @author anders
 */
public class CommandResponseJsonObject implements CommandResponse {
  private boolean status = false;
  private String jsonString = null;
  
  /**
   * Constructor
   * @param status the status
   */
  public CommandResponseJsonObject(boolean status) {
    this.status = status;
  }

  /**
   * Constructor
   * @param status the status
   * @param jsonString the object as a json string
   */
  public CommandResponseJsonObject(boolean status, String jsonString) {
    this.status = status;
    setJsonString(jsonString);
  }

  /**
   * Constructor. Sets status to true.
   * @param jsonString the object as a json string
   */
  public CommandResponseJsonObject(String jsonString) {
    this.status = true;
    setJsonString(jsonString);
  }

  /**
   * @see CommandResponse#wasSuccessful()
   */
  @Override
  public boolean wasSuccessful() {
    return status;
  }

  /**
   * @return the jsonString
   */
  public String getJsonString() {
    return jsonString;
  }

  /**
   * @param jsonString the jsonString to set
   */
  public void setJsonString(String jsonString) {
    this.jsonString = jsonString;
  }

  
}
