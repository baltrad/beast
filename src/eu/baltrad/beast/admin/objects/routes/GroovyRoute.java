/**
 * 
 */
package eu.baltrad.beast.admin.objects.routes;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonRootName;

import eu.baltrad.beast.router.IRouterManager;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.groovy.GroovyRule;

/**
 * @author anders
 */
@JsonRootName("groovy-route")
public class GroovyRoute extends Route {
  private String script = null;

  /**
   * @return the script
   */
  public String getScript() {
    return script;
  }

  /**
   * @param script the script to set
   */
  public void setScript(String script) {
    this.script = script;
  }
  
  /**
   * Fills self with information from route definition
   * @param def - the route definition
   */
  @Override
  public void fromRouteDefinition(RouteDefinition def) {
    GroovyRule rule = (GroovyRule)def.getRule();
    this.setAuthor(def.getAuthor());
    this.setDescription(def.getDescription());
    this.setName(def.getName());
    this.setRecipients(def.getRecipients());
    this.setActive(def.isActive());
    this.setScript(rule.getScript());
    
  }

  /**
   * Creates a rule from self using the provided router manager
   * @routerManager - the router manager
   */
  @Override
  public IRule toRule(IRouterManager routerManager) {
    GroovyRule rule = (GroovyRule)routerManager.createRule(GroovyRule.TYPE);
    rule.setScript(this.getScript());
    return rule;
  }
  
  
  @Override
  @JsonIgnore
  public boolean isValid() {
    if (getName() != null && !getName().isEmpty()) {
      return true;
    }
    return false;
  }
}
