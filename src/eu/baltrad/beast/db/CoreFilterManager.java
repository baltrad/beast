/*
Copyright 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

This file is part of Beast library.

Beast library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Beast library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Beast library.  If not, see <http://www.gnu.org/licenses/>.
*/

package eu.baltrad.beast.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * the core filter manager. All filter management should go through
 * this class which delegates to correct submanager based on filter
 * type.
 */
class CoreFilterManager implements IFilterManager {
  private Map<String, IFilterManager> subManagers = null;
  private SimpleJdbcOperations template;

  public void setJdbcTemplate(SimpleJdbcOperations template) {
    this.template = template;
  }

  public void setSubManagers(Map<String,IFilterManager> subManagers) {
    this.subManagers = subManagers;
  }

  /**
   * @see IFilterManager#store(IFilter)
   */
  @Transactional(propagation=Propagation.REQUIRED,
                 rollbackFor=Exception.class)
  @Override
  public void store(IFilter filter) {
    int id = sqlInsertFilter(filter.getType());
    filter.setId(id);
    IFilterManager subManager = getSubManager(filter);
    subManager.store(filter);
  }
  
  /**
   * @see IFilterManager#load(int)
   */
  @Override
  public IFilter load(int id) {
    Map<String, Object> map = sqlSelectFilter(id);

    IFilterManager subManager = getSubManager((String)map.get("type"));
    IFilter filter = subManager.load(id);
    filter.setId(id);

    return filter;
  }

  /**
   * @see IFilterManager#update(IFilter)
   */
  @Transactional(propagation=Propagation.REQUIRED,
                 rollbackFor=Exception.class)
  @Override
  public void update(IFilter filter) {
    IFilterManager subManager = getSubManager(filter);
    subManager.update(filter);
  }
  
  /**
   * @see IFilterManager#remove(IFilter)
   */
  @Transactional(propagation=Propagation.REQUIRED,
                 rollbackFor=Exception.class)
  @Override
  public void remove(IFilter filter) {
    IFilterManager subManager = getSubManager(filter);
    subManager.remove(filter);

    sqlDeleteFilter(filter.getId());
    filter.setId(null);
  }

  private IFilterManager getSubManager(IFilter filter) {
    return subManagers.get(filter.getType());
  }

  private IFilterManager getSubManager(String type) {
    return subManagers.get(type);
  }

  protected Map<String, Object> sqlSelectFilter(int id) {
    String sql = "select * from beast_filters where filter_id=?";
    return template.queryForMap(sql, id);
  }

  protected int sqlInsertFilter(String filterType) {
    final String sql = "insert into beast_filters(type) values (?)";
    final String type = filterType;

    KeyHolder keyHolder = new GeneratedKeyHolder();
    template.getJdbcOperations().update(
      new PreparedStatementCreator() {
        public PreparedStatement createPreparedStatement(Connection conn)
            throws SQLException {
          PreparedStatement ps =
            conn.prepareStatement(sql, new String[]{"filter_id"});
          ps.setString(1, type);
          return ps;
        }
      },
      keyHolder);

    return keyHolder.getKey().intValue();
  }

  protected void sqlDeleteFilter(int id) {
    String sql = "delete from beast_filters where filter_id=?";
    template.update(sql, id);
  }
}
