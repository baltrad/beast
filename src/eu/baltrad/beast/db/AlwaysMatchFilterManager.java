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

public class AlwaysMatchFilterManager implements IFilterManager {
  @Override
  public AlwaysMatchFilter load(int id) {
    return new AlwaysMatchFilter();
  }

  @Override
  public void store(IFilter filter) { }

  @Override
  public void update(IFilter filter) { }

  @Override
  public void remove(IFilter filter) { }
}
