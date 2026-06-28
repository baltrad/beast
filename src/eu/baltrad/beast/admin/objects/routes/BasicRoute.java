/**
 * 
 */
package eu.baltrad.beast.admin.objects.routes;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author anders
 *
 */
public class BasicRoute extends Route {
  /**
   * Default constructor
   */
  public BasicRoute() {
    super();
  }
  
  /**
   * Constructor
   * @param name the name
   */
  public BasicRoute(String name) {
    super(name);
  }
  
  /**
   * @return if this route is valid or not
   */
  @Override
  @JsonIgnore
  public boolean isValid() {
    return true;
  }
}
