package eu.baltrad.beast.system;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class BeastSystemAppender extends AppenderSkeleton 
{
  @Override
  public void close() {
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }

  @Override
  protected void append(LoggingEvent event) {
    //SystemStatusManager.getInstance().add(event);
  }

}
