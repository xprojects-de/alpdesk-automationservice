package x.Devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.DeviceUtils.TransferObject;
import x.DeviceUtils.Types;
import x.utils.DashboardInfo;
import x.utils.PropertyInfo;

public class DimmerDevice extends BaseDevice {

  private final Logger logger = LoggerFactory.getLogger(DimmerDevice.class);
  public static final int MAX_VALUE = 0x7FFF;
  public static final int MIN_VALUE = 0;
  private static final int DIRECTION_UP = 1;
  private static final int DIRECTION_DOWN = 2;

  @PropertyInfo(handle = 0, displayName = "Aktiv", type = Types.TYPE_PROPERTIEINFO_INFO, stateful = true)
  public boolean activ = false;
  @PropertyInfo(handle = 1, displayName = "Wert (%)", type = Types.TYPE_PROPERTIEINFO_INFO)
  @DashboardInfo(handle = 1, displayName = "Wert (%)")
  public String valuePercent;
  @PropertyInfo(handle = 2, displayName = "Verzögerung (ms)", type = Types.TYPE_PROPERTIEINFO_INFO)
  public int startDelay = 1250;
  public int busAddress = 0;

  private long startDelayTime = 0;
  private boolean value = false;
  private int CURRENTDIRECTION = DimmerDevice.DIRECTION_UP;
  private int value2set = 1;
  private int liveValue = 0;
  private boolean stateChanged = true;

  public DimmerDevice(int cycleTime) {
    super(cycleTime);
  }

  @Override
  public void receiveMessage(Object message) {
    if (message instanceof TransferObject) {
      stateChanged = ((TransferObject) message).isValueBoolean();
    } else {
      value = (boolean) message;
      if (value) {
        startDelayTime = System.currentTimeMillis();
        if (liveValue == DimmerDevice.MIN_VALUE) {
          liveValue = value2set;
          activ = true;
        } else {
          liveValue = DimmerDevice.MIN_VALUE;
          activ = false;
        }
        stateChanged = true;
      }
      logger.debug("DIMMER <" + id + "> => <" + value + "><" + value2set + "><" + liveValue + "><" + CURRENTDIRECTION + ">");
    }
    super.receiveMessage(message);
  }

  @Override
  public void receiveIdle() {
    if (value && (System.currentTimeMillis() - startDelayTime) >= startDelay) {
      if (value2set >= DimmerDevice.MAX_VALUE) {
        CURRENTDIRECTION = DimmerDevice.DIRECTION_DOWN;
      } else if (value2set <= DimmerDevice.MIN_VALUE) {
        CURRENTDIRECTION = DimmerDevice.DIRECTION_UP;
      }
      if (CURRENTDIRECTION == DimmerDevice.DIRECTION_UP) {
        value2set += 250;
      } else {
        value2set -= 250;
      }
      liveValue = value2set;
      stateChanged = true;
      activ = true;
    }
    valuePercent = ((int) ((value2set * 100.0f) / DimmerDevice.MAX_VALUE)) + "";
    super.receiveIdle();
  }

  public boolean isValue() {
    return value;
  }

  public int getLiveValue() {
    return liveValue;
  }

  public boolean isStateChanged() {
    return stateChanged;
  }

}
