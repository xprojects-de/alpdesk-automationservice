package x.Devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.DeviceUtils.RecorderItem;
import x.DeviceUtils.Types;
import x.utils.DashboardInfo;
import x.utils.PropertyInfo;

public class SensorTemperatureDevice extends BaseDevice {

  private final Logger logger = LoggerFactory.getLogger(SensorTemperatureDevice.class);

  public int busAddress = 0;

  @PropertyInfo(handle = 0, displayName = "Temp. (°C)", type = Types.TYPE_PROPERTIEINFO_INFO)
  @DashboardInfo(handle = 0, displayName = "Temp. (°C)")
  public int valueVisu = 1000000;
  private int value = 1000000;

  @PropertyInfo(handle = 1, editable = true, displayName = "Sollwert ändern", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE, visibleOnREST = true)
  public int sollvalue = 0;

  @PropertyInfo(handle = 2, editable = true, displayName = "Status", type = Types.TYPE_PROPERTIEINFO_TOGGLEACTIVATION, stateful = true)
  @DashboardInfo(handle = 1, displayName = "Status", stateful = true)
  public boolean activ = false;

  private String[] concurrentTemperatureIds = null;
  @PropertyInfo(handle = 3, displayName = "Valid", type = Types.TYPE_PROPERTIEINFO_INFO)
  @DashboardInfo(handle = 2, displayName = "Valid", stateful = true)
  public boolean status = true;

  public SensorTemperatureDevice(int cycleTime) {
    super(cycleTime);
    this.ri = new RecorderItem(24, RecorderItem.LEGENDTYPE_DATE_HOUT_MINUTE, (1000 * 60 * 60));
  }

  public void addConcurrentTemperatureIds(String concurrentTemperatureIds) {
    this.concurrentTemperatureIds = concurrentTemperatureIds.split(";");
  }

  public String[] getConcurrentTemperatureIds() {
    return concurrentTemperatureIds;
  }

  public boolean getStatus() {
    return status;
  }

  @Override
  public void receiveMessage(Object message) {
    value = (int) ((short) ((int) message)); // Zweierkomplement => 65535 -> -1
    status = (value >= sollvalue);
    logger.debug("SENSOR-TEMPERATURE <" + id + "> => <" + value + ";" + status + "> (" + sollvalue + ")> => STATE <" + activ + ">");
    super.receiveMessage(message);
  }

  @Override
  public void receiveIdle() {
    valueVisu = value;
    if (ri != null) {
      ri.setActive(activ);
      Object data[] = {getValueAsFloat(), getSollValueAsFloat()};
      ri.addItem(new RecorderItem.InsertItem(data), id);
    }
    super.receiveIdle();
  }

  public float getValueAsFloat() {
    return (float) (value / 10.0);
  }

  public float getSollValueAsFloat() {
    return (float) (sollvalue / 10.0);
  }

  public int getValueAsInt() {
    return value;
  }

  public int getSollValueAsInt() {
    return sollvalue;
  }

}
