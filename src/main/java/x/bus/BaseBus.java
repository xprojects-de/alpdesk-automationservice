package x.bus;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.DeviceUtils.DeviceListUtils;
import x.Devices.BaseDevice;
import x.Devices.OutputDevice;
import x.MessageHandling.MessageHandler;
import x.utils.DashboardInfo;
import x.utils.FieldParser;
import x.utils.PropertyInfo;
import x.websocket.model.AsyncStatusMessage;
import java.util.Calendar;

public class BaseBus {

  private final Logger logger = LoggerFactory.getLogger(BaseBus.class);
  private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  public void start() {

  }

  public void updateDeviceHandleStatusForWebSocket(int cycleTime, long elapsedTime, long worstCycleTime) {
    AsyncStatusMessage globalASM = new AsyncStatusMessage();
    globalASM.setKind(AsyncStatusMessage.MULTISET);
    globalASM.setError(AsyncStatusMessage.NOERROR);
    globalASM.getAsm().add(new AsyncStatusMessage(AsyncStatusMessage.NOERROR, AsyncStatusMessage.SET, "-10", cycleTime + "|" + elapsedTime + "|" + worstCycleTime));
    //MessageHandler.getInstance().messageToWebSocketClients(new AsyncStatusMessage(AsyncStatusMessage.NOERROR, AsyncStatusMessage.SET, "-10", cycleTime + "|" + elapsedTime + "|" + worstCycleTime));
    for (Object deviceHandle : DeviceListUtils.getInstance().getDeviceList()) {
      if (deviceHandle instanceof OutputDevice) {
        globalASM.getAsm().add(new AsyncStatusMessage(AsyncStatusMessage.NOERROR, AsyncStatusMessage.SET, "" + ((OutputDevice) deviceHandle).deviceHandle, (((OutputDevice) deviceHandle).isValue() == true ? "1" : "0")));
        //MessageHandler.getInstance().messageToWebSocketClients(new AsyncStatusMessage(AsyncStatusMessage.NOERROR, AsyncStatusMessage.SET, "" + ((OutputDevice) deviceHandle).deviceHandle, (((OutputDevice) deviceHandle).isValue() == true ? "1" : "0")));
      }
      Field[] fields = deviceHandle.getClass().getFields();
      for (Field f : fields) {

        // PropertyInfos
        PropertyInfo p = (PropertyInfo) f.getAnnotation(PropertyInfo.class);
        if (p != null) {
          try {
            String value2Send = FieldParser.convertField(f, deviceHandle);
            if (p.stateful()) {
              if (FieldParser.convertFieldState(f, deviceHandle)) {
                value2Send = value2Send + "|xactive";
              }
            }
            //MessageHandler.getInstance().messageToWebSocketClients(new AsyncStatusMessage(AsyncStatusMessage.NOERROR, AsyncStatusMessage.SET, "p" + ((BaseDevice) deviceHandle).deviceHandle + "_" + p.handle(), value2Send));
            globalASM.getAsm().add(new AsyncStatusMessage(AsyncStatusMessage.NOERROR, AsyncStatusMessage.SET, "p" + ((BaseDevice) deviceHandle).deviceHandle + "_" + p.handle(), value2Send));
          } catch (Exception e) {
            logger.error(e.getMessage());
          }
        }

        // DashboardInfos
        if (((BaseDevice) deviceHandle).dashboard == true) {
          DashboardInfo d = (DashboardInfo) f.getAnnotation(DashboardInfo.class);
          if (d != null) {
            try {
              String value2Send = FieldParser.convertField(f, deviceHandle);
              if (d.stateful()) {
                if (FieldParser.convertFieldState(f, deviceHandle)) {
                  value2Send = value2Send + "|xactive";
                }
              }
              //MessageHandler.getInstance().messageToWebSocketClients(new AsyncStatusMessage(AsyncStatusMessage.NOERROR, AsyncStatusMessage.SET, "d" + ((BaseDevice) deviceHandle).deviceHandle + "_" + d.handle(), value2Send));
              globalASM.getAsm().add(new AsyncStatusMessage(AsyncStatusMessage.NOERROR, AsyncStatusMessage.SET, "d" + ((BaseDevice) deviceHandle).deviceHandle + "_" + d.handle(), value2Send));
            } catch (Exception e) {
              logger.error(e.getMessage());
            }
          }
        }
      }
    }

    MessageHandler.getInstance().messageToWebSocketClients(globalASM);

    try {
      DeviceListUtils.getInstance().setLastBusUpdate(dateFormat.format(Calendar.getInstance().getTime()));
    } catch (Exception e) {
      DeviceListUtils.getInstance().setLastBusUpdate("-");
    }

  }
}
