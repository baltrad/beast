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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class FileUploadHandlerBase implements FileUploadHandler {
  /**
   * append a path to an URI.
   *
   * Handles appending to hierarchical URIs.
   */
  @Override
  public URI appendPath(URI uri, String path) {
    String uriPathString = uri.getPath();
    if (uriPathString == null)
      throw new IllegalArgumentException(uri + " has no path");
    File uriPath = new File(uriPathString, path);
    try {
      return new URI(
        uri.getScheme(),
        uri.getAuthority(),
        uriPath.toString(),
        uri.getQuery(),
        uri.getFragment()
      );
    } catch (URISyntaxException e) {
      throw new RuntimeException(
        "could not append '" + path + "' to " + uri, e
      );
    }
  }
}
