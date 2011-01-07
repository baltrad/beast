/* --------------------------------------------------------------------
Copyright (C) 2009-2010 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.rules.groovy;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IScriptableRule;
import eu.baltrad.beast.rules.RuleException;
import groovy.lang.GroovyClassLoader;

/**
 * @author Anders Henja
 *
 */
public class GroovyRule implements IRule {
  /**
   * The type name of this rule
   */
  public final static String TYPE = "groovy";
  
  /**
   * The the groovy script
   */
  private String script = null;

  /**
   * The scriptable rule that is defined by a groovy script
   */
  private IScriptableRule rule = null;
  
  /**
   * Default constructor
   */
  public GroovyRule() {
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRule#getType()
   */
  @Override
  public String getType() {
    return TYPE;
  }

  /**
   * To be able to set a precompiled groovy rule
   * @param rule the rule to set
   */
  public void setScriptableRule(IScriptableRule rule) {
    this.rule = rule;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    if (message == null) {
      throw new NullPointerException();
    } else if (rule == null) {
      throw new RuleException();
    }
    return rule.handle(message);
  }

  /**
   * Creates an instance of the groovy script. Must be implementing
   * the @ref {@link IScriptableRule}. Will compile and validate the script.
   * @param script the script to set
   * @throws RuleException if the script could not be set
   */
  @SuppressWarnings({"rawtypes" })
  public void setScript(String script) {
    GroovyClassLoader gcl = new GroovyClassLoader();
    try {
      Class c = gcl.parseClass(script);
      rule = (IScriptableRule)c.newInstance();
    } catch (Throwable t) {
      throw new RuleException("Failed to instantiate groovy script", t);
    }       
    this.script = script;
  }
  
  /**
   * @return the groovy script as a string
   */
  public String getScript() {
    return this.script;
  }
}
