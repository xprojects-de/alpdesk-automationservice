package x.Devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.DeviceUtils.DeviceListUtils;
import x.DeviceUtils.RecorderItem;
import x.DeviceUtils.Types;
import x.utils.DashboardInfo;
import x.utils.ExecCommander;
import x.utils.PropertyInfo;

public class DHT22Device extends BaseDevice {

  private final Logger logger = LoggerFactory.getLogger(DHT22Device.class);

  @PropertyInfo(handle = 0, displayName = "Temp.", type = Types.TYPE_PROPERTIEINFO_INFO)
  @DashboardInfo(handle = 0, displayName = "Temp.")
  public String temperature = "";

  @PropertyInfo(handle = 1, displayName = "Hum.", type = Types.TYPE_PROPERTIEINFO_INFO)
  @DashboardInfo(handle = 1, displayName = "Hum.")
  public String humanity = "";

  @PropertyInfo(handle = 2, editable = true, displayName = "Status", type = Types.TYPE_PROPERTIEINFO_TOGGLEACTIVATION, stateful = true)
  @DashboardInfo(handle = 2, displayName = "Status", stateful = true)
  public boolean activ = true;

  private String commandExec = "";
  private String commandParams = "";
  private boolean commandSetWritable = false;
  private final int commandTimeout = 15000;
  private final ExecCommander dht22ExecCommander = new ExecCommander();

  public DHT22Device(int cycleTime) {
    super(cycleTime);
    this.ri = new RecorderItem(24, RecorderItem.LEGENDTYPE_DATE_HOUT_MINUTE, (1000 * 60 * 60));
  }

  public void addExec(String command, String params, boolean setWritable) {
    this.commandExec = command;
    this.commandParams = params;
    this.commandSetWritable = setWritable;
  }

  private void setErrorInfo() {
    temperature = "-";
    humanity = "-";
  }

  @Override
  public void receiveMessage(Object message) {
    super.receiveMessage(message);
  }

  @Override
  public void receiveIdle() {
    if (activ) {
      if (!commandExec.equals("")) {
        try {
          ExecCommander.RetVal value = dht22ExecCommander.executeCommand(commandExec, commandParams, commandSetWritable, commandTimeout);
          if (value.isError() || value.isKilled()) {
            setErrorInfo();
          } else {
            for (String s : value.getReturnvalues()) {
              if (!s.equals("")) {
                String[] values = s.split(";");
                if (values.length == 2) {
                  temperature = values[0];
                  humanity = values[1];
                  logger.debug("DHT22 <" + id + "> => <" + temperature + "> <" + humanity + "> => STATE <" + activ + ">");
                } else {
                  setErrorInfo();
                }
              } else {
                setErrorInfo();
              }
              break;
            }
          }
        } catch (Exception ex) {
          ex.printStackTrace();
          setErrorInfo();
        }
      } else {
        setErrorInfo();
      }
      super.receiveIdle();
    } else {
      setErrorInfo();
    }

    if (ri != null) {
      ri.setActive(activ);
      Object data[] = {getTemperatureAsFloat(), getHumanityAsFloat()};
      ri.addItem(new RecorderItem.InsertItem(data), id);
    }
  }

  public float getTemperatureAsFloat() {
    float result = (float) 0.0;
    if (!temperature.equals("-")) {
      String[] split = temperature.split(" ");
      if (split.length == 2) {
        try {
          result = Float.parseFloat(split[0]);
        } catch (Exception e) {
          result = (float) 0.0;
        }
      }
    }
    return result;
  }

  public float getHumanityAsFloat() {
    float result = (float) 0.0;
    if (!humanity.equals("-")) {
      String[] split = humanity.split(" ");
      if (split.length == 2) {
        try {
          result = Float.parseFloat(split[0]);
        } catch (Exception e) {
          result = (float) 0.0;
        }
      }
    }
    return result;
  }

}
