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
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.FileUtils;

public class CopyFileUploadHandler extends FileUploadHandlerBase {
  @Override
  public void upload(File src, URI dst) throws IOException {
    File dstPath = getPath(dst);
    if (isDirectory(dstPath)) {
      copyFileToDirectory(src, dstPath);
    } else {
      copyFile(src, dstPath);
    }
  }

  protected File getPath(URI uri) {
    String path = uri.getPath();
    if (path == null || path.isEmpty())
      throw new IllegalArgumentException("no path in URI: " + uri);
    return new File(path);
  }

  protected boolean isDirectory(File path) {
    return path.isDirectory();
  }

  protected void copyFileToDirectory(File src, File dst) throws IOException {
    FileUtils.copyFileToDirectory(src, dst);
  }

  protected void copyFile(File src, File dst) throws IOException {
    FileUtils.copyFile(src, dst);
  }
}
