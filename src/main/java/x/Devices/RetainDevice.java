package x.Devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.retain.RetainProcess;

public class RetainDevice extends BaseDevice {

  private final Logger logger = LoggerFactory.getLogger(RetainDevice.class);

  public boolean activ = true;
  private int retryCounter = 0;
  private RetainProcess retainProcess;
  private static final int CYCLETIME = (1000 * 60 * 60);

  public RetainDevice(boolean active, RetainProcess retainProcess) {
    super(CYCLETIME);
    this.activ = active;
    this.retainProcess = retainProcess;
    this.preIdle();
  }

  private void preIdle() {
    if (retainProcess != null) {
      try {
        retainProcess.runonce();
      } catch (Exception ex) {
        activ = false;
        logger.error(ex.getMessage());
      }
    }
  }

  @Override
  public void receiveIdle() {
    if (retainProcess != null && activ == true) {
      try {
        retainProcess.execute();
        retryCounter = 0;
        logger.debug("REMOTE Request ok");
      } catch (Exception ex) {
        retryCounter++;
        if (retryCounter >= 5) {
          activ = false;
          logger.error("Deaktivating RetainDevice in Exception!");
          logger.error(ex.getMessage());
        }
      }
    }
  }
}
