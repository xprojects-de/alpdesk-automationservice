package x.Devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.DeviceUtils.DateUtils;
import x.DeviceUtils.DeviceListUtils;
import x.DeviceUtils.Types;
import x.MessageHandling.MessageHandler;
import x.utils.DashboardInfo;
import x.utils.PropertyInfo;
import x.websocket.model.AsyncStatusMessage;

public class OutputDevice extends BaseDevice {

  private final Logger logger = LoggerFactory.getLogger(OutputDevice.class);

  @PropertyInfo(handle = 0, editable = true, displayName = "Start-Zeit", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE, propertyType = Types.PROPERTYTYPE_TIME, stepValue = 60)
  public String timeStart = "-";

  @PropertyInfo(handle = 1, editable = true, displayName = "Stop-Zeit", type = Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE, propertyType = Types.PROPERTYTYPE_TIME, stepValue = 60)
  public String timeStop = "-";

  @PropertyInfo(handle = 2, displayName = "Status", type = Types.TYPE_PROPERTIEINFO_INFO)
  @DashboardInfo(handle = 0, displayName = "Status", stateful = true)
  public boolean valueVisu = false;

  private DateUtils du = null;
  private boolean value = false;
  public int busAddress = 0;
  String[] concurrentOutputs = null;

  public OutputDevice(int cycleTime) {
    super(cycleTime);
    this.du = new DateUtils();
  }

  public void setTimeSettings(String timeStart, String timeStop) {
    this.timeStart = timeStart;
    this.timeStop = timeStop;
  }

  public void addConcurrentOutputs(String concurrentOutputs) {
    if (!concurrentOutputs.equals("")) {
      this.concurrentOutputs = concurrentOutputs.split(";");
    }
  }

  @Override
  public void receiveMessage(Object message) {
    if (concurrentOutputs != null && concurrentOutputs.length > 0 && ((boolean) message) == true) {
      boolean valid = true;
      for (String output : concurrentOutputs) {
        OutputDevice o = DeviceListUtils.getInstance().getOutputById(output);
        if (o != null && o.isValue()) {
          valid = false;
          break;
        }
      }
      value = checkTimeSettings(valid);
    } else {
      value = checkTimeSettings((boolean) message);
    }

    valueVisu = value;

    logger.debug("OUTPUT <" + id + "> => <" + value + ">");

    AsyncStatusMessage asm = new AsyncStatusMessage();
    asm.setId("" + deviceHandle);
    asm.setValue((value == true ? "1" : "0"));
    asm.setKind(AsyncStatusMessage.SET);
    MessageHandler.getInstance().messageToWebSocketClients(asm);
    super.receiveMessage(message);
  }

  @Override
  public void receiveIdle() {
    super.receiveIdle();
  }

  public boolean isValue() {
    return value;
  }

  private boolean checkTimeSettings(boolean currentStateValue) {
    boolean returnValue = currentStateValue;
    if (currentStateValue == true && du != null && !timeStart.equals("-") && !timeStop.equals("-")) {
      returnValue = du.checkStatus(timeStart, timeStop);
      String[] dateString = du.getCompleteDateString();
      logger.debug("OUTPUT TIMECHECK <" + id + "> => <" + returnValue + "><" + dateString[0] + "><" + dateString[1] + ">");
    }
    return returnValue;
  }

}
