/* --------------------------------------------------------------------
Copyright (C) 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.rules;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.db.IFilterManager;

/**
 * Manage rule and filter associations in table beast_rule_filters
 */
public class RuleFilterManager {
  /**
   * JDBC operations
   */
  private SimpleJdbcOperations jdbcOps = null;
  
  /**
   */
  private IFilterManager filterManager = null;

  /**
   * @param jdbcOps the JDBC operations to set
   */
  public void setSimpleJdbcOperations(SimpleJdbcOperations jdbcOps) {
    this.jdbcOps = jdbcOps;
  }

  public void setFilterManager(IFilterManager filterManager) {
    this.filterManager = filterManager;
  }

  /**
   * delete rule/filter associations from the database.
   * @param ruleId id of the rule whose filters should be deleted
   */
  @Transactional(propagation=Propagation.REQUIRED,
                 rollbackFor=Exception.class)
  public void deleteFilters(int ruleId) {
    List<Integer> filterIds = getRuleFilterIds(ruleId);
    jdbcOps.update(
      "delete from beast_rule_filters where rule_id=?",
      new Object[]{ruleId}
    );
    for (int id : filterIds) {
      filterManager.remove(filterManager.load(id));
    }
  }

  protected List<Integer> getRuleFilterIds(int ruleId) {
    return new ArrayList<Integer>(getRuleFilterKeyIdMap(ruleId).values());
  }
  
  protected Map<String, Integer> getRuleFilterKeyIdMap(int ruleId) {
    List<Map<String, Integer>> data = jdbcOps.query(
      "select key, filter_id from beast_rule_filters where rule_id=?",
      new ParameterizedRowMapper<Map<String, Integer>>() {
        @Override
        public Map<String, Integer> mapRow(ResultSet rs, int rowNum)
            throws SQLException {
          Map<String, Integer> map = new HashMap<String, Integer>();
          map.put(rs.getString("key"), rs.getInt("filter_id"));
          return map;
        }
      },
      new Object[]{ruleId}
    );
    
    Map<String, Integer> result = new HashMap<String, Integer>();
    for (Map<String, Integer> row : data) {
      result.putAll(row);
    }

    return result;
  }
  
  /**
   * load filters from the database.
   * @param ruleId id of the rule filters should be loaded for
   * @return map of filters
   */
  public Map<String, IFilter> loadFilters(int ruleId) {
    Map<String, IFilter> filters = new HashMap<String, IFilter>();
    Map<String, Integer> filterKeyIdMap = getRuleFilterKeyIdMap(ruleId);
    for (String key : filterKeyIdMap.keySet()) {
      filters.put(key, filterManager.load(filterKeyIdMap.get(key)));
    }
    return filters;
  }
  
  /**
   * store rule filters in the database.
   * @param ruleId id of the rule these filters are for
   * @param filters map of filter
   */
  @Transactional(propagation=Propagation.REQUIRED,
                 rollbackFor=Exception.class)
  public void storeFilters(int ruleId, Map<String, IFilter> filters) {
    for (String key: filters.keySet()) {
      IFilter filter = filters.get(key);
      filterManager.store(filter);
      jdbcOps.update(
        "insert into beast_rule_filters " +
        "(rule_id, key, filter_id) values (?, ?, ?)",
        new Object[]{ruleId, key, filter.getId()}
      );
    }
  }
  
  /**
   * update rule filters in the database.
   * @param ruleId id of the rule these filters are for
   * @param filters map of filters
   */
  @Transactional(propagation=Propagation.REQUIRED,
                 rollbackFor=Exception.class)
  public void updateFilters(int ruleId, Map<String, IFilter> filters) {
    deleteFilters(ruleId);
    storeFilters(ruleId, filters);
  }
}
