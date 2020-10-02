package x.Devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.DeviceUtils.Types;
import x.utils.MathParser;
import x.utils.PropertyInfo;

public class AnalogInDevice extends BaseDevice {

  private final Logger logger = LoggerFactory.getLogger(AnalogInDevice.class);

  public int busAddress = 0;

  @PropertyInfo(handle = 0, displayName = "Label", type = Types.TYPE_PROPERTIEINFO_INFO)
  public String label = "";

  @PropertyInfo(handle = 1, displayName = "Value", type = Types.TYPE_PROPERTIEINFO_INFO)
  public float valueVisu = 0;

  private int value = 0;
  private String math = "";

  public AnalogInDevice(int cycleTime) {
    super(cycleTime);
  }

  public void setMath(String math) {
    this.math = math;
  }

  @Override
  public void receiveMessage(Object message) {
    value = (int) ((short) ((int) message)); // Zweierkomplement => 65535 -> -1
    valueVisu = MathParser.parse(value, math);
    logger.debug("ANALOG_IN <" + id + "> => <" + value + "> <" + valueVisu + "> <" + label + ">");
    super.receiveMessage(message);
  }

}
