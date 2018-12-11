package eu.baltrad.beast.net.scp;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import net.schmizz.sshj.xfer.scp.ScpCommandLine;

public class SCPOnlyFileUploadHandler extends SCPFileUploadHandler {

  /**
   * @see eu.baltrad.beast.net.FileUploadHandler#upload(java.io.File, java.net.URI)
   */
  @Override
  protected void store(SSHClient client, File src, URI dst) throws IOException {
    SCPFileTransfer xfer = client.newSCPFileTransfer();
    String dststr = getPath(dst);
    if (dststr.startsWith("/")) {
      dststr = dststr.substring(1);
    }
    xfer.newSCPUploadClient().copy(new FileSystemFile(src.toString()), dststr, ScpCommandLine.EscapeMode.NoEscape);
  }
}
