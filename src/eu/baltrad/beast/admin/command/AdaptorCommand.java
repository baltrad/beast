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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
  public final static String EXPORT = "export_adaptors";
  public final static String IMPORT = "import_adaptors";

  /**
   * Operation
   */
  private String operation = null;
  
  /**
   * Adaptor object
   */
  private Adaptor adaptor = null;
  
  /**
   * Imported adaptors
   */
  private List<Adaptor> importedAdaptors = new ArrayList<Adaptor>();
  
  /**
   * If all adaptors should be removed before importing the data.
   */
  private boolean clearAllBeforeImport = false;
  
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
   * @see Command#validate()
   */
  @Override
  public boolean validate() {
    try {
      if (LIST.equals(operation) || EXPORT.equalsIgnoreCase(operation)) {
        return true;
      } else if (adaptor != null && (GET.equals(operation) || REMOVE.equals(operation))) {
        if (adaptor.getName() != null && !adaptor.getName().isEmpty()) {
          return true;
        }
      } else if (adaptor != null && (ADD.equals(operation) || UPDATE.equals(operation))) {
        if ((adaptor.getName() != null && !adaptor.getName().isEmpty()) &&
            (adaptor.getType() != null && !adaptor.getType().isEmpty()) &&
            validateUri(adaptor.getUri()) &&
            adaptor.getTimeout() >= 0) {
          return true;
        }
      } else if (IMPORT.equalsIgnoreCase(operation)) {
        for (Adaptor adaptor : importedAdaptors) {
          if (adaptor.getName() == null || adaptor.getName().isEmpty() ||
              adaptor.getType() == null || adaptor.getType().isEmpty() ||
              !validateUri(adaptor.getUri()) ||
              adaptor.getTimeout() < 0) {
            return false;
          }
        }
        return true;
      }
    } catch (Exception e) {
      // pass
    }
    return false;
  }

  protected boolean validateUri(String uri) {
    try {
      URI.create(uri);
      return true;
    } catch (Exception e) {
      return false;
    }
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

  /**
   * @return the importedAdaptors
   */
  public List<Adaptor> getImportedAdaptors() {
    return importedAdaptors;
  }

  /**
   * @param importedAdaptors the importedAdaptors to set
   */
  public void setImportedAdaptors(List<Adaptor> importedAdaptors) {
    this.importedAdaptors = importedAdaptors;
  }

  /**
   * @return the clearAllBeforeImport
   */
  public boolean isClearAllBeforeImport() {
    return clearAllBeforeImport;
  }

  /**
   * @param clearAllBeforeImport the clearAllBeforeImport to set
   */
  public void setClearAllBeforeImport(boolean clearAllBeforeImport) {
    this.clearAllBeforeImport = clearAllBeforeImport;
  }
}
