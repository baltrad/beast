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
import eu.baltrad.beast.admin.objects.User;

/**
 * @author anders
 */
public class UserCommand extends Command {
  /**
   * Change password for a user
   */
  public final static String CHANGE_PASSWORD = "change_password";

  /**
   * Returns a list of available users.
   */
  public final static String LIST = "list_users";
  
  /**
   * The operation
   */
  private String operation = null;
  
  /**
   * The user object
   */
  private User user = null;

  /**
   * Default constructor
   */
  public UserCommand() {
  }
  
  /**
   * Constructor
   * @param operation the operation
   */
  public UserCommand(String operation) {
    setOperation(operation);
  }
  
  public UserCommand(String operation, User user) {
    setOperation(operation);
    setUser(user);
  }
  
  /**
   * @param operation the operation to set
   */
  public void setOperation(String operation) {
    this.operation = operation;
  }
  
  /**
   * @returns the operation
   */
  @Override
  public String getOperation() {
    return this.operation;
  }

  /**
   * @return the user
   */
  public User getUser() {
    return user;
  }

  /**
   * @param user the user to set
   */
  public void setUser(User user) {
    this.user = user;
  }
}
