/* --------------------------------------------------------------------
Copyright (C) 2009-2013 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.net.sftp;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import eu.baltrad.beast.net.scp.SCPFileUploadHandler;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;

/**
 * @author Anders Henja
 *
 */
public class SFTPFileUploadHandler extends SCPFileUploadHandler {

  /**
   * @see eu.baltrad.beast.net.FileUploadHandler#upload(java.io.File, java.net.URI)
   */
  @Override
  protected void store(SSHClient client, File src, URI dst) throws IOException {
    SFTPClient sftp = null;
    try {
      sftp=client.newSFTPClient();
      sftp.put(src.toString(), getPath(dst));
    } finally {
      if (sftp != null) {
        sftp.close();
      }
    }
  }
}
