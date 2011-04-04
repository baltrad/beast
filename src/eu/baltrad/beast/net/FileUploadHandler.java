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
package eu.baltrad.beast.net;

public interface FileUploadHandler {
  /**
   * Upload a file.
   * `dst` should be interpreted as a directory by implementers.
   * File should retain its name. A rename could be supported through
   * query arguments (e.g "scheme://host/path?name=newfilename"), though
   * this is not required.
   *
   * @param src the file to copy
   * @param dst the destination URI.
   * @throws IOException when copying fails
   */
  void upload(java.io.File src, java.net.URI dst) throws java.io.IOException;
}
