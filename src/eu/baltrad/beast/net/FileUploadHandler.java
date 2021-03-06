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

import java.net.URI;
import java.io.File;
import java.io.IOException;

public interface FileUploadHandler {
  /**
   * Upload a file. The implementations should determine if the
   * `dst` refers to a file or a directory and in case of the former
   * rename the file.
   *
   * @param src the file to copy
   * @param dst the destination URI.
   * @throws IOException when copying fails
   */
  void upload(File src, URI dst) throws IOException;
  
  /**
   * Append a path to an URI.
   *
   * @param uri the uri to append to
   * @param path the path to append
   * @return a new URI with the path appended.
   */
  URI appendPath(URI uri, String path);
}
