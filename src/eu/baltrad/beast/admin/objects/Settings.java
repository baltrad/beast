/**
 * 
 */
package eu.baltrad.beast.admin.objects;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * @author anders
 */
@JsonRootName("settings")
public class Settings  {
  /**
   * Status messages for various fields
   */
  private Map<String, String> properties = new HashMap<String, String>();

  /**
   * Constructor
   */
  public Settings() {
    
  }
  
  /**
   * @return the fieldStatus
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * @param fieldStatus the fieldStatus to set
   */
  public void setProperties(Map<String, String> fields) {
    if (fields == null) {
      this.properties = new HashMap<String, String>();
    } else {
      this.properties = fields;
    }
  }
  
  /**
   * Adds a field
   * @param field
   * @param description
   */
  public void addProperty(String field, String value) {
    this.properties.put(field, value);
  }

  public boolean hasProperty(String field) {
    return this.properties.containsKey(field);
  }
  
  /**
   * Returns the property with name
   * @param field the name of the field
   * @return the value
   */
  public String getProperty(String field) {
    return this.properties.get(field);
  }
  
  /**
   * Returns description for field
   * @param field the field
   * @return the description
   */
  public String getPropertyAsString(String field) {
    return (String) this.properties.get(field).toString();
  }
  
  /**
   * Returns the value as an integer
   * @param defaultValue
   * @return
   */
  public int getPropertyAsInt(String name, int defaultValue) {
    try {
      return Integer.parseInt(this.properties.get(name));
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
  
  /**
   * Returns the value as an integer
   * @param defaultValue
   * @return
   */
  public long getPropertyAsLong(String name, long defaultValue) {
    try {
      return Long.parseLong(this.properties.get(name));
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
  
  /**
   * Returns the value as an integer
   * @param defaultValue
   * @return
   */
  public double getPropertyAsDouble(String name, double defaultValue) {
    try {
      return Double.parseDouble(this.properties.get(name));
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
  
  public boolean getPropertyAsBoolean(String name, boolean defaultValue) {
    try {
      return Boolean.parseBoolean(this.properties.get(name));
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
  
}
