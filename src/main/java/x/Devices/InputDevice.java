package x.Devices;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.DeviceUtils.InputParameter;
import x.DeviceEffects.BaseEffect;

public class InputDevice extends BaseDevice {

  private final Logger logger = LoggerFactory.getLogger(InputDevice.class);
  private boolean value = false;
  private long startTime = 0;
  private boolean lockedByVisu = false;
  private long removeTimeLockedByVisu = 0;
  public ArrayList<InputParameter> inputParams = new ArrayList<>();
  public int busAddress = 0;

  public InputDevice(int cycleTime) {
    super(cycleTime);
  }

  @Override
  public void receiveMessage(Object message) {
    value = (boolean) message;
    logger.debug("INPUT <" + id + "> => <" + value + ">");
    if (value) {
      startTime = System.currentTimeMillis();
      for (InputParameter param : inputParams) {
        ((BaseEffect) param.effect).resetEffect();
        ((BaseEffect) param.effect).initEffect(startTime);
        ((BaseEffect) param.effect).trigger(value);
      }
    }
    super.receiveMessage(message);
  }

  @Override
  public void receiveIdle() {
    for (InputParameter param : inputParams) {
      ((BaseEffect) param.effect).trigger(value);
    }
    if (isLockedByVisu()) {
      if (System.currentTimeMillis() > removeTimeLockedByVisu) {
        setLockedByVisu(false);
      }
    }
    super.receiveIdle();
  }

  synchronized public boolean isLockedByVisu() {
    return lockedByVisu;
  }

  synchronized public void setLockedByVisu(boolean lockedByVisu) {
    this.lockedByVisu = lockedByVisu;
    if (lockedByVisu) {
      removeTimeLockedByVisu = 90000 + System.currentTimeMillis();
    } else {
      removeTimeLockedByVisu = 0;
    }
  }

  public boolean isValue() {
    return value;
  }

}
