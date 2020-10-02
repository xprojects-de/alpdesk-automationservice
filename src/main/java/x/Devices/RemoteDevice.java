package x.Devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.utils.RemoteAccessRequest;

public class RemoteDevice extends BaseDevice {

  private final Logger logger = LoggerFactory.getLogger(RemoteDevice.class);
  RemoteAccessRequest request = null;
  public boolean activ = false;
  private int retryCounter = 0;

  public RemoteDevice(boolean activ, String url, int cycleTime, String jwt) {
    super(cycleTime);
    this.activ = activ;
    this.request = new RemoteAccessRequest(url, jwt);
  }

  @Override
  public void receiveIdle() {
    if (request != null && activ == true) {
      try {
        boolean response = request.execute();
        if (response == false) {
          retryCounter++;
          if (retryCounter >= 100) {
            activ = false;
            logger.info("Deaktivating RemoteAccess in response!");
          }
        } else {
          retryCounter = 0;
          logger.debug("REMOTE " + response);
        }
      } catch (Exception ex) {
        retryCounter++;
        if (retryCounter >= 100) {
          activ = false;
          logger.info("Deaktivating RemoteAccess in Exception!");
          logger.error(ex.getMessage());
        }
      }
    }
  }
}
