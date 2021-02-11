package x.DeviceUtils;

import java.util.ArrayList;
import java.util.List;
import x.Devices.AnalogInDevice;
import x.Devices.BaseDevice;
import x.Devices.DHT22Device;
import x.Devices.DimmerDevice;
import x.Devices.HeatingPumpDevice;
import x.Devices.InputDevice;
import x.Devices.OutputDevice;
import x.Devices.SceneDevice;
import x.Devices.SensorTemperatureDevice;
import x.Devices.ShadingDevice;
import x.Devices.TemperatureDevice;
import x.Devices.TimeDevice;
import x.Devices.VentilationDevice;

public class DeviceListUtils {

  List<BaseDevice> deviceList = null;
  private int maxInputBusaddress = -1;
  private int maxOutputBusaddress = -1;
  private int maxAnalogInBusaddress = -1;
  private String lastBusUpdate = "";

  private static DeviceListUtils singleton = null;

  public DeviceListUtils() {
  }

  public static DeviceListUtils getInstance() {
    if (singleton == null) {
      singleton = new DeviceListUtils();
    }
    return singleton;
  }

  public void setDeviceList(List<BaseDevice> deviceList) {
    this.deviceList = deviceList;
  }

  public List<BaseDevice> getDeviceList() {
    return this.deviceList;
  }

  public String getLastBusUpdate() {
    return lastBusUpdate;
  }

  public void setLastBusUpdate(String lastBusUpdate) {
    this.lastBusUpdate = lastBusUpdate;
  }

  public ArrayList<InputDevice> getInputDevicesByCategorie(String categorie) {
    ArrayList<InputDevice> list = new ArrayList<>();
    for (Object device : deviceList) {
      if (device instanceof InputDevice) {
        InputDevice temp = (InputDevice) device;
        if (temp.categorie.equals(categorie)) {
          list.add(temp);
        }
      }
    }
    return list;
  }

  public ArrayList<OutputDevice> getOutputDevicesByCategorie(String categorie) {
    ArrayList<OutputDevice> list = new ArrayList<>();
    for (Object device : deviceList) {
      if (device instanceof OutputDevice) {
        OutputDevice temp = (OutputDevice) device;
        if (temp.categorie.equals(categorie)) {
          list.add(temp);
        }
      }
    }
    return list;
  }

  public ArrayList<DimmerDevice> getDimmerDevicesByCategorie(String categorie) {
    ArrayList<DimmerDevice> list = new ArrayList<>();
    for (Object device : deviceList) {
      if (device instanceof DimmerDevice) {
        DimmerDevice temp = (DimmerDevice) device;
        if (temp.categorie.equals(categorie)) {
          list.add(temp);
        }
      }
    }
    return list;
  }

  public ArrayList<DHT22Device> getDHT22DevicesByCategorie(String categorie) {
    ArrayList<DHT22Device> list = new ArrayList<>();
    for (Object device : deviceList) {
      if (device instanceof DHT22Device) {
        DHT22Device temp = (DHT22Device) device;
        if (temp.categorie.equals(categorie)) {
          list.add(temp);
        }
      }
    }
    return list;
  }

  public ArrayList<HeatingPumpDevice> getHeatingPumpDevicesByCategorie(String categorie) {
    ArrayList<HeatingPumpDevice> list = new ArrayList<>();
    for (Object device : deviceList) {
      if (device instanceof HeatingPumpDevice) {
        HeatingPumpDevice temp = (HeatingPumpDevice) device;
        if (temp.categorie.equals(categorie)) {
          list.add(temp);
        }
      }
    }
    return list;
  }

  public ArrayList<VentilationDevice> getVentilationDevicesByCategorie(String categorie) {
    ArrayList<VentilationDevice> list = new ArrayList<>();
    for (Object device : deviceList) {
      if (device instanceof VentilationDevice) {
        VentilationDevice temp = (VentilationDevice) device;
        if (temp.categorie.equals(categorie)) {
          list.add(temp);
        }
      }
    }
    return list;
  }

  public ArrayList<ShadingDevice> getShadingDevicesByCategorie(String categorie) {
    ArrayList<ShadingDevice> list = new ArrayList<>();
    for (Object device : deviceList) {
      if (device instanceof ShadingDevice) {
        ShadingDevice temp = (ShadingDevice) device;
        if (temp.categorie.equals(categorie)) {
          list.add(temp);
        }
      }
    }
    return list;
  }

  public ArrayList<AnalogInDevice> getAnalogInDevicesByCategorie(String categorie) {
    ArrayList<AnalogInDevice> list = new ArrayList<>();
    for (Object device : deviceList) {
      if (device instanceof AnalogInDevice) {
        AnalogInDevice temp = (AnalogInDevice) device;
        if (temp.categorie.equals(categorie)) {
          list.add(temp);
        }
      }
    }
    return list;
  }

  public ArrayList<TemperatureDevice> getTemperatureDevicesByCategorie(String categorie) {
    ArrayList<TemperatureDevice> list = new ArrayList<>();
    for (Object device : deviceList) {
      if (device instanceof TemperatureDevice) {
        TemperatureDevice temp = (TemperatureDevice) device;
        if (temp.categorie.equals(categorie)) {
          list.add(temp);
        }
      }
    }
    return list;
  }

  public ArrayList<SensorTemperatureDevice> getSensorTemperatureDevicesByCategorie(String categorie) {
    ArrayList<SensorTemperatureDevice> list = new ArrayList<>();
    for (Object device : deviceList) {
      if (device instanceof SensorTemperatureDevice) {
        SensorTemperatureDevice temp = (SensorTemperatureDevice) device;
        if (temp.categorie.equals(categorie)) {
          list.add(temp);
        }
      }
    }
    return list;
  }

  public ArrayList<SceneDevice> getSceneDevicesByCategorie(String categorie) {
    ArrayList<SceneDevice> list = new ArrayList<>();
    for (Object device : deviceList) {
      if (device instanceof SceneDevice) {
        SceneDevice temp = (SceneDevice) device;
        if (temp.categorie.equals(categorie)) {
          list.add(temp);
        }
      }
    }
    return list;
  }

  public ArrayList<TimeDevice> getTimeDevicesByCategorie(String categorie) {
    ArrayList<TimeDevice> list = new ArrayList<>();
    for (Object device : deviceList) {
      if (device instanceof TimeDevice) {
        TimeDevice temp = (TimeDevice) device;
        if (temp.categorie.equals(categorie)) {
          list.add(temp);
        }
      }
    }
    return list;
  }

  public OutputDevice getOutputById(String id) {
    OutputDevice d = null;
    for (Object device : deviceList) {
      if (device instanceof OutputDevice) {
        if (((OutputDevice) device).id.equals(id)) {
          d = (OutputDevice) device;
          break;
        }
      }
    }
    return d;
  }

  public TemperatureDevice getTemperatureDeviceById(String id) {
    TemperatureDevice d = null;
    for (Object device : deviceList) {
      if (device instanceof TemperatureDevice) {
        if (((TemperatureDevice) device).id.equals(id)) {
          d = (TemperatureDevice) device;
          break;
        }
      }
    }
    return d;
  }

  public SensorTemperatureDevice getSensorTemperatureDeviceById(String id) {
    SensorTemperatureDevice d = null;
    for (Object device : deviceList) {
      if (device instanceof SensorTemperatureDevice) {
        if (((SensorTemperatureDevice) device).id.equals(id)) {
          d = (SensorTemperatureDevice) device;
          break;
        }
      }
    }
    return d;
  }

  public DimmerDevice getDimmerById(String id) {
    DimmerDevice d = null;
    for (Object device : deviceList) {
      if (device instanceof DimmerDevice) {
        if (((DimmerDevice) device).id.equals(id)) {
          d = (DimmerDevice) device;
          break;
        }
      }
    }
    return d;
  }

  public SceneDevice getSceneDeviceBySpeechIdent(String ident) {
    SceneDevice d = null;
    for (Object device : deviceList) {
      if (device instanceof SceneDevice) {
        if (((SceneDevice) device).speechIdent.equals(ident)) {
          d = (SceneDevice) device;
          break;
        }
      }
    }
    return d;
  }

  public SceneDevice getSceneDeviceByHandle(int deviceHandle) {
    SceneDevice d = null;
    for (Object device : deviceList) {
      if (device instanceof SceneDevice) {
        if (((SceneDevice) device).deviceHandle == deviceHandle) {
          d = (SceneDevice) device;
          break;
        }
      }
    }
    return d;
  }

  public int getMaxInputBusAddress() {
    if (maxInputBusaddress == -1) {
      for (Object device : deviceList) {
        if (device instanceof InputDevice) {
          if (((InputDevice) device).busAddress > maxInputBusaddress) {
            maxInputBusaddress = ((InputDevice) device).busAddress;
          }
        }
      }
    }
    return maxInputBusaddress;
  }

  public int getMaxOutputBusAddress() {
    if (maxOutputBusaddress == -1) {
      for (Object device : deviceList) {
        if (device instanceof OutputDevice) {
          if (((OutputDevice) device).busAddress > maxOutputBusaddress) {
            maxOutputBusaddress = ((OutputDevice) device).busAddress;
          }
        }
      }
    }
    return maxOutputBusaddress;
  }

  public int getMaxAnalogInBusAddress() {
    if (maxAnalogInBusaddress == -1) {
      for (Object device : deviceList) {
        if (device instanceof TemperatureDevice) {
          if (((TemperatureDevice) device).busAddress > maxAnalogInBusaddress) {
            maxAnalogInBusaddress = ((TemperatureDevice) device).busAddress;
          }
        } else if (device instanceof SensorTemperatureDevice) {
          if (((SensorTemperatureDevice) device).busAddress > maxAnalogInBusaddress) {
            maxAnalogInBusaddress = ((SensorTemperatureDevice) device).busAddress;
          }
        } else if (device instanceof AnalogInDevice) {
          if (((AnalogInDevice) device).busAddress > maxAnalogInBusaddress) {
            maxAnalogInBusaddress = ((AnalogInDevice) device).busAddress;
          }
        }
      }
    }
    return maxAnalogInBusaddress;
  }

  public boolean anyTemperatureDeviceIsActive() {
    boolean state = false;
    for (Object device : deviceList) {
      if (device instanceof TemperatureDevice) {
        if (((TemperatureDevice) device).isOutputState()) {
          state = true;
          break;
        }
      }
    }
    return state;
  }

}
