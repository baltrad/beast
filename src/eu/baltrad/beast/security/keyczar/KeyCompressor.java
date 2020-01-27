package eu.baltrad.beast.security.keyczar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import eu.baltrad.beast.security.SecurityStorageException;

/**
 * Utility for compressing and uncompressing keyczar keys.
 * @author anders
 */
public class KeyCompressor {

  /**
   * Unzips a byte array into a file structure
   * @param file the root folder where the files should be extracted
   * @param bytes the byte array
   * @throws SecurityStorageException if something erroneous occurs during the unzip
   */
  public void unzip(File file, byte[] bytes) throws IOException {
    if (!file.exists()) {
      file.mkdir();
    }
    ZipInputStream zis = null;
    byte[] buff = new byte[1024];
    try {
      zis = new ZipInputStream(new ByteArrayInputStream(bytes));
      ZipEntry entry = zis.getNextEntry();
      while (entry != null) {
        String fileName = entry.getName();
        File extractedFile = new File(file.getAbsolutePath() + File.separator + fileName);
        FileOutputStream fos = new FileOutputStream(extractedFile);
        int len = 0;
        while ((len = zis.read(buff)) > 0) {
          fos.write(buff, 0, len);
        }
        fos.close();
        entry = zis.getNextEntry();
      }
    } finally {
      zis.closeEntry();
      zis.close();
    }
  }
  
  /**
   * Zips a keyczar key folder into a byte array
   * @param rootFolder the keyczar folder
   * @return the byte array
   * @throws IOException on IO error
   */
  public byte[] zip(File rootFolder) throws IOException {
    File[] files = new File[] {new File(rootFolder, "1"), new File(rootFolder, "meta")};
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(bos);
    for (File f: files) {
      ZipEntry entry = new ZipEntry(f.getName());
      zos.putNextEntry(entry);
      zos.write(Files.readAllBytes(f.toPath()));
      zos.closeEntry();
    }
    zos.finish();
    bos.flush();
    zos.close();
    bos.close();
    return bos.toByteArray();
  }
}
