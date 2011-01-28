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
package eu.baltrad.beast.router;

import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleManager;

/**
 * Test class.
 * @author Anders Henja
 */
public class DummyRuleManager implements IRuleManager {
  private String type = null;
  private int deleteId = -1;
  private int loadId = -1;
  private int storeId = -1;
  private IRule storeRule = null;
  private int updateId = -1;
  private IRule updateRule = null;
  private RuntimeException storeException = null;
  
  public DummyRuleManager(String type) {
    this.type = type;
  }
  
  @Override
  public void delete(int ruleId) {
    this.deleteId = ruleId;
  }

  @Override
  public IRule load(int ruleId) {
    this.loadId = ruleId;
    return new DummyRule(type);
  }

  @Override
  public void store(int ruleId, IRule rule) {
    this.storeId = ruleId;
    this.storeRule = rule;
    if (storeException != null) {
      throw storeException;
    }
  }

  @Override
  public void update(int ruleId, IRule rule) {
    this.updateId = ruleId;
    this.updateRule = rule;
  }
  
  public int getDeleteId() {
    return this.deleteId;
  }
  
  public int getLoadId() {
    return this.loadId;
  }
  
  public int getStoreId() {
    return this.storeId;
  }
  
  public IRule getStoreRule() {
    return this.storeRule;
  }
  
  public int getUpdateId() {
    return this.updateId;
  }
  
  public IRule getUpdateRule() {
    return this.updateRule;
  }
  
  public String getType() {
    return this.type;
  }
  
  public void setStoreException(RuntimeException e) {
    this.storeException = e;
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#createRule()
   */
  @Override
  public IRule createRule() {
    // TODO Auto-generated method stub
    return null;
  }
}
