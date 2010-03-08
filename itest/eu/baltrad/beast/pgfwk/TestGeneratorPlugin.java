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
package eu.baltrad.beast.pgfwk;

/**
 * @author Anders Henja
 *
 */
public class TestGeneratorPlugin implements IGeneratorPlugin {
  /**
   * If the call should generate an exception or not
   */
  private boolean fail = false;

  /**
   * The algorithm that was provided at last call
   */
  private String algorithm = null;
  
  /**
   * The files that was provided at the last generate call
   */
  private String[] files = null;
  
  /**
   * The arguments that was provided at the last generate call
   */
  private Object[] args = null;
  
  /**
   * @param fail the fail to set
   */
  public void setFail(boolean fail) {
    this.fail = fail;
  }

  /**
   * @return the fail
   */
  public boolean isFail() {
    return fail;
  }
  
  /**
   * @return the algorithm
   */
  public String getAlgorithm() {
    return algorithm;
  }

  /**
   * @return the files
   */
  public String[] getFiles() {
    return files;
  }

  /**
   * @return the args
   */
  public Object[] getArgs() {
    return args;
  }
  
  /**
   * @see eu.baltrad.beast.pgfwk.IGeneratorPlugin#generate(java.lang.String, java.lang.String[], java.lang.Object[])
   */
  @Override
  public void generate(String algorithm, String[] files, Object[] arguments) {
    this.algorithm = algorithm;
    this.files = files;
    this.args = arguments;
    
    if (isFail()) {
      throw new RuntimeException();
    }
  }
}
