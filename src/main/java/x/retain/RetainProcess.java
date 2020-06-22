package x.retain;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import x.DeviceUtils.DeviceListUtils;
import x.DeviceUtils.PropertyChangedEvent;
import x.Devices.BaseDevice;
import x.Devices.DimmerDevice;
import x.Devices.HeatingPumpDevice;
import x.Devices.SensorTemperatureDevice;
import x.Devices.TemperatureDevice;
import x.Devices.TimeDevice;
import x.Devices.OutputDevice;
import x.Devices.ShadingDevice;
import x.Devices.VentilationDevice;
import x.retain.database.Retainvariables;
import x.retain.database.RetainvariablesRepository;
import x.utils.FieldParser;
import x.utils.PropertyInfo;

@Service
public class RetainProcess {

  private final Logger logger = LoggerFactory.getLogger(RetainProcess.class);

  @Autowired
  private RetainvariablesRepository retainvariablesRepository;

  public RetainProcess() {
    logger.debug("RetainProcess initialized");
  }

  public void runonce() throws Exception {
    Iterable<Retainvariables> entries = retainvariablesRepository.findAll();
    for (Retainvariables rv : entries) {
      for (BaseDevice deviceHandle : DeviceListUtils.getInstance().getDeviceList()) {
        if (deviceHandle.deviceHandle == rv.getDevicehandle()) {
          updateProperty(deviceHandle, rv.getPropertyhandle(), rv.getPropertyvalue());
          break;
        }
      }
    }
  }

  private void updateProperty(BaseDevice deviceHandleObject, int properryHandle, String propertyValue) {
    Field[] fields = deviceHandleObject.getClass().getFields();
    for (Field f : fields) {
      PropertyInfo p = (PropertyInfo) f.getAnnotation(PropertyInfo.class);
      if (p != null) {
        if (p.handle() == properryHandle) {
          if (p.stateful() || p.editable()) {
            deviceHandleObject.sendPropertyChangedEvent(new PropertyChangedEvent(f, p, propertyValue, true));
          }
          break;
        }
      }
    }
  }

  private void getProperties(BaseDevice deviceHandleObject) throws Exception {
    Field[] fields = deviceHandleObject.getClass().getFields();
    for (Field f : fields) {
      PropertyInfo p = (PropertyInfo) f.getAnnotation(PropertyInfo.class);
      if (p != null) {
        if (p.stateful() || p.editable()) {
          int deviceHandle = deviceHandleObject.deviceHandle;
          int propertyHandle = p.handle();
          String propertyValue = FieldParser.convertField(f, deviceHandleObject);
          String displayName = p.displayName();
          LocalDate ld = LocalDate.now();
          Retainvariables rv = retainvariablesRepository.findByDevicehandleAndPropertyhandle(deviceHandle, propertyHandle);
          if (rv == null) {
            rv = new Retainvariables();
            rv.setDevicehandle(deviceHandle);
            rv.setPropertyhandle(propertyHandle);
            rv.setPropertyname(displayName);
          }
          rv.setPropertyvalue(propertyValue);
          rv.setTstamp(ld.atTime(LocalTime.now()));
          retainvariablesRepository.save(rv);
        }
      }
    }
  }

  public void execute() throws Exception {
    for (BaseDevice deviceHandle : DeviceListUtils.getInstance().getDeviceList()) {
      if ((deviceHandle instanceof TemperatureDevice)
              || (deviceHandle instanceof HeatingPumpDevice)
              || (deviceHandle instanceof SensorTemperatureDevice)
              || (deviceHandle instanceof TimeDevice)
              || (deviceHandle instanceof VentilationDevice)
              || (deviceHandle instanceof OutputDevice)
              || (deviceHandle instanceof ShadingDevice)
              || (deviceHandle instanceof DimmerDevice)) {
        getProperties(deviceHandle);
      }
    }
  }

}
