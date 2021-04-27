/**
 * 
 */
package eu.baltrad.beast.admin.command;

import eu.baltrad.beast.admin.Command;
import eu.baltrad.beast.admin.objects.Settings;

/**
 * @author anders
 *
 */
public class SettingCommand extends Command {
  public final static String UPDATE_SETTINGS = "update_settings";

  public final static String LIST = "list_settings";
  
  public final static String IMPORT = "import_settings";
  
  public final static String EXPORT = "export_settings";
  
  /**
   * The operation
   */
  private String operation = null;

  /**
   * Settings
   */
  private Settings settings = new Settings();
  
  /**
   * Default constructor
   */
  public SettingCommand() {
  }
  
  /**
   * Constructor
   */
  public SettingCommand(String operation) {
    setOperation(operation);
  }

  /**
   * Constructor
   */
  public SettingCommand(String operation, Settings settings) {
    setOperation(operation);
    setSettings(settings);
  }

  /**
   * @see Command#getOperation()
   */
  @Override
  public String getOperation() {
    return this.operation;
  }

  /**
   * @see Command#validate()
   */
  @Override
  public boolean validate() {
    if (getOperation() != null) {
      if (getOperation().equals(SettingCommand.UPDATE_SETTINGS) ||
          getOperation().equals(SettingCommand.LIST) ||
          getOperation().equals(SettingCommand.IMPORT) ||
          getOperation().equals(SettingCommand.EXPORT)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param operation the operation to set
   */
  public void setOperation(String operation) {
    this.operation = operation;
  }

  /**
   * Sets a full map
   * @param map the map to set
   */
  public void setSettings(Settings settings)  {
    if (settings != null) {
      this.settings = settings;
    } else {
      this.settings = new Settings();
    }
  }

  /**
   * Sets a full map
   * @param map the map to set
   */
  public Settings getSettings()  {
    return this.settings;
  }

  /**
   * Return if setting exists or not
   * @param name of setting
   * @return true if setting exists
   */
  public boolean hasSetting(String name) {
    return this.settings.hasProperty(name);
  }
  
  /**
   * Returns the setting
   * @param name the name of the setting
   * @return the value
   */
  public String getSetting(String name) {
    return this.settings.getProperty(name).toString();
  }
  
  /**
   * Return the setting or default value
   * @param name the name of the setting
   * @param defaultValue the value to return if no matching setting found
   * @return the value
   */
  public String getSetting(String name, String defaultValue) {
    if (!this.settings.hasProperty(name)) {
      return defaultValue;
    }
    return this.settings.getProperty(name);
  }
}
