package x.rest;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import x.DeviceUtils.DeviceListUtils;
import x.DeviceUtils.PropertyChangedEvent;
import x.DeviceUtils.Types;
import x.Devices.DHT22Device;
import x.Devices.HeatingPumpDevice;
import x.Devices.OutputDevice;
import x.Devices.SensorTemperatureDevice;
import x.Devices.TemperatureDevice;
import x.Devices.VentilationDevice;
import x.rest.data.DashboardResponse;
import x.rest.data.devices.RestBaseDevice;
import x.rest.data.devices.RestParam;
import x.utils.PropertyInfo;
import x.utils.FieldParser;
import x.Devices.BaseDevice;
import x.rest.data.StatusRequest;
import x.rest.data.StatusResponse;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/rest/data")
public class RestServerMessageResource {

  private final Logger logger = LoggerFactory.getLogger(RestServerMessageResource.class);

  private DashboardResponse dashboard() {

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    String tsTamp = dateFormat.format(Calendar.getInstance().getTime());

    DashboardResponse sr = new DashboardResponse();
    sr.setError(0);
    sr.setTstamp(tsTamp);

    for (Object deviceHandle : DeviceListUtils.getInstance().getDeviceList()) {
      BaseDevice bdo = (BaseDevice) deviceHandle;
      RestBaseDevice bd = new RestBaseDevice();
      bd.setNameCategory("" + bdo.categorie);
      bd.setHandleDevice("" + bdo.deviceHandle);
      bd.setIdDevice(bdo.id);
      bd.setDisplayNameDevice(bdo.displayName);
      bd.setStyleDevice(bdo.style);
      boolean valid = false;
      if (deviceHandle instanceof SensorTemperatureDevice) {
        bd.setTypeDevice("" + Types.TYPE_SENSOR);
        bd.setSollvalue("" + ((SensorTemperatureDevice) deviceHandle).getSollValueAsFloat());
        bd.setValue("" + ((SensorTemperatureDevice) deviceHandle).getValueAsFloat());
        getPropertyInfosForDevice(deviceHandle, bd);
        valid = true;
      } else if (deviceHandle instanceof DHT22Device) {
        bd.setTypeDevice("" + Types.TYPE_DHT22);
        bd.setActiv("" + ((DHT22Device) deviceHandle).activ);
        bd.setTemperature("" + ((DHT22Device) deviceHandle).getTemperatureAsFloat());
        bd.setHumanity("" + ((DHT22Device) deviceHandle).getHumanityAsFloat());
        getPropertyInfosForDevice(deviceHandle, bd);
        valid = true;
      } else if (deviceHandle instanceof HeatingPumpDevice) {
        bd.setTypeDevice("" + Types.TYPE_HEATINGPUMP);
        bd.setRunningState("" + ((HeatingPumpDevice) deviceHandle).runningState);
        bd.setForceActiv("" + ((HeatingPumpDevice) deviceHandle).forceActiv);
        bd.setActiv("" + ((HeatingPumpDevice) deviceHandle).activ);
        getPropertyInfosForDevice(deviceHandle, bd);
        valid = true;
      } else if (deviceHandle instanceof VentilationDevice) {
        bd.setTypeDevice("" + Types.TYPE_VENTILATION);
        bd.setRunningState("" + ((VentilationDevice) deviceHandle).runningState);
        bd.setForceActiv("" + ((VentilationDevice) deviceHandle).forceActiv);
        bd.setActiv("" + ((VentilationDevice) deviceHandle).activ);
        getPropertyInfosForDevice(deviceHandle, bd);
        valid = true;
      } else if (deviceHandle instanceof OutputDevice) {
        bd.setTypeDevice("" + Types.TYPE_OUTPUT);
        bd.setValue("" + ((OutputDevice) deviceHandle).isValue());
        getPropertyInfosForDevice(deviceHandle, bd);
        valid = true;
      } else if (deviceHandle instanceof TemperatureDevice) {
        bd.setTypeDevice("" + Types.TYPE_TEMPERATURE);
        bd.setSollvalue("" + ((TemperatureDevice) deviceHandle).getSollValueAsFloat());
        bd.setValue("" + ((TemperatureDevice) deviceHandle).getValueAsFloat());
        getPropertyInfosForDevice(deviceHandle, bd);
        valid = true;
      }

      if (valid) {
        sr.getDevices().add(bd);
      }

    }
    return sr;

  }

  private void getPropertyInfosForDevice(Object deviceHandle, RestBaseDevice bd) {
    Field[] fields = deviceHandle.getClass().getFields();
    for (Field f : fields) {
      PropertyInfo p = (PropertyInfo) f.getAnnotation(PropertyInfo.class);
      if (p != null) {
        try {
          if (p.visibleOnREST()) {
            RestParam param = new RestParam();
            param.setHandle("" + p.handle());
            param.setEditable("" + p.editable());
            param.setVisibleValue("" + p.visibleValue());
            param.setType("" + p.type());
            param.setValue(FieldParser.convertField(f, deviceHandle));
            param.setDisplayName(p.displayName());
            bd.getParams().add(param);
          }
        } catch (Exception e) {
          logger.error(e.getMessage());
        }
      }
    }
  }

  private boolean messageToDevice(String message) {
    boolean error = true;
    String[] messageIdentifier = message.split("@");
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
                    error = false;
                    break;
                  }
                }
              }
              break;
            }
          }
        }
      }
    }
    return error;
  }

  @RequestMapping(value = "/dashboardv2", method = RequestMethod.GET)
  @ResponseStatus(value = HttpStatus.OK)
  @ResponseBody
  public DashboardResponse dashboardv2() {
    return dashboard();
  }

  //http://localhost:5005/rest/data/set/p-3005_1@1/true
  @RequestMapping(value = "/setv2", method = RequestMethod.POST)
  @ResponseStatus(value = HttpStatus.OK)
  @ResponseBody
  public StatusResponse setv2(@RequestBody StatusRequest data) {
    boolean error = true;
    if (data != null && data.getValue().length() > 0) {
      error = messageToDevice(data.getValue());
    }
    StatusResponse sr = new StatusResponse();
    sr.setError("" + error);
    sr.setEcho(data.getValue());
    return sr;
  }

}
