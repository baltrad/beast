/**
 * 
 */
package eu.baltrad.beast.admin.command_response;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.annotate.JsonRootName;

import eu.baltrad.beast.admin.CommandResponse;
import eu.baltrad.beast.admin.objects.Settings;

/**
 * @author anders
 *
 */
@JsonRootName("settings-response")
public class SettingCommandResponse  implements CommandResponse {
  private boolean status = false;
  
  /**
   * The settings
   */
  private Settings settings = new Settings();

  /**
   * Constructor
   */
  public SettingCommandResponse() {
  }

  /**
   * Constructor
   * @param status status of operation
   */
  public SettingCommandResponse(boolean status) {
    this.setStatus(status);
  }

  /**
   * If operation was successful or not
   */
  @Override
  public boolean wasSuccessful() {
    return this.status;
  }

  /**
   * @return the status
   */
  public boolean isStatus() {
    return status;
  }

  /**
   * @param status the status to set
   */
  public void setStatus(boolean status) {
    this.status = status;
  }
  
  /**
   * Adds a property to settings
   * @param field the field name
   * @param value the value
   */
  public void addProperty(String field, String value) {
    this.settings.addProperty(field, value);
  }
  
  /**
   * The settings
   * @return the settings
   */
  public Settings getSettings() {
    return this.settings;
  }
}
