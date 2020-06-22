package x.utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import x.DeviceUtils.DeviceListUtils;
import x.DeviceUtils.RecorderItem;
import x.DeviceUtils.Types;
import x.Devices.InputDevice;
import x.Devices.OutputDevice;
import x.Devices.SceneDevice;
import x.Devices.TemperatureDevice;
import x.Devices.BaseDevice;
import x.Devices.DHT22Device;
import x.Devices.HeatingPumpDevice;
import x.Devices.DimmerDevice;
import x.Devices.SensorTemperatureDevice;
import x.Devices.ShadingDevice;
import x.Devices.TimeDevice;
import x.Devices.VentilationDevice;
import x.rest.data.devices.Record;
import x.rest.data.devices.RestBaseDevice;
import x.rest.data.devices.RestParam;
import x.websocket.model.AsyncStatusMessage;

public class JsonBuilder {

  private final ArrayList<SortableList> globalResultList;

  public JsonBuilder() {
    globalResultList = sortList();
  }

  public DeviceHandleList fromDeviceHandleList() {

    DeviceHandleList dhl = new DeviceHandleList();

    for (SortableList sort : globalResultList) {
      RestBaseDeviceRecorder rbd = new RestBaseDeviceRecorder();
      rbd.setNameCategory(sort.categorie);
      rbd.setHandleDevice("" + sort.handleDevice);
      rbd.setStyleDevice(sort.styleDevice);
      rbd.setTypeDevice("" + sort.typeDevice);
      rbd.setDisplayNameDevice(sort.displayName);
      if (sort.params != null) {
        for (PropertyInfo param : sort.params) {
          RestParam rp = new RestParam();
          rp.setHandle("" + param.handle());
          rp.setEditable("" + param.editable());
          rp.setVisibleValue("" + param.visibleValue());
          rp.setType("" + param.type());
          rp.setValue("");
          rp.setDisplayName(param.displayName());
          rbd.getParams().add(rp);
        }
      }
      if (sort.ri != null) {
        ArrayList<RecorderItem.ResultItem> riItems = sort.ri.getAllItems();
        if (riItems.size() > 0) {
          for (RecorderItem.ResultItem ri : riItems) {
            Record r = new Record();
            r.setId(getUUID());
            r.setDimension("" + ri.getYDimension());
            r.setLegend(ri.getX());
            r.setValues(ri.getYasString());
            r.setDate(ri.getDateTime());
            rbd.getRecords().add(r);
          }
        }
      }
      dhl.getDevices().add(rbd);
    }
    return dhl;
  }

  private String getUUID() {
    AtomicLong counter = new AtomicLong(System.currentTimeMillis() * 1000);
    return Long.toString(counter.incrementAndGet(), 36);
  }

  private ArrayList<SortableList> sortList() {

    // Categorie
    ArrayList<SortableList> returnList = new ArrayList<>();
    HashMap<String, String> categorieList = new HashMap<>();
    for (int k = 0; k < DeviceListUtils.getInstance().getDeviceList().size(); k++) {
      BaseDevice temp = (BaseDevice) DeviceListUtils.getInstance().getDeviceList().get(k);
      if (!categorieList.containsKey(temp.categorie)) {
        categorieList.put(temp.categorie, "");
      }
    }
    Iterator iterator = categorieList.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry mentry = (Map.Entry) iterator.next();

      // Input
      ArrayList<InputDevice> in = DeviceListUtils.getInstance().getInputDevicesByCategorie(mentry.getKey().toString());
      for (InputDevice device : in) {
        returnList.add(new SortableList(device.categorie, device.deviceHandle, device.style, Types.TYPE_INPUT, device.displayName, null, null, device.getRecorderItem()));
      }

      // Temperature
      ArrayList<TemperatureDevice> temp = DeviceListUtils.getInstance().getTemperatureDevicesByCategorie(mentry.getKey().toString());
      for (TemperatureDevice device : temp) {
        ArrayList<PropertyInfo> params = new ArrayList<>();
        ArrayList<DashboardInfo> dashboard = new ArrayList<>();
        Field[] fields = device.getClass().getFields();
        for (Field f : fields) {
          PropertyInfo p = (PropertyInfo) f.getAnnotation(PropertyInfo.class);
          if (p != null) {
            params.add(p);
          }
          DashboardInfo d = (DashboardInfo) f.getAnnotation(DashboardInfo.class);
          if (d != null) {
            dashboard.add(d);
          }
        }
        returnList.add(new SortableList(device.categorie, device.deviceHandle, device.style, Types.TYPE_TEMPERATURE, device.displayName, params, (device.dashboard == true ? dashboard : null), device.getRecorderItem()));
      }

      // Sensor
      ArrayList<SensorTemperatureDevice> sensor = DeviceListUtils.getInstance().getSensorTemperatureDevicesByCategorie(mentry.getKey().toString());
      for (SensorTemperatureDevice device : sensor) {
        ArrayList<PropertyInfo> params = new ArrayList<>();
        ArrayList<DashboardInfo> dashboard = new ArrayList<>();
        Field[] fields = device.getClass().getFields();
        for (Field f : fields) {
          PropertyInfo p = (PropertyInfo) f.getAnnotation(PropertyInfo.class);
          if (p != null) {
            params.add(p);
          }
          DashboardInfo d = (DashboardInfo) f.getAnnotation(DashboardInfo.class);
          if (d != null) {
            dashboard.add(d);
          }
        }
        returnList.add(new SortableList(device.categorie, device.deviceHandle, device.style, Types.TYPE_SENSOR, device.displayName, params, (device.dashboard == true ? dashboard : null), device.getRecorderItem()));
      }

      // Scene
      ArrayList<SceneDevice> scene = DeviceListUtils.getInstance().getSceneDevicesByCategorie(mentry.getKey().toString());
      for (SceneDevice device : scene) {
        ArrayList<PropertyInfo> params = new ArrayList<>();
        ArrayList<DashboardInfo> dashboard = new ArrayList<>();
        Field[] fields = device.getClass().getFields();
        for (Field f : fields) {
          PropertyInfo p = (PropertyInfo) f.getAnnotation(PropertyInfo.class);
          if (p != null) {
            params.add(p);
          }
          DashboardInfo d = (DashboardInfo) f.getAnnotation(DashboardInfo.class);
          if (d != null) {
            dashboard.add(d);
          }
        }
        returnList.add(new SortableList(device.categorie, device.deviceHandle, device.style, Types.TYPE_SCENE, device.displayName, params, (device.dashboard == true ? dashboard : null), device.getRecorderItem()));
      }

      // Time
      ArrayList<TimeDevice> time = DeviceListUtils.getInstance().getTimeDevicesByCategorie(mentry.getKey().toString());
      for (TimeDevice device : time) {
        ArrayList<PropertyInfo> params = new ArrayList<>();
        ArrayList<DashboardInfo> dashboard = new ArrayList<>();
        Field[] fields = device.getClass().getFields();
        for (Field f : fields) {
          PropertyInfo p = (PropertyInfo) f.getAnnotation(PropertyInfo.class);
          if (p != null) {
            params.add(p);
          }
          DashboardInfo d = (DashboardInfo) f.getAnnotation(DashboardInfo.class);
          if (d != null) {
            dashboard.add(d);
          }
        }
        returnList.add(new SortableList(device.categorie, device.deviceHandle, device.style, Types.TYPE_TIME, device.displayName, params, (device.dashboard == true ? dashboard : null), device.getRecorderItem()));
      }

      // Shading
      ArrayList<ShadingDevice> shading = DeviceListUtils.getInstance().getShadingDevicesByCategorie(mentry.getKey().toString());
      for (ShadingDevice device : shading) {
        ArrayList<PropertyInfo> params = new ArrayList<>();
        ArrayList<DashboardInfo> dashboard = new ArrayList<>();
        Field[] fields = device.getClass().getFields();
        for (Field f : fields) {
          PropertyInfo p = (PropertyInfo) f.getAnnotation(PropertyInfo.class);
          if (p != null) {
            params.add(p);
          }
          DashboardInfo d = (DashboardInfo) f.getAnnotation(DashboardInfo.class);
          if (d != null) {
            dashboard.add(d);
          }
        }
        returnList.add(new SortableList(device.categorie, device.deviceHandle, device.style, Types.TYPE_SHADING, device.displayName, params, (device.dashboard == true ? dashboard : null), device.getRecorderItem()));
      }

      // Output
      ArrayList<OutputDevice> output = DeviceListUtils.getInstance().getOutputDevicesByCategorie(mentry.getKey().toString());
      for (OutputDevice device : output) {
        ArrayList<PropertyInfo> params = new ArrayList<>();
        ArrayList<DashboardInfo> dashboard = new ArrayList<>();
        Field[] fields = device.getClass().getFields();
        for (Field f : fields) {
          PropertyInfo p = (PropertyInfo) f.getAnnotation(PropertyInfo.class);
          if (p != null) {
            params.add(p);
          }
          DashboardInfo d = (DashboardInfo) f.getAnnotation(DashboardInfo.class);
          if (d != null) {
            dashboard.add(d);
          }
        }
        returnList.add(new SortableList(device.categorie, device.deviceHandle, device.style, Types.TYPE_OUTPUT, device.displayName, params, (device.dashboard == true ? dashboard : null), device.getRecorderItem()));
      }

      // Dimmer
      ArrayList<DimmerDevice> dimmer = DeviceListUtils.getInstance().getDimmerDevicesByCategorie(mentry.getKey().toString());
      for (DimmerDevice device : dimmer) {
        ArrayList<PropertyInfo> params = new ArrayList<>();
        ArrayList<DashboardInfo> dashboard = new ArrayList<>();
        Field[] fields = device.getClass().getFields();
        for (Field f : fields) {
          PropertyInfo p = (PropertyInfo) f.getAnnotation(PropertyInfo.class);
          if (p != null) {
            params.add(p);
          }
          DashboardInfo d = (DashboardInfo) f.getAnnotation(DashboardInfo.class);
          if (d != null) {
            dashboard.add(d);
          }
        }
        returnList.add(new SortableList(device.categorie, device.deviceHandle, device.style, Types.TYPE_DIMMERDEVICE, device.displayName, params, (device.dashboard == true ? dashboard : null), device.getRecorderItem()));
      }

      // DHT22
      ArrayList<DHT22Device> dht22 = DeviceListUtils.getInstance().getDHT22DevicesByCategorie(mentry.getKey().toString());
      for (DHT22Device device : dht22) {
        ArrayList<PropertyInfo> params = new ArrayList<>();
        ArrayList<DashboardInfo> dashboard = new ArrayList<>();
        Field[] fields = device.getClass().getFields();
        for (Field f : fields) {
          PropertyInfo p = (PropertyInfo) f.getAnnotation(PropertyInfo.class);
          if (p != null) {
            params.add(p);
          }
          DashboardInfo d = (DashboardInfo) f.getAnnotation(DashboardInfo.class);
          if (d != null) {
            dashboard.add(d);
          }
        }
        returnList.add(new SortableList(device.categorie, device.deviceHandle, device.style, Types.TYPE_DHT22, device.displayName, params, (device.dashboard == true ? dashboard : null), device.getRecorderItem()));
      }

      // Heatingpump
      ArrayList<HeatingPumpDevice> heatingPump = DeviceListUtils.getInstance().getHeatingPumpDevicesByCategorie(mentry.getKey().toString());
      for (HeatingPumpDevice device : heatingPump) {
        ArrayList<PropertyInfo> params = new ArrayList<>();
        ArrayList<DashboardInfo> dashboard = new ArrayList<>();
        Field[] fields = device.getClass().getFields();
        for (Field f : fields) {
          PropertyInfo p = (PropertyInfo) f.getAnnotation(PropertyInfo.class);
          if (p != null) {
            params.add(p);
          }
          DashboardInfo d = (DashboardInfo) f.getAnnotation(DashboardInfo.class);
          if (d != null) {
            dashboard.add(d);
          }
        }
        returnList.add(new SortableList(device.categorie, device.deviceHandle, device.style, Types.TYPE_HEATINGPUMP, device.displayName, params, (device.dashboard == true ? dashboard : null), device.getRecorderItem()));
      }

      // Ventilation
      ArrayList<VentilationDevice> ventilation = DeviceListUtils.getInstance().getVentilationDevicesByCategorie(mentry.getKey().toString());
      for (VentilationDevice device : ventilation) {
        ArrayList<PropertyInfo> params = new ArrayList<>();
        ArrayList<DashboardInfo> dashboard = new ArrayList<>();
        Field[] fields = device.getClass().getFields();
        for (Field f : fields) {
          PropertyInfo p = (PropertyInfo) f.getAnnotation(PropertyInfo.class);
          if (p != null) {
            params.add(p);
          }
          DashboardInfo d = (DashboardInfo) f.getAnnotation(DashboardInfo.class);
          if (d != null) {
            dashboard.add(d);
          }
        }
        returnList.add(new SortableList(device.categorie, device.deviceHandle, device.style, Types.TYPE_VENTILATION, device.displayName, params, (device.dashboard == true ? dashboard : null), device.getRecorderItem()));
      }

    }
    return returnList;
  }

  static class SortableList {

    String categorie = "";
    int handleDevice = 0;
    String styleDevice = "";
    int typeDevice = 0;
    String displayName = "";
    ArrayList<PropertyInfo> params = null;
    ArrayList<DashboardInfo> dashboard = null;
    RecorderItem ri = null;

    public SortableList(String categorie, int handleDevice, String styleDevice, int typeDevice, String displayName, ArrayList<PropertyInfo> params, ArrayList<DashboardInfo> dashboard, RecorderItem ri) {
      this.categorie = categorie;
      this.handleDevice = handleDevice;
      this.styleDevice = styleDevice;
      this.typeDevice = typeDevice;
      this.displayName = displayName;
      this.params = params;
      this.dashboard = dashboard;
      this.ri = ri;
    }

  }

  public static class DeviceHandleList {

    private int error = AsyncStatusMessage.NOERROR;
    private int kind = AsyncStatusMessage.INIT;
    private ArrayList<RestBaseDevice> devices = new ArrayList<>();

    public int getError() {
      return error;
    }

    public void setError(int error) {
      this.error = error;
    }

    public int getKind() {
      return kind;
    }

    public void setKind(int kind) {
      this.kind = kind;
    }

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public ArrayList<RestBaseDevice> getDevices() {
      return devices;
    }

    public void setDevices(ArrayList<RestBaseDevice> devices) {
      this.devices = devices;
    }

  }

  public static class RestBaseDeviceRecorder extends RestBaseDevice {

    private ArrayList<Record> records = new ArrayList<>();

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public ArrayList<Record> getRecords() {
      return records;
    }

    public void setRecords(ArrayList<Record> records) {
      this.records = records;
    }

  }

}
