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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class CombinedFilterManager implements IFilterManager {
  private SimpleJdbcOperations template;
  private IFilterManager childManager;

  private class CombinedFilterMapper
      implements ParameterizedRowMapper<CombinedFilter> {
    @Override
    public CombinedFilter mapRow(ResultSet rs, int rnum)
        throws SQLException {
      CombinedFilter flt = new CombinedFilter();
      flt.setMatchType(CombinedFilter.MatchType.valueOf(rs.getString("match_type")));
      return flt;
    }
  }

  private class ChildFilterMapper
      implements ParameterizedRowMapper<IFilter> {
    private IFilterManager childManager;

    ChildFilterMapper(IFilterManager childManager) {
      this.childManager = childManager;
    }

    @Override
    public IFilter mapRow(ResultSet rs, int rnum)
        throws SQLException {
      return childManager.load(rs.getInt("child_id"));
    }
  }

  public void setJdbcTemplate(SimpleJdbcOperations template) {
    this.template = template;
  }
  
  /**
   * set manager used for loading child filters
   */
  public void setChildManager(IFilterManager manager) {
    childManager = manager;
  }
  
  public CombinedFilter load(int id) {
    String sql = "select * from beast_combined_filters where filter_id=?";
    CombinedFilter flt = template.queryForObject(sql, new CombinedFilterMapper(), id);

    sql = "select * from beast_combined_filter_children where filter_id=?";
    List<IFilter> children = template.query(sql, new ChildFilterMapper(childManager), id);
    flt.setChildFilters(children);
    return flt;
  }

  @Override
  public void store(IFilter filter) {
    CombinedFilter flt = (CombinedFilter)filter;

    template.update(
      "insert into beast_combined_filters(filter_id, match_type) " +
        "values (?, ?)",
      flt.getId(),
      flt.getMatchType().toString());
    insertChildren(flt);
  }

  @Override
  public void update(IFilter filter) {
    CombinedFilter flt = (CombinedFilter)filter;

    template.update(
      "update beast_combined_filters set match_type=? where filter_id=?",
      flt.getMatchType().toString(),
      flt.getId());
    template.update(
      "delete from beast_combined_filter_children where filter_id=?",
      flt.getId());
    insertChildren(flt);
  }

  @Override
  public void remove(IFilter filter) {
    CombinedFilter flt = (CombinedFilter)filter;

    template.update(
      "delete from beast_combined_filter_children where filter_id=?",
      flt.getId());
    template.update(
      "delete from beast_combined_filters where filter_id=?",
      flt.getId());
  }

  private void insertChildren(CombinedFilter filter) {
    for (IFilter child : filter.getChildFilters()) {
      template.update(
        "insert into beast_combined_filter_children " +
          "(filter_id, child_id) values (?, ?)",
        filter.getId(),
        child.getId());
    }
  }
}
