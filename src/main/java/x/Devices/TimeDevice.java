package x.Devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.DeviceUtils.DateUtils;
import x.DeviceUtils.DeviceListUtils;
import x.DeviceUtils.Types;
import x.utils.DashboardInfo;
import x.utils.PropertyInfo;

public class TimeDevice extends BaseDevice {

  private final Logger logger = LoggerFactory.getLogger(TimeDevice.class);

  @PropertyInfo(handle = 0, editable = true, displayName = "Start-Zeit", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE, propertyType = Types.PROPERTYTYPE_TIME, stepValue = 30)
  public String timeStart;
  @PropertyInfo(handle = 1, editable = true, displayName = "Stop-Zeit", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE, propertyType = Types.PROPERTYTYPE_TIME, stepValue = 30)
  public String timeStop;
  @PropertyInfo(handle = 2, editable = true, displayName = "Status", type = Types.TYPE_PROPERTIEINFO_TOGGLEACTIVATION, stateful = true)
  @DashboardInfo(handle = 0, displayName = "Status", stateful = true)
  public boolean activ = false;

  private DateUtils du = null;
  private boolean started = false;
  private boolean timevalue = false;
  String[] outputs = null;

  public TimeDevice(int cycleTime) {
    super(cycleTime);
    this.du = new DateUtils();
  }

  public void addOutputs(String outputs) {
    this.outputs = outputs.split(";");
  }

  @Override
  public void receiveMessage(Object message) {
    super.receiveMessage(message);
  }

  @Override
  public void receiveIdle() {
    if (activ) {
      timevalue = du.checkStatus(timeStart, timeStop);
      if (timevalue && started == false) {
        String[] dateString = du.getCompleteDateString();
        logger.debug("TIME <" + id + "> => (" + dateString[0] + "," + dateString[1] + ") => STATE <" + activ + ">");
        setOutputs(true);
        started = true;
      } else if (timevalue == false && started) {
        String[] dateString = du.getCompleteDateString();
        logger.debug("TIME <" + id + "> => (" + dateString[0] + "," + dateString[1] + ") => STATE <" + activ + ">");
        setOutputs(false);
        started = false;
      }
    }
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
}
