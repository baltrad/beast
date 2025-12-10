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

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

public class AttributeFilterManager implements IFilterManager {
  private JdbcOperations template;

  public void setJdbcTemplate(JdbcOperations template) {
    this.template = template;
  }

  @Override
  public AttributeFilter load(int id) {
    RowMapper<AttributeFilter> mapper =
      new RowMapper<AttributeFilter>() {
        @Override
        public AttributeFilter mapRow(ResultSet rs, int rnum)
            throws SQLException {
          AttributeFilter flt = new AttributeFilter();
          flt.setAttribute(rs.getString("attr"));
          flt.setOperator(AttributeFilter.Operator.valueOf(rs.getString("op")));
          flt.setValueType(AttributeFilter.ValueType.valueOf(rs.getString("value_type")));
          flt.setValue(rs.getString("value"));
          flt.setNegated(rs.getBoolean("negated"));
          return flt;
        }
      };

    String sql = "select * from beast_attr_filters where filter_id=?";
    return template.queryForObject(sql, mapper, id);
  }

  @Override
  public void store(IFilter filter) {
    AttributeFilter flt = (AttributeFilter)filter;

    template.update(
      "insert into beast_attr_filters " +
        "(filter_id, attr, op, value_type, value, negated)" +
        "values (?,?,?,?,?,?)",
      flt.getId(),
      flt.getAttribute(),
      flt.getOperator().toString(),
      flt.getValueType().toString(),
      flt.getValue(),
      flt.isNegated());
  }

  @Override
  public void update(IFilter filter) {
    AttributeFilter flt = (AttributeFilter)filter;

    template.update(
      "update beast_attr_filters set attr=?, op=?, value_type=?, value=?, negated=? where filter_id=?",
      flt.getAttribute(),
      flt.getOperator().toString(),
      flt.getValueType().toString(),
      flt.getValue(),
      flt.isNegated(),
      flt.getId());
  }

  @Override
  public void remove(IFilter filter) {
    AttributeFilter flt = (AttributeFilter)filter;
    template.update(
      "delete from beast_attr_filters where filter_id=?",
      flt.getId());
  }
}
