package x.Devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.DeviceUtils.DateUtils;
import x.DeviceUtils.DeviceListUtils;
import x.DeviceUtils.Types;
import x.utils.DashboardInfo;
import x.utils.PropertyInfo;

public class ShadingDevice extends BaseDevice {

  private final Logger logger = LoggerFactory.getLogger(ShadingDevice.class);

  @PropertyInfo(handle = 0, editable = true, displayName = "Start-Zeit", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE, propertyType = Types.PROPERTYTYPE_TIME, stepValue = 30)
  public String timeStart;

  @PropertyInfo(handle = 1, editable = true, displayName = "Stop-Zeit", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE, propertyType = Types.PROPERTYTYPE_TIME, stepValue = 30)
  public String timeStop;

  @PropertyInfo(handle = 2, displayName = "Abh. Sens.Temp.", type = Types.TYPE_PROPERTIEINFO_INFO)
  public String concurrentSensorTemperatureInfo = "-";

  @PropertyInfo(handle = 3, displayName = "Abh. Outputs", type = Types.TYPE_PROPERTIEINFO_INFO)
  public String outputsInfo = "-";

  @PropertyInfo(handle = 4, editable = true, displayName = "Sollwert Temp. ändern", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE, visibleOnREST = true)
  public int sollvalueTemp = 0;

  @PropertyInfo(handle = 5, editable = true, displayName = "Dauer activ", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE, visibleOnREST = true)
  public int durationActive = 5;

  @PropertyInfo(handle = 6, editable = true, displayName = "Dauer Nachlauf aktiv", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE, visibleOnREST = true)
  public int durationPostActive = 100;

  @PropertyInfo(handle = 7, editable = true, displayName = "Dauer reactiv", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE, visibleOnREST = true)
  public int durationReactive = 5;

  @PropertyInfo(handle = 8, editable = true, displayName = "Status", type = Types.TYPE_PROPERTIEINFO_TOGGLEACTIVATION, stateful = true)
  @DashboardInfo(handle = 0, displayName = "Status", stateful = true)
  public boolean activ = false;

  private DateUtils du = null;
  private boolean started = false;
  private boolean idle = true;
  String[] outputs = null;
  String[] concurrentTemperatureIds = null;

  public ShadingDevice(int cycleTime) {
    super(cycleTime);
    this.du = new DateUtils();
  }

  public void addOutputs(String outputs) {
    this.outputs = outputs.split(";");
  }

  public void addConcurrentTemperatureIds(String concurrentTemperatureIds) {
    this.concurrentTemperatureIds = concurrentTemperatureIds.split(";");
  }

  @Override
  public void afterIdle() {
    if (concurrentTemperatureIds != null) {
      StringBuilder namedconcurrentSensorTemperatureIds = new StringBuilder();
      for (String tId : concurrentTemperatureIds) {
        if (!tId.equals(id)) {
          SensorTemperatureDevice td = DeviceListUtils.getInstance().getSensorTemperatureDeviceById(tId);
          if (td != null) {
            namedconcurrentSensorTemperatureIds.append(td.categorie).append(" (").append(td.displayName).append(")" + ";");
          }
        }
      }
      concurrentSensorTemperatureInfo = namedconcurrentSensorTemperatureIds.toString();
    }
    if (outputs != null) {
      StringBuilder namedOutputIds = new StringBuilder();
      for (String tId : outputs) {
        if (!tId.equals(id)) {
          OutputDevice td = DeviceListUtils.getInstance().getOutputById(tId);
          if (td != null) {
            namedOutputIds.append(td.categorie).append(" (").append(td.displayName).append(")" + ";");
          }
        }
      }
      outputsInfo = namedOutputIds.toString();
    }
  }

  @Override
  public void receiveMessage(Object message) {
    super.receiveMessage(message);
  }

  @Override
  public void receiveIdle() {
    if (activ) {
      if (this.outputs != null && this.outputs.length == 2 && idle == true) {
        boolean condition = false;
        if (du.checkStatus(timeStart, timeStop) && checkTemperatureCondition()) {
          condition = true;
        }
        if (condition && started == false) {
          String[] dateString = du.getCompleteDateString();
          logger.debug("SHADING <" + id + "> => (" + dateString[0] + "," + dateString[1] + ") => STATE ON");
          setOutput(0, durationActive * 1000, durationPostActive);
          started = true;
        } else if (condition == false && started) {
          String[] dateString = du.getCompleteDateString();
          logger.debug("SHADING <" + id + "> => (" + dateString[0] + "," + dateString[1] + ") => STATE OFF");
          setOutput(1, durationReactive * 1000, 0);
          started = false;
        }
      }
    }
    super.receiveIdle();
  }

  private void setOutput(int index, int duration, int postduration) {
    if (outputs != null && duration > 0) {
      idle = false;
      OutputDevice o = DeviceListUtils.getInstance().getOutputById(outputs[index]);
      try {
        if (o.isValue() != true) {
          o.sendMessage(true);
        }
        Thread.sleep(duration);
        o.sendMessage(false);
        if (postduration > 0) {
          Thread.sleep(3000);
          if (o.isValue() != true) {
            o.sendMessage(true);
          }
          Thread.sleep(postduration);
          o.sendMessage(false);
        }
      } catch (Exception ex) {
        if (o.isValue() == true) {
          o.sendMessage(false);
        }
      }
      idle = true;
    }
  }

  private boolean checkTemperatureCondition() {
    boolean state = false;
    if (concurrentTemperatureIds != null) {
      if (concurrentTemperatureIds.length >= 1) {
        SensorTemperatureDevice o = DeviceListUtils.getInstance().getSensorTemperatureDeviceById(concurrentTemperatureIds[0]);
        if (o != null) {
          state = ((o.getValueAsInt() - sollvalueTemp) > 0);
        }
      }
    }
    return state;
  }
}
