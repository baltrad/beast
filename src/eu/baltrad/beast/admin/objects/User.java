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
package eu.baltrad.beast.admin.objects;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * User object used by the user command
 * @author anders
 */
@JsonRootName("user")
public class User {
  public final static String ROLE_ADMIN = "admin";
  public final static String ROLE_OPERATOR = "operator";
  public final static String ROLE_USER = "user";
  public final static String ROLE_PEER = "peer"; /* Not used by beast */
  public final static String ROLE_NODE = "node"; /* Not used by beast */
  
  /**
   * The user affected by command
   */
  private String name = null;
  
  /**
   * The role
   */
  private String role = null;
  
  /**
   * Password of user if required.
   */
  private String password = null;
  
  /**
   * New password if a password change is taking place
   */
  private String newpassword = null;

//  private String orgName;
//  private String orgUnit;
//  private String locality;
//  private String state;
//  private String countryCode;
//  private String nodeAddress;

  /**
   * Default constructor
   */
  public User() {
  }
  
  /**
   * Constructor
   * @param name the name of the user
   */
  public User(String name) {
    this.name = name;
  }
  
  /**
   * Constructor
   * @param name the name of the user
   * @param role the name of the role
   */
  public User(String name, String role) {
    this.name = name;
    this.role = role;
  }
  
  /**
   * @return the user
   */
  public String getName() {
    return name;
  }

  /**
   * @param user the user to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * @return the newpassword
   */
  @JsonProperty(value="new-password")
  public String getNewpassword() {
    return newpassword;
  }

  /**
   * @param newpassword the newpassword to set
   */
  @JsonProperty(value="new-password")
  public void setNewpassword(String newpassword) {
    this.newpassword = newpassword;
  }

  /**
   * @return the role
   */
  public String getRole() {
    return role;
  }

  /**
   * @param role the role to set
   */
  public void setRole(String role) {
    this.role = role;
  }
}
