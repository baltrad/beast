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
import eu.baltrad.beast.admin.objects.Adaptor;

/**
 * Commands affecting adaptors
 * @author anders
 */
public class AdaptorCommand extends Command {
  public final static String ADD = "add_adaptor";
  public final static String UPDATE = "update_adaptor";
  public final static String REMOVE = "remove_adaptor";
  public final static String GET = "get_adaptor";
  public final static String LIST = "list_adaptors";
  
  /**
   * Operation
   */
  private String operation = null;
  
  /**
   * Adaptor object
   */
  private Adaptor adaptor = null;
  
  /**
   * Constructor
   */
  public AdaptorCommand() {
  }

  /**
   * Constructor
   * @param operation the operation
   */
  public AdaptorCommand(String operation) {
    setOperation(operation);
  }

  /**
   * Constructor
   * @param operation the operation
   * @param adaptor the adaptor affected
   */
  public AdaptorCommand(String operation, Adaptor adaptor) {
    setOperation(operation);
    setAdaptor(adaptor);
  }

  /**
   * @return the operation
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
   * @return the adaptor
   */
  public Adaptor getAdaptor() {
    return adaptor;
  }

  /**
   * @param adaptor the adaptor to set
   */
  public void setAdaptor(Adaptor adaptor) {
    this.adaptor = adaptor;
  }

}
