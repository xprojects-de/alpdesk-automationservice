package x.MessageHandling;

import java.lang.reflect.Field;
import x.DeviceUtils.DeviceListUtils;
import x.DeviceUtils.PropertyChangedEvent;
import x.Devices.InputDevice;
import x.Devices.SceneDevice;
import x.Devices.BaseDevice;
import x.utils.PropertyInfo;
import x.websocket.model.AsyncStatusMessage;
import x.websocket.model.ConnectionManager;

public class MessageHandler {

  private static MessageHandler singleton = null;
  private ConnectionManager cm = null;

  public MessageHandler() {
  }

  synchronized public static MessageHandler getInstance() {
    if (singleton == null) {
      singleton = new MessageHandler();
    }
    return singleton;
  }

  public void setConnectionManager(ConnectionManager cm) {
    if (this.cm == null) {
      this.cm = cm;
    }
  }

  synchronized public void messageToWebSocketClients(AsyncStatusMessage messageToSend) {
    if (this.cm != null) {
      synchronized (this.cm.getConnections()) {
        this.cm.cleanConnections();
        if (this.cm.getConnections().size() > 0) {
          for (ConnectionManager.Status u : this.cm.getConnections()) {
            if (u.isInitDone()) {
              this.cm.sendMessage(u.getUuid(), messageToSend);
            }
          }
        }
      }
    }
  }

  synchronized public void messageToDevice(String message) {
    String[] messageIdentifier = message.split("#");
    if (messageIdentifier.length == 2) {
      if (messageIdentifier[0].startsWith("p")) {
        String[] handleObject = messageIdentifier[0].substring(1, messageIdentifier[0].length()).split("_");
        if (handleObject.length == 2) {
          int deviceHandle = Integer.parseInt(handleObject[0]);
          int propertyHandle = Integer.parseInt(handleObject[1]);
          for (Object deviceHandleMap : DeviceListUtils.getInstance().getDeviceList()) {
            if (((BaseDevice) deviceHandleMap).deviceHandle == deviceHandle) {
              Field[] fields = ((BaseDevice) deviceHandleMap).getClass().getFields();
              for (Field f : fields) {
                PropertyInfo p = (PropertyInfo) f.getAnnotation(PropertyInfo.class);
                if (p != null) {
                  if (p.handle() == propertyHandle) {
                    ((BaseDevice) deviceHandleMap).sendPropertyChangedEvent(new PropertyChangedEvent(f, p, messageIdentifier[1], false));
                    break;
                  }
                }
              }
              break;
            }
          }
        }
      } else if (messageIdentifier[0].equals("scene")) {
        SceneDevice d = null;
        if (messageIdentifier[1].startsWith("-") && messageIdentifier[1].length() == 5) {
          d = DeviceListUtils.getInstance().getSceneDeviceByHandle(Integer.parseInt(messageIdentifier[1]));
        } else {
          d = DeviceListUtils.getInstance().getSceneDeviceBySpeechIdent(messageIdentifier[1]);
        }
        if (d != null) {
          d.sendMessage(true);
        }
      } else if (!messageIdentifier[0].equals("") && !messageIdentifier[0].equals("undefined")) {
        int deviceHandle = Integer.parseInt(messageIdentifier[0]);
        for (Object inputMap : DeviceListUtils.getInstance().getDeviceList()) {
          if (inputMap instanceof InputDevice) {
            if (((InputDevice) inputMap).deviceHandle == deviceHandle) {
              ((InputDevice) inputMap).setLockedByVisu(messageIdentifier[1].equals("1"));
              ((InputDevice) inputMap).sendMessage(messageIdentifier[1].equals("1"));
              AsyncStatusMessage asm = new AsyncStatusMessage();
              asm.setKind(AsyncStatusMessage.SET);
              asm.setId(messageIdentifier[0]);
              asm.setValue(messageIdentifier[1]);
              messageToWebSocketClients(asm);
              break;
            }
          }
        }
      }
    }
  }
}
