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

import java.util.ArrayList;
import java.util.List;

import eu.baltrad.beast.admin.Command;
import eu.baltrad.beast.scheduler.CronEntry;
import eu.baltrad.beast.scheduler.CronEntryUtilities;

/**
 * @author anders
 */
public class ScheduleCommand extends Command {
  public final static String ADD = "add_schedule";
  public final static String UPDATE = "update_schedule";
  public final static String REMOVE = "remove_schedule";
  public final static String GET = "get_schedule";
  public final static String LIST = "list_schedule";
  public final static String IMPORT = "import_schedule";
  public final static String EXPORT = "export_schedule";
  
  /**
   * The operation
   */
  private String operation = null;

  /**
   * A single entry for add/update/remove ...
   */
  private CronEntry entry = null;
  
  /**
   * When importing cron entries
   */
  private List<CronEntry> importedEntries = new ArrayList<CronEntry>();

  /**
   * If all scheduled entries should be removed before importing the data.
   */
  private boolean clearAllBeforeImport = false;

  /**
   * Constructor
   */
  public ScheduleCommand() {
  }
  
  /**
   * Constructor
   * @param operation -the operation
   */
  public ScheduleCommand(String operation) {
    setOperation(operation);
  }

  /**
   * Constructor
   */
  public ScheduleCommand(String operation, CronEntry entry) {
    setOperation(operation);
    setEntry(entry);
  }

  /**
   * @return the operation
   */
  @Override
  public String getOperation() {
    return operation;
  }
  
  /**
   * @param operation the operation
   */
  public void setOperation(String operation) {
    this.operation = operation;
  }

  /**
   * @see Command#validate()
   */
  @Override
  public boolean validate() {
    if (ADD.equalsIgnoreCase(operation) && entry != null) {
      if (entry.getName() != null && !entry.getName().isEmpty() &&
          validateExpression(entry.getExpression())) {
        return true;
      }
    } else if (UPDATE.equalsIgnoreCase(operation) && entry != null) {
      if (entry.getName() != null && !entry.getName().isEmpty() &&
          validateExpression(entry.getExpression()) &&
          entry.getId() > 0) {
        return true;
      }
    } else if (REMOVE.equalsIgnoreCase(operation) && entry != null) {
      if (entry.getId() > 0) {
        return true;
      }
    } else if (GET.equalsIgnoreCase(operation) && entry != null) {
      if (entry.getId() > 0 || (entry.getName() != null && !entry.getName().isEmpty())) {
        return true;
      }
    } else if (LIST.equalsIgnoreCase(operation) || EXPORT.equalsIgnoreCase(operation)) {
      return true;
    } else if (IMPORT.equalsIgnoreCase(operation)) {
      for (CronEntry entry : importedEntries) {
        if (entry.getName() == null || entry.getName().isEmpty() ||
            !validateExpression(entry.getExpression())) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Validates the cron expression
   * @return true if expression is valid
   */
  protected boolean validateExpression(String expression) {
    try {
      CronEntryUtilities.validateExpression(expression);
      return true;
    } catch (Exception e) {
      // pass
    }
    return false;
  }

  /**
   * @return the entry
   */
  public CronEntry getEntry() {
    return entry;
  }

  /**
   * @param entry the entry to set
   */
  public void setEntry(CronEntry entry) {
    this.entry = entry;
  }

  /**
   * @return the entries
   */
  public List<CronEntry> getImportedEntries() {
    return importedEntries;
  }

  /**
   * @param entries the entries to set
   */
  public void setImportedEntries(List<CronEntry> entries) {
    this.importedEntries = entries;
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
