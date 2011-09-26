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

import org.codehaus.groovy.control.CompilationFailedException;

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
   * Rule has not got any script set
   */
  public final static int UNITIALIZED = -1;
  
  /**
   * Rule is ok
   */
  public final static int OK = 0;
  
  /**
   * Rule could not be compiled
   */
  public final static int COMPILATION_ERROR = 1;
  
  /**
   * Rule coult not be instantiated
   */
  public final static int INSTANTIATION_EXCEPTION = 2;
  
  /**
   * Access problems
   */
  public final static int ILLEGAL_ACCESS_EXCEPTION = 3;
  
  /**
   * Class cast exception, probably not implementing IScriptableRule
   */
  public final static int CLASS_CAST_EXCEPTION = 4;
  
  /**
   * Any type of throwable causing a failure
   */
  public final static int THROWABLE = 5;
  
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
   * The state of this rule
   */
  private int state = UNITIALIZED;
  
  /**
   * Upon error during set of compilation of code, this exception will be set
   */
  private Throwable throwable = null;
  
  /**
   * Default constructor, however use manager for creation
   */
  protected GroovyRule() {
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRule#getType()
   */
  @Override
  public String getType() {
    return TYPE;
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#isValid()
   */
  @Override
  public boolean isValid() {
    return (this.state == OK);
  }
  
  /**
   * To be able to set a precompiled groovy rule
   * @param rule the rule to set
   */
  public void setScriptableRule(IScriptableRule rule) {
    this.rule = rule;
    this.state = OK;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    if (message == null) {
      throw new NullPointerException();
    } else if (rule == null) {
      return null;
    }
    return rule.handle(message);
  }

  /**
   * Sets the script that has been loaded from the database. So that
   * it is possible to load faulty scripts.
   * @param script the script to set
   * @return the status
   */
  @SuppressWarnings("unchecked")
  int setScriptInternal(String script, boolean setscriptonfailure, boolean throwexception) {
    this.throwable = null;
    try {
      Class c = parseClass(script);
      rule = (IScriptableRule)c.newInstance();
      state = OK;
    } catch (CompilationFailedException e) {
      state = COMPILATION_ERROR;
      this.throwable = e;
    } catch (InstantiationException e) {
      state = INSTANTIATION_EXCEPTION;
      this.throwable = e;
    } catch (IllegalAccessException e) {
      state = ILLEGAL_ACCESS_EXCEPTION;
      this.throwable = e;
    } catch (ClassCastException e) {
      state = CLASS_CAST_EXCEPTION;
      this.throwable = e;
    } catch (Throwable t) {
      state = THROWABLE;
      this.throwable = t;
    }
    if (setscriptonfailure == true || (setscriptonfailure == false && throwable == null)) {
      this.script = script;
    }
    if (throwable != null && throwexception) {
      throw new RuleException(this.throwable);
    }
    return state;
  }
  
  /**
   * Creates an instance of the groovy script. Must be implementing
   * the @ref {@link IScriptableRule}. Will compile and validate the script.
   * @param script the script to set
   * @throws RuleException if the script could not be set
   */
  public void setScript(String script) {
    setScriptInternal(script,false,true);
  }
  
  /**
   * @return the groovy script as a string
   */
  public String getScript() {
    return this.script;
  }

  /**
   * @return the state
   */
  public int getState() {
    return state;
  }
  
  /**
   * @return the exception if any occured during compilation
   */
  public Throwable getThrowable() {
    return this.throwable;
  }
  
  /**
   * Parses code into a class
   * @param code the code to parse
   * @return a class
   */
  @SuppressWarnings("unchecked")
  protected Class parseClass(String code) {
    GroovyClassLoader gcl = new GroovyClassLoader();
    return gcl.parseClass(code);
  }
}
