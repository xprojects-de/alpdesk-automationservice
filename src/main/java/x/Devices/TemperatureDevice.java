package x.Devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.DeviceUtils.DeviceListUtils;
import x.DeviceUtils.RecorderItem;
import x.DeviceUtils.Types;
import x.utils.DashboardInfo;
import x.utils.PropertyInfo;

public class TemperatureDevice extends BaseDevice {

  private final Logger logger = LoggerFactory.getLogger(TemperatureDevice.class);

  @PropertyInfo(handle = 0, editable = true, displayName = "Sollwert ändern", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE, visibleOnREST = true)
  public int sollvalue = 0;

  @PropertyInfo(handle = 1, displayName = "Aktuelle Temp. (°C)", type = Types.TYPE_PROPERTIEINFO_INFO)
  @DashboardInfo(handle = 0, displayName = "Temp. (°C)")
  public int valueVisu = 1000000;

  @PropertyInfo(handle = 2, displayName = "Schaltschwelle unten (°C)", type = Types.TYPE_PROPERTIEINFO_INFO)
  public int uHysterese = 0;

  @PropertyInfo(handle = 3, displayName = "Schaltschwelle oben (°C)", type = Types.TYPE_PROPERTIEINFO_INFO)
  public int oHysterese = 0;

  @PropertyInfo(handle = 4, editable = true, displayName = "Status", type = Types.TYPE_PROPERTIEINFO_TOGGLEACTIVATION, stateful = true, visibleOnREST = true)
  @DashboardInfo(handle = 1, displayName = "Status", stateful = true)
  public boolean activ = false;

  @PropertyInfo(handle = 5, displayName = "Enabled", type = Types.TYPE_PROPERTIEINFO_INFO)
  @DashboardInfo(handle = 2, displayName = "Enabled", stateful = true)
  public boolean enabled = true;

  @PropertyInfo(handle = 6, displayName = "Abh. Temp.", type = Types.TYPE_PROPERTIEINFO_INFO)
  public String concurrentTemperatureInfo = "-";
  @PropertyInfo(handle = 7, editable = true, displayName = "Abh. Temp. Status", type = Types.TYPE_PROPERTIEINFO_TOGGLEACTIVATION, stateful = true)
  public boolean concurrentTemperatureActive = true;

  public int busAddress = 0;
  String[] outputs = null;
  String[] concurrentTemperatureIds = null;
  private int value = 1000000;
  private boolean outputState = false;

  public TemperatureDevice(int cycleTime) {
    super(cycleTime);
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
      StringBuilder namedconcurrentTemperatureIds = new StringBuilder();
      for (String tId : concurrentTemperatureIds) {
        if (!tId.equals(id)) {
          TemperatureDevice td = DeviceListUtils.getInstance().getTemperatureDeviceById(tId);
          if (td != null) {
            namedconcurrentTemperatureIds.append(td.categorie).append(" (").append(td.displayName).append(")" + ";");
          }
        }
      }
      concurrentTemperatureInfo = namedconcurrentTemperatureIds.toString();
    }
  }

  @Override
  public void receiveMessage(Object message) {
    value = (int) ((short) ((int) message)); // Zweierkomplement => 65535 -> -1
    logger.debug("TEMPERATURE <" + id + "> => <" + value + "> (" + sollvalue + "," + oHysterese + "," + uHysterese + ") => STATE <" + activ + ">");
    if (activ) {
      if (value != 1000000) {
        enabled = checkConcurrentTemperatures();
        if (enabled == true) {
          enabled = checkSensorDevice();
        }
        if (enabled == true) {
          int valueToCompare = value - sollvalue;
          if (valueToCompare > 0) {
            if (valueToCompare >= oHysterese) {
              logger.debug("TEMPERATURE <" + id + "> => <off>");
              for (String output : outputs) {
                OutputDevice o = DeviceListUtils.getInstance().getOutputById(output);
                if (o.isValue() != false) {
                  o.sendMessage(false);
                }
              }
              outputState = false;
            }
          } else if (valueToCompare < 0) {
            valueToCompare = valueToCompare * -1;
            if (valueToCompare >= uHysterese) {
              logger.debug("TEMPERATURE <" + id + "> => <on>");
              for (String output : outputs) {
                OutputDevice o = DeviceListUtils.getInstance().getOutputById(output);
                if (o.isValue() != true) {
                  o.sendMessage(true);
                }
              }
              outputState = true;
            }
          }
        } else {
          logger.debug("TEMPERATURE <" + id + "> => <off disabled>");
          for (String output : outputs) {
            OutputDevice o = DeviceListUtils.getInstance().getOutputById(output);
            if (o.isValue() != false) {
              o.sendMessage(false);
            }
          }
          outputState = false;
        }
      }
      super.receiveMessage(message);
    }
  }

  @Override
  public void receiveIdle() {
    if (activ == false) {
      for (String output : outputs) {
        OutputDevice o = DeviceListUtils.getInstance().getOutputById(output);
        if (o.isValue() != false) {
          o.sendMessage(false);
        }
      }
      outputState = false;
    }

    if (ri != null) {
      ri.setActive(activ);
      Object data[] = {getValueAsFloat(), outputState, getSollValueAsFloat()};
      ri.addItem(new RecorderItem.InsertItem(data), id);
    }

    valueVisu = value;
    super.receiveIdle();
  }

  public float getValueAsFloat() {
    return (float) (value / 10.0);
  }

  public float getSollValueAsFloat() {
    return (float) (sollvalue / 10.0);
  }

  private boolean checkConcurrentTemperatures() {
    boolean enableDevice = true;
    if (concurrentTemperatureIds != null && concurrentTemperatureActive) {
      for (String tId : concurrentTemperatureIds) {
        if (!tId.equals(id)) {
          TemperatureDevice td = DeviceListUtils.getInstance().getTemperatureDeviceById(tId);
          if (td != null) {
            int triggerTemperatureLevel = td.sollvalue - td.uHysterese;
            if (td.value < triggerTemperatureLevel) {
              enableDevice = false;
              logger.debug("TEMPERATURE <" + id + "> disabled because TEMPERATURE <" + td.id + "> to low");
              break;
            }
          }
        }
      }
    }
    return enableDevice;
  }

  private boolean checkSensorDevice() {
    boolean enableDevice = true;
    for (Object deviceHandleList : DeviceListUtils.getInstance().getDeviceList()) {
      if (deviceHandleList instanceof SensorTemperatureDevice) {
        if (((SensorTemperatureDevice) deviceHandleList).activ == true) {
          String[] concurrentIds = ((SensorTemperatureDevice) deviceHandleList).getConcurrentTemperatureIds();
          if (concurrentIds != null) {
            for (String cId : concurrentIds) {
              if (cId.equals(id)) {
                if (((SensorTemperatureDevice) deviceHandleList).getStatus() == false) {
                  enableDevice = false;
                  logger.debug("TEMPERATURE <" + id + "> disabled because SENSOR <" + ((SensorTemperatureDevice) deviceHandleList).id + "> status");
                  break;
                }
              }
            }
          }
        }
      }
    }
    return enableDevice;
  }

  public boolean isOutputState() {
    return outputState;
  }

}
