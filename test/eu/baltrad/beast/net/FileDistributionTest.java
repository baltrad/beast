package eu.baltrad.beast.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.easymock.EasyMock.expectLastCall;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownServiceException;

import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileDistributionTest extends EasyMockSupport {
  
  private static interface FileDistributionMethods {
    void warnAboutOngoingUpload(File src, URI fullDestination);
  };
  
  private FileDistribution classUnderTest;
  private FileUploadHandler uploadHandler;
  private FileDistributionMethods methods;
  private final String defaultSrcName = "/path/to/file";
  private final String defaultDestinationName = "ftp://u:p@h/d";
  private final String defaultEntryName = "entryName";
  private final String defaultFullDestinationName = defaultDestinationName + "/" + defaultEntryName;
  private File defaultSrc;
  private URI defaultDestination;
  private URI defaultFullDestination;

  @Before
  public void setUp() throws Exception {
    defaultSrc = new File(defaultSrcName);
    defaultDestination = new URI(defaultDestinationName);
    defaultFullDestination = new URI(defaultFullDestinationName);
    
    methods = createMock(FileDistributionMethods.class);

    uploadHandler = createMock(FileUploadHandler.class);

    classUnderTest = new FileDistribution(defaultSrc, defaultDestination, defaultEntryName) {
      @Override
      protected void warnAboutOngoingUpload(File src, URI fullDestination) {
        methods.warnAboutOngoingUpload(src, fullDestination);
      }
    };
    
    classUnderTest.setUploadHandler(uploadHandler);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGetHandlerByScheme() {

    String[] validSchemes = {"copy", "ftp", "scp", "scponly", "sftp"};

    for (int i = 0; i < validSchemes.length; i++) {
      String scheme = validSchemes[i];
      try {
        classUnderTest.getHandlerByScheme(scheme);        
      } catch (UnknownServiceException e) {
        fail("Exception should not be thrown for valid scheme: " + scheme);
      }
    }
    
    String invalidScheme = "inval";
    boolean exceptionCaught = false;
    try {
      classUnderTest.getHandlerByScheme(invalidScheme);        
    } catch (UnknownServiceException e) {
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
    
  }
  
  @Test
  public void testNonAbsoluteSourcePath() throws IOException {
    
    classUnderTest = new FileDistribution(new File("../non/absolute/path"), defaultDestination, defaultEntryName);
        
    boolean exceptionCaught = false;
    try {
      classUnderTest.run();
    } catch (IllegalArgumentException e) {
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
    
    assertEquals(FileDistribution.getCurrentUploads().size(), 0);
  }
  
  @Test
  public void testUpload() throws IOException {
    
    uploadHandler.upload(defaultSrc, defaultFullDestination);
    replayAll();
    
    classUnderTest.run();
    
    assertEquals(FileDistribution.getCurrentUploads().size(), 0);
  }
  
  @Test
  public void testUploadAlreadyOngoing() throws IOException {
    FileDistribution concurrentDistribution = new FileDistribution(defaultSrc, defaultDestination, defaultEntryName);
    concurrentDistribution.lockUpload(defaultSrc, defaultFullDestination);

    methods.warnAboutOngoingUpload(defaultSrc, defaultFullDestination);
    replayAll();

    classUnderTest.run();
    
    verifyAll();
    
    assertEquals(FileDistribution.getCurrentUploads().size(), 1);
    concurrentDistribution.unlockUpload(defaultFullDestination);
    assertEquals(FileDistribution.getCurrentUploads().size(), 0);
  }
  
  @Test
  public void testUnknownService() throws URISyntaxException {
    
    String invalidDestinationName = "hftp://u:p@h/d";
    URI invalidDestination = new URI(invalidDestinationName);
    
    boolean exceptionCaught = false;
    try {
      classUnderTest = new FileDistribution(defaultSrc, invalidDestination, defaultEntryName);
    } catch (UnknownServiceException e) {
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
  }
  
  @Test
  public void testUploadThrowsException() throws IOException {
    
    Appender mockAppender = createMock(Appender.class);
    LogManager.getRootLogger().addAppender(mockAppender);
  
    uploadHandler.upload(defaultSrc, defaultFullDestination);
    
    expectLastCall().andThrow(new IOException("Something terrible happened"));
    
    Capture<LoggingEvent> capturedArgument = new Capture<LoggingEvent>();
    mockAppender.doAppend(EasyMock.capture(capturedArgument));
    
    replayAll();
  
    classUnderTest.run();
    
    // clean up an remove temporary appender to not cause problems for other tests
    LogManager.getRootLogger().removeAppender(mockAppender);
    
    verifyAll();
    
    LoggingEvent logEvent = capturedArgument.getValue();    
    assertEquals(logEvent.getRenderedMessage(), "File distribution failed!");
    
    assertEquals(FileDistribution.getCurrentUploads().size(), 0);
  }
  

}
