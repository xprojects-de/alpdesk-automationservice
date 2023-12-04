package x.Devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.DeviceUtils.DateUtils;
import x.DeviceUtils.DeviceListUtils;
import x.DeviceUtils.RecorderItem;
import x.DeviceUtils.Types;
import x.utils.DashboardInfo;
import x.utils.PropertyInfo;

public class VentilationDevice extends BaseDevice {

  private final Logger logger = LoggerFactory.getLogger(VentilationDevice.class);

  @PropertyInfo(handle = 0, editable = true, displayName = "Sollwert ändern", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE)
  public int sollvalue = 0;

  @PropertyInfo(handle = 1, editable = true, displayName = "Status", type = Types.TYPE_PROPERTIEINFO_TOGGLEACTIVATION, stateful = true, visibleOnREST = true)
  @DashboardInfo(handle = 0, displayName = "Status", stateful = true)
  public boolean activ = false;

  @PropertyInfo(handle = 2, displayName = "Abh. Sens.Temp.", type = Types.TYPE_PROPERTIEINFO_INFO)
  public String concurrentSensorTemperatureInfo = "-";

  @PropertyInfo(handle = 3, displayName = "Abh. Outputs", type = Types.TYPE_PROPERTIEINFO_INFO)
  public String outputsInfo = "-";

  @PropertyInfo(handle = 4, displayName = "Running", type = Types.TYPE_PROPERTIEINFO_INFO)
  @DashboardInfo(handle = 1, displayName = "Running", stateful = true)
  public boolean runningState = false;

  @PropertyInfo(handle = 5, editable = true, displayName = "Force Activ", type = Types.TYPE_PROPERTIEINFO_TOGGLEACTIVATION, stateful = true, visibleOnREST = true)
  @DashboardInfo(handle = 2, displayName = "Force Activ", stateful = true)
  public boolean forceActiv = false;

  @PropertyInfo(handle = 6, editable = true, displayName = "Start-Zeit", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE, propertyType = Types.PROPERTYTYPE_TIME, stepValue = 300, visibleOnREST = true)
  public String timeStart;
  @PropertyInfo(handle = 7, editable = true, displayName = "Stop-Zeit", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE, propertyType = Types.PROPERTYTYPE_TIME, stepValue = 300, visibleOnREST = true)
  public String timeStop;
  @PropertyInfo(handle = 8, editable = true, displayName = "AUS-Zeit Status", type = Types.TYPE_PROPERTIEINFO_TOGGLEACTIVATION, stateful = true, visibleOnREST = true)
  public boolean offTimeActive = false;

  String[] outputs = null;
  String[] concurrentTemperatureIds = null;
  private DateUtils du = null;

  public VentilationDevice(int cycleTime) {
    super(cycleTime);
    this.du = new DateUtils();
    this.ri = new RecorderItem(24, RecorderItem.LEGENDTYPE_DATE_HOUT_MINUTE, (1000 * 60 * 60));
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
      if (forceActiv) {
        runningState = true;
      } else {
        if (checkTemperatureCondition()) {
          runningState = !(offTimeActive && checkOffTime());
        } else {
          runningState = false;
        }
      }
    } else {
      runningState = false;
    }
    setOutputs(runningState);
    if (ri != null) {
      ri.setActive(activ);
      Object data[] = {runningState};
      ri.addItem(new RecorderItem.InsertItem(data), id);
    }
    logger.debug("VentilationDevice <" + id + ">  => STATE <" + activ + "> =>  RUNNINGSTATE <" + runningState + ">" + " =>  FORCE <" + forceActiv + ">");
    super.receiveIdle();
  }

  private void setOutputs(boolean state) {
    if (outputs != null) {
      for (String output : outputs) {
        OutputDevice o = DeviceListUtils.getInstance().getOutputById(output);
        if (o.isValue() != state) {
          o.sendMessage(state);
        }
      }
    }
  }

  private boolean checkTemperatureCondition() {
    boolean state = false;
    if (concurrentTemperatureIds != null) {
      for (String temp : concurrentTemperatureIds) {
        SensorTemperatureDevice o = DeviceListUtils.getInstance().getSensorTemperatureDeviceById(temp);
        if (o != null) {
          state = ((o.getValueAsInt() - sollvalue) >= 0);
          if (state == false) {
            break;
          }
        } else {
          state = false;
          break;
        }
      }
    }
    return state;
  }

  private boolean checkOffTime() {
    return du.checkStatus(timeStart, timeStop);
  }
}
