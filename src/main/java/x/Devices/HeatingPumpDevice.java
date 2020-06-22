package x.Devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.DeviceUtils.DateUtils;
import x.DeviceUtils.DeviceListUtils;
import x.DeviceUtils.RecorderItem;
import x.DeviceUtils.Types;
import x.utils.DashboardInfo;
import x.utils.PropertyInfo;

public class HeatingPumpDevice extends BaseDevice {

  private final Logger logger = LoggerFactory.getLogger(HeatingPumpDevice.class);
  private static final int WATERINDEX = 0;
  private static final int PUFFERINDEX = 1;
  private static final int FLOWINDEX = 2;
  private static final int PUMPINDEX = 0;
  private static final int FLOWWARMINDEX = 1;
  private static final int FLOWCOLDINDEX = 2;
  private static final int FLOWSHUTDOWNTIME = 180000;
  private static final int FLOWHYSTERESE = 20;
  private static final int FLOWSECURITSHUTDOWN = 490;
  // 140s komplettlauf => 2% = 2,8s
  private static final int FLOWTHREADWAIT = 2800;

  @PropertyInfo(handle = 0, editable = true, displayName = "Status", type = Types.TYPE_PROPERTIEINFO_TOGGLEACTIVATION, stateful = true, visibleOnREST = true)
  @DashboardInfo(handle = 0, displayName = "Status", stateful = true)
  public boolean activ = false;

  @PropertyInfo(handle = 1, displayName = "Abh. Sens.Temp.", type = Types.TYPE_PROPERTIEINFO_INFO)
  public String concurrentSensorTemperatureInfo = "-";

  @PropertyInfo(handle = 2, displayName = "Abh. Outputs", type = Types.TYPE_PROPERTIEINFO_INFO)
  public String outputsInfo = "-";

  @PropertyInfo(handle = 3, editable = true, displayName = "Start-Zeit", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE, propertyType = Types.PROPERTYTYPE_TIME, stepValue = 30, visibleOnREST = true)
  public String timeStart;

  @PropertyInfo(handle = 4, editable = true, displayName = "Stop-Zeit", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE, propertyType = Types.PROPERTYTYPE_TIME, stepValue = 30, visibleOnREST = true)
  public String timeStop;

  @PropertyInfo(handle = 5, displayName = "Running", type = Types.TYPE_PROPERTIEINFO_INFO)
  @DashboardInfo(handle = 1, displayName = "Running", stateful = true)
  public boolean runningState = false;

  @PropertyInfo(handle = 6, editable = true, displayName = "Force Activ", type = Types.TYPE_PROPERTIEINFO_TOGGLEACTIVATION, stateful = true, visibleOnREST = true)
  @DashboardInfo(handle = 2, displayName = "Force Activ", stateful = true)
  public boolean forceActiv = false;

  String[] outputs = null;
  String[] concurrentTemperatureIds = null;
  private DateUtils du = null;

  public HeatingPumpDevice(int cycleTime) {
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
    if (activ && checkAnyTemperatureDevicesIsActive()) {
      boolean outputState = false;
      boolean checkWaterCondition = checkTemperatureCondition(HeatingPumpDevice.WATERINDEX);
      boolean checkPufferCondition = checkTemperatureCondition(HeatingPumpDevice.PUFFERINDEX);
      if (forceActiv) {
        outputState = checkPufferCondition;
      } else {
        if (checkTime()) {
          outputState = checkPufferCondition;
        } else {
          outputState = (checkWaterCondition && checkPufferCondition);
        }
      }
      if (outputState) {
        outputState = checkFlowTemperatureForPumpSecurityShutdown();
      }
      setOutput(HeatingPumpDevice.PUMPINDEX, outputState);
      setFlowTemperature(outputState);
      runningState = outputState;
      String[] dateString = du.getCompleteDateString();
      logger.debug("HEATINGPUMP <" + id + "> => (" + dateString[0] + "," + dateString[1] + ") => STATE <" + activ + "> WATERCHECK <" + checkWaterCondition + "> PUFFERCHECK <" + checkPufferCondition + "> OUTPUTSTATE <" + outputState + "> RUNNINGSTATE <" + runningState + ">" + " =>  FORCE <" + forceActiv + ">");
    } else {
      setOutput(HeatingPumpDevice.PUMPINDEX, false);
      setFlowTemperature(false);
      runningState = false;
    }
    if (ri != null) {
      ri.setActive(activ);
      Object data[] = {runningState};
      ri.addItem(new RecorderItem.InsertItem(data), id);
    }
    super.receiveIdle();
  }

  private void setOutput(int index, boolean state) {
    if (outputs != null) {
      if (outputs.length >= (index + 1)) {
        OutputDevice o = DeviceListUtils.getInstance().getOutputById(outputs[index]);
        if (o != null) {
          if (o.isValue() != state) {
            o.sendMessage(state);
          }
        }
      }
    }
  }

  private boolean checkTime() {
    return du.checkStatus(timeStart, timeStop);
  }

  private boolean checkTemperatureCondition(int index) {
    boolean state = false;
    if (concurrentTemperatureIds != null) {
      if (concurrentTemperatureIds.length >= (index + 1)) {
        SensorTemperatureDevice o = DeviceListUtils.getInstance().getSensorTemperatureDeviceById(concurrentTemperatureIds[index]);
        if (o != null) {
          state = ((o.getValueAsInt() - o.getSollValueAsInt()) > 0);
        }
      }
    }
    return state;
  }

  private boolean checkFlowTemperatureForPumpSecurityShutdown() {
    boolean state = false;
    if (concurrentTemperatureIds != null) {
      if (concurrentTemperatureIds.length >= (HeatingPumpDevice.FLOWINDEX + 1)) {
        SensorTemperatureDevice o = DeviceListUtils.getInstance().getSensorTemperatureDeviceById(concurrentTemperatureIds[HeatingPumpDevice.FLOWINDEX]);
        if (o != null) {
          state = ((HeatingPumpDevice.FLOWSECURITSHUTDOWN - o.getValueAsInt()) > 0);
        }
      }
    }
    return state;
  }

  private void setFlowTemperature(boolean statePump) {
    boolean triggered = false;
    if (statePump) {
      if (concurrentTemperatureIds != null) {
        if (concurrentTemperatureIds.length >= (HeatingPumpDevice.FLOWINDEX + 1)) {
          SensorTemperatureDevice o = DeviceListUtils.getInstance().getSensorTemperatureDeviceById(concurrentTemperatureIds[HeatingPumpDevice.FLOWINDEX]);
          if (o != null) {
            resetFlowShutdown();
            triggered = true;
            int valueToCompare = o.getValueAsInt() - o.getSollValueAsInt();
            if (valueToCompare > 0) {
              if (valueToCompare >= HeatingPumpDevice.FLOWHYSTERESE) {
                setFlowTemperatureMixer(false);
              }
            } else if (valueToCompare < 0) {
              valueToCompare = valueToCompare * -1;
              if (valueToCompare >= HeatingPumpDevice.FLOWHYSTERESE) {
                setFlowTemperatureMixer(true);
              }
            }
          }
        }
      }
    }
    if (triggered == false) {
      setFlowShutdown();
    }
  }

  private void setFlowTemperatureMixer(boolean state) {
    if (state) {
      setOutput(HeatingPumpDevice.FLOWCOLDINDEX, false);
      setOutput(HeatingPumpDevice.FLOWWARMINDEX, true);
      try {
        Thread.sleep(HeatingPumpDevice.FLOWTHREADWAIT);
      } catch (InterruptedException ex) {
      }
      setOutput(HeatingPumpDevice.FLOWWARMINDEX, false);
    } else {
      setOutput(HeatingPumpDevice.FLOWWARMINDEX, false);
      setOutput(HeatingPumpDevice.FLOWCOLDINDEX, true);
      try {
        Thread.sleep(HeatingPumpDevice.FLOWTHREADWAIT);
      } catch (InterruptedException ex) {
      }
      setOutput(HeatingPumpDevice.FLOWCOLDINDEX, false);
    }

  }

  private long shutdownFlowTemperatureStart = 0;

  public void setFlowShutdown() {
    if (shutdownFlowTemperatureStart == 0) {
      setOutput(HeatingPumpDevice.FLOWWARMINDEX, false);
      setOutput(HeatingPumpDevice.FLOWCOLDINDEX, true);
      shutdownFlowTemperatureStart = System.currentTimeMillis();
    }
    if ((shutdownFlowTemperatureStart + HeatingPumpDevice.FLOWSHUTDOWNTIME) <= System.currentTimeMillis()) {
      setOutput(HeatingPumpDevice.FLOWCOLDINDEX, false);
    }
  }

  private void resetFlowShutdown() {
    if (shutdownFlowTemperatureStart != 0) {
      setOutput(HeatingPumpDevice.FLOWCOLDINDEX, false);
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
      }
    }
    shutdownFlowTemperatureStart = 0;
  }

  private boolean checkAnyTemperatureDevicesIsActive() {
    return DeviceListUtils.getInstance().anyTemperatureDeviceIsActive();
  }

}
