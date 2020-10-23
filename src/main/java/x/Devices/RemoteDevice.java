package x.Devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.utils.RemoteAccessRequest;

public class RemoteDevice extends BaseDevice {

  private final Logger logger = LoggerFactory.getLogger(RemoteDevice.class);
  RemoteAccessRequest request = null;
  public boolean activ = false;

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
          logger.error("Error sending RemoteRequest");
        } else {
          logger.debug("REMOTE " + response);
        }
      } catch (Exception ex) {
        logger.error("Error sending RemoteRequest");
        logger.error(ex.getMessage());
      }
    }
  }
}
