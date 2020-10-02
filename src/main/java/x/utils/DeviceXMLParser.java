package x.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import x.DeviceEffects.BaseEffect;
import x.DeviceEffects.BridgeEffect;
import x.DeviceEffects.DimmerEffect;
import x.DeviceEffects.OffEffect;
import x.DeviceEffects.OnEffect;
import x.DeviceEffects.ToggleEffect;
import x.DeviceUtils.InputParameter;
import x.Devices.OutputDevice;
import x.Devices.BaseDevice;
import x.DeviceUtils.Types;
import x.Devices.AnalogInDevice;
import x.Devices.DHT22Device;
import x.Devices.DimmerDevice;
import x.Devices.HeatingPumpDevice;
import x.Devices.InputDevice;
import x.Devices.SceneDevice;
import x.Devices.SensorTemperatureDevice;
import x.Devices.ShadingDevice;
import x.Devices.TemperatureDevice;
import x.Devices.TimeDevice;
import x.Devices.VentilationDevice;

public class DeviceXMLParser {

  private final ArrayList<String> outputsId = new ArrayList<>();
  private final ArrayList<String> dimmerId = new ArrayList<>();
  private final ArrayList<String> inputId = new ArrayList<>();
  private final ArrayList<String> temperatureId = new ArrayList<>();
  private final ArrayList<String> temperatureSensorId = new ArrayList<>();
  private final ArrayList<Integer> outputsBus = new ArrayList<>();
  private final ArrayList<Integer> dimmerBus = new ArrayList<>();
  private final ArrayList<Integer> inputsBus = new ArrayList<>();
  private final ArrayList<Integer> analogInBus = new ArrayList<>();
  private final ArrayList<String> sceneId = new ArrayList<>();
  private final ArrayList<String> sceneSpeechIdent = new ArrayList<>();
  private final ArrayList<String> timeId = new ArrayList<>();
  private final ArrayList<String> dht22Id = new ArrayList<>();
  private final ArrayList<String> heatingPumpId = new ArrayList<>();
  private final ArrayList<String> ventilationId = new ArrayList<>();
  private final ArrayList<String> shadingId = new ArrayList<>();
  private final ArrayList<String> analogInId = new ArrayList<>();

  private void checkForValidOutput(String output) throws Exception {
    String[] outputs = output.split(";");
    for (String s : outputs) {
      if (!outputsId.contains(s)) {
        throw new Exception("Output-ID :" + s + " not valid/found");
      }
    }
  }

  private void checkForValidTemperatures(String temperatureIds) throws Exception {
    String[] temperatureIdsList = temperatureIds.split(";");
    for (String s : temperatureIdsList) {
      if (!temperatureId.contains(s)) {
        throw new Exception("Temperature-ID :" + s + " not valid/found");
      }
    }
  }

  private void checkForValidSensorTemperatures(String temperatureIds) throws Exception {
    String[] temperatureIdsList = temperatureIds.split(";");
    for (String s : temperatureIdsList) {
      if (!temperatureSensorId.contains(s)) {
        throw new Exception("SensorTemperature-ID :" + s + " not valid/found");
      }
    }
  }

  private void checkForValidDimmer(String dimmer) throws Exception {
    String[] outputs = dimmer.split(";");
    for (String s : outputs) {
      if (!dimmerId.contains(s)) {
        throw new Exception("Dimmer-ID :" + s + " not valid/found");
      }
    }
  }

  private int getType(String type) throws Exception {
    int returnType = 0;
    if (type.equalsIgnoreCase("TOGGLE")) {
      returnType = Types.TYPE_TOGGLE;
    } else if (type.equalsIgnoreCase("ON")) {
      returnType = Types.TYPE_ON;
    } else if (type.equalsIgnoreCase("OFF")) {
      returnType = Types.TYPE_OFF;
    } else if (type.equalsIgnoreCase("BRIDGE")) {
      returnType = Types.TYPE_BRIDGE;
    } else if (type.equalsIgnoreCase("DIMMER")) {
      returnType = Types.TYPE_DIMMER;
    }
    if (returnType == 0) {
      throw new Exception("Invalid Type :" + type);
    }
    return returnType;
  }

  private String checkAndGetAttribute(Element e, String name, boolean blankCheck) throws Exception {
    String value = null;
    if (e.hasAttribute(name)) {
      String attr = e.getAttribute(name);
      if (blankCheck) {
        if (!attr.isEmpty()) {
          value = attr;
        }
      } else {
        value = attr;
      }
    }
    if (value == null) {
      throw new Exception("error in checkAttribute == null for " + e.getNodeName() + " and " + name);
    }
    return value;
  }

  private String getCategoryName(Element e) throws Exception {
    String name = null;
    Element p = (Element) e.getParentNode();
    if (p.getNodeName().equals("category")) {
      name = checkAndGetAttribute(p, "name", true);
    } else {
      throw new Exception("Wrong parent Node. Should be Category");
    }
    return name;
  }

  void parseOutputDevice(List<BaseDevice> devices, Element e, String nodeType) throws Exception {
    String categorieName = getCategoryName(e);
    String id = checkAndGetAttribute(e, "id", true);
    String busAddress = checkAndGetAttribute(e, "busAddress", true);
    String displayName = checkAndGetAttribute(e, "displayName", true);
    String concurrentOutputs = checkAndGetAttribute(e, "concurrentOutputs", false);
    String dashboard = checkAndGetAttribute(e, "dashboard", false);
    String timeStart = checkAndGetAttribute(e, "timeStart", false);
    String timeStop = checkAndGetAttribute(e, "timeStop", false);
    OutputDevice d = new OutputDevice(BaseDevice.DEFAULTCYCLETIME * 20);
    d.id = id;
    d.busAddress = Integer.parseInt(busAddress);
    if (!outputsId.contains(d.id) && !"".equals(d.id)) {
      outputsId.add(d.id);
    } else {
      throw new Exception("Double ID/Invalid:" + d.id + " for OutputDevice");
    }
    if (!outputsBus.contains(d.busAddress)) {
      outputsBus.add(d.busAddress);
    } else {
      throw new Exception("Double OutputBusAddress:" + d.busAddress);
    }
    d.deviceHandle = (d.busAddress + Types.TYPE_OUTPUT) * -1;
    d.categorie = categorieName;
    d.displayName = displayName;
    if (!concurrentOutputs.equals("")) {
      d.addConcurrentOutputs(concurrentOutputs);
    }
    if (dashboard.equals("true")) {
      d.dashboard = true;
    }
    if (!timeStart.equals("") && !timeStop.equals("")) {
      d.setTimeSettings(timeStart, timeStop);
    }
    devices.add(d);
  }

  void parseDimmerDevice(List<BaseDevice> devices, Element e, String nodeType) throws Exception {
    String categorieName = getCategoryName(e);
    String id = checkAndGetAttribute(e, "id", true);
    String busAddress = checkAndGetAttribute(e, "busAddress", true);
    String displayName = checkAndGetAttribute(e, "displayName", true);
    String dashboard = checkAndGetAttribute(e, "dashboard", false);
    String startDelay = checkAndGetAttribute(e, "startDelay", false);
    String style = checkAndGetAttribute(e, "style", false);
    DimmerDevice d = new DimmerDevice(BaseDevice.DEFAULTCYCLETIME);
    d.id = id;
    d.busAddress = Integer.parseInt(busAddress);
    if (!dimmerId.contains(d.id) && !"".equals(d.id)) {
      dimmerId.add(d.id);
    } else {
      throw new Exception("Double ID/Invalid:" + d.id + " for DimmerDevice");
    }
    if (!dimmerBus.contains(d.busAddress)) {
      dimmerBus.add(d.busAddress);
    } else {
      throw new Exception("Double DimmerBusAddress:" + d.busAddress);
    }
    d.deviceHandle = (d.busAddress + Types.TYPE_DIMMERDEVICE) * -1;
    d.categorie = categorieName;
    d.displayName = displayName;
    if (dashboard.equals("true")) {
      d.dashboard = true;
    }
    d.startDelay = (startDelay.equals("") ? 1250 : Integer.parseInt(startDelay));
    d.style = (style.equals("") ? "dimmer" : style);
    devices.add(d);
  }

  void parseInputDevice(List<BaseDevice> devices, Element e, String nodeType) throws Exception {
    String categorieName = getCategoryName(e);
    String id = checkAndGetAttribute(e, "id", true);
    String busAddress = checkAndGetAttribute(e, "busAddress", true);
    String displayName = checkAndGetAttribute(e, "displayName", true);
    String style = checkAndGetAttribute(e, "style", false);
    InputDevice d = new InputDevice(BaseDevice.DEFAULTCYCLETIME);
    d.id = id;
    d.busAddress = Integer.parseInt(busAddress);
    if (!inputId.contains(d.id) && !"".equals(d.id)) {
      inputId.add(d.id);
    } else {
      throw new Exception("Double ID/Invalid:" + d.id + " for InputDevice");
    }
    if (!inputsBus.contains(d.busAddress)) {
      inputsBus.add(d.busAddress);
    } else {
      throw new Exception("Double InputBusAddress:" + d.busAddress);
    }
    d.deviceHandle = (d.busAddress + Types.TYPE_INPUT) * -1;
    d.categorie = categorieName;
    d.displayName = displayName;
    d.style = (style.equals("") ? "light" : style);
    NodeList params = e.getChildNodes();
    for (int i = 0; i < params.getLength(); i++) {
      if (params.item(i).getNodeType() == Node.ELEMENT_NODE) {
        Element el = (Element) params.item(i);
        String forwardHandle = checkAndGetAttribute(el, "forwardHandle", true);
        String typeString = checkAndGetAttribute(el, "type", true);
        String delay = checkAndGetAttribute(el, "delay", true);
        String duration = checkAndGetAttribute(el, "duration", true);
        int type = getType(typeString);
        if (type == Types.TYPE_DIMMER) {
          checkForValidDimmer(forwardHandle);
        } else {
          checkForValidOutput(forwardHandle);
        }
        BaseEffect effect = null;
        if (type != 0) {
          if (type == Types.TYPE_ON) {
            effect = new OnEffect(Integer.parseInt(duration), Integer.parseInt(delay), forwardHandle);
          } else if (type == Types.TYPE_OFF) {
            effect = new OffEffect(Integer.parseInt(delay), forwardHandle);
          } else if (type == Types.TYPE_TOGGLE) {
            effect = new ToggleEffect(Integer.parseInt(delay), forwardHandle);
          } else if (type == Types.TYPE_BRIDGE) {
            effect = new BridgeEffect(Integer.parseInt(delay), forwardHandle);
          } else if (type == Types.TYPE_DIMMER) {
            effect = new DimmerEffect(Integer.parseInt(delay), forwardHandle);
          }
          InputParameter deviceParam = new InputParameter(type, effect);
          d.inputParams.add(deviceParam);
        }
      }
    }
    devices.add(d);
  }

  void parseTemperatureDevice(List<BaseDevice> devices, Element e, String nodeType) throws Exception {
    String categorieName = getCategoryName(e);
    String id = checkAndGetAttribute(e, "id", true);
    String busAddress = checkAndGetAttribute(e, "busAddress", true);
    String displayName = checkAndGetAttribute(e, "displayName", true);
    String sollvalue = checkAndGetAttribute(e, "sollvalue", true);
    String uHysterese = checkAndGetAttribute(e, "uHysterese", true);
    String oHysterese = checkAndGetAttribute(e, "oHysterese", true);
    String forwardHandle = checkAndGetAttribute(e, "forwardHandle", true);
    String active = checkAndGetAttribute(e, "active", false);
    String style = checkAndGetAttribute(e, "style", false);
    String concurrentTemperatures = checkAndGetAttribute(e, "concurrentTemperatures", false);
    String dashboard = checkAndGetAttribute(e, "dashboard", false);
    TemperatureDevice d = new TemperatureDevice(BaseDevice.DEFAULTCYCLETIME * 50);
    d.id = id;
    d.busAddress = Integer.parseInt(busAddress);
    if (!temperatureId.contains(d.id) && !"".equals(d.id)) {
      temperatureId.add(d.id);
    } else {
      throw new Exception("Double ID/Invalid:" + d.id + " for TemperatureDevice");
    }
    if (!analogInBus.contains(d.busAddress)) {
      analogInBus.add(d.busAddress);
    } else {
      throw new Exception("Double AnalogInBusAddress:" + d.busAddress);
    }
    checkForValidOutput(forwardHandle);
    d.deviceHandle = (d.busAddress + Types.TYPE_TEMPERATURE) * -1;
    d.categorie = categorieName;
    d.displayName = displayName;
    d.sollvalue = Integer.parseInt(sollvalue);
    d.uHysterese = Integer.parseInt(uHysterese);
    d.oHysterese = Integer.parseInt(oHysterese);
    if (active.equals("true")) {
      d.activ = true;
    }
    d.style = (style.equals("") ? "temperature" : style);
    d.addOutputs(forwardHandle);
    if (!concurrentTemperatures.equals("")) {
      d.addConcurrentTemperatureIds(concurrentTemperatures);
    }
    if (dashboard.equals("true")) {
      d.dashboard = true;
    }
    devices.add(d);
  }

  void parseSensorTemperatureDevice(List<BaseDevice> devices, Element e, String nodeType) throws Exception {
    String categorieName = getCategoryName(e);
    String id = checkAndGetAttribute(e, "id", true);
    String busAddress = checkAndGetAttribute(e, "busAddress", true);
    String displayName = checkAndGetAttribute(e, "displayName", true);
    String sollvalue = checkAndGetAttribute(e, "sollvalue", true);
    String concurrentTemperatures = checkAndGetAttribute(e, "concurrentTemperatures", false);
    String active = checkAndGetAttribute(e, "active", false);
    String style = checkAndGetAttribute(e, "style", false);
    String dashboard = checkAndGetAttribute(e, "dashboard", false);
    SensorTemperatureDevice d = new SensorTemperatureDevice(BaseDevice.DEFAULTCYCLETIME * 50);
    d.id = id;
    d.busAddress = Integer.parseInt(busAddress);
    if (!temperatureSensorId.contains(d.id) && d.id != "") {
      temperatureSensorId.add(d.id);
    } else {
      throw new Exception("Double ID/Invalid:" + d.id + " for SensorTemperatureDevice");
    }
    if (!analogInBus.contains(d.busAddress)) {
      analogInBus.add(d.busAddress);
    } else {
      throw new Exception("Double AnalogInBusAddress:" + d.busAddress);
    }
    if (!"".equals(concurrentTemperatures)) {
      checkForValidTemperatures(concurrentTemperatures);
    }
    d.deviceHandle = (d.busAddress + Types.TYPE_SENSOR) * -1;
    d.categorie = categorieName;
    d.displayName = displayName;
    d.sollvalue = Integer.parseInt(sollvalue);
    if (active.equals("true")) {
      d.activ = true;
    }
    d.style = (style.equals("") ? "sensor" : style);
    if (!"".equals(concurrentTemperatures)) {
      d.addConcurrentTemperatureIds(concurrentTemperatures);
    }
    if (dashboard.equals("true")) {
      d.dashboard = true;
    }
    devices.add(d);
  }

  private int sceneCounter = 0;

  void parseSceneDevice(List<BaseDevice> devices, Element e, String nodeType) throws Exception {
    String categorieName = getCategoryName(e);
    String id = checkAndGetAttribute(e, "id", true);
    String speechIdent = checkAndGetAttribute(e, "speechIdent", true);
    String displayName = checkAndGetAttribute(e, "displayName", true);
    String style = checkAndGetAttribute(e, "style", false);
    String dashboard = checkAndGetAttribute(e, "dashboard", false);
    SceneDevice d = new SceneDevice(BaseDevice.DEFAULTCYCLETIME);
    d.id = id;
    d.speechIdent = speechIdent;
    if (!sceneId.contains(d.id) && !"".equals(d.id)) {
      sceneId.add(d.id);
    } else {
      throw new Exception("Double ID/Invalid:" + d.id + " for SceneDevice");
    }
    if (!sceneSpeechIdent.contains(d.speechIdent)) {
      sceneSpeechIdent.add(d.speechIdent);
    } else {
      throw new Exception("Double SceneSpeechIdent:" + d.speechIdent);
    }
    d.deviceHandle = (sceneCounter + Types.TYPE_SCENE) * -1;
    sceneCounter++;
    d.categorie = categorieName;
    d.displayName = displayName;
    d.style = (style.equals("") ? "scene" : style);
    if (dashboard.equals("true")) {
      d.dashboard = true;
    }
    NodeList params = e.getChildNodes();
    for (int i = 0; i < params.getLength(); i++) {
      if (params.item(i).getNodeType() == Node.ELEMENT_NODE) {
        Element el = (Element) params.item(i);
        String forwardHandle = checkAndGetAttribute(el, "forwardHandle", true);
        String typeString = checkAndGetAttribute(el, "type", true);
        String delay = checkAndGetAttribute(el, "delay", true);
        int type = getType(typeString);
        if (type == Types.TYPE_DIMMER) {
          checkForValidDimmer(forwardHandle);
        } else {
          checkForValidOutput(forwardHandle);
        }
        String duration = checkAndGetAttribute(el, "duration", true);
        BaseEffect effect = null;
        if (type != 0) {
          if (type == Types.TYPE_ON) {
            effect = new OnEffect(Integer.parseInt(duration), Integer.parseInt(delay), forwardHandle);
          } else if (type == Types.TYPE_OFF) {
            effect = new OffEffect(Integer.parseInt(delay), forwardHandle);
          } else if (type == Types.TYPE_TOGGLE) {
            effect = new ToggleEffect(Integer.parseInt(delay), forwardHandle);
          } else if (type == Types.TYPE_BRIDGE) {
            effect = new BridgeEffect(Integer.parseInt(delay), forwardHandle);
          } else if (type == Types.TYPE_DIMMER) {
            effect = new DimmerEffect(Integer.parseInt(delay), forwardHandle);
          }
          InputParameter deviceParam = new InputParameter(type, effect);
          d.inputParams.add(deviceParam);
        }
      }
    }
    devices.add(d);
  }

  private int timeCounter = 0;

  void parseTimeDevice(List<BaseDevice> devices, Element e, String nodeType) throws Exception {
    String categorieName = getCategoryName(e);
    String id = checkAndGetAttribute(e, "id", true);
    String timeStart = checkAndGetAttribute(e, "timeStart", true);
    String timeStop = checkAndGetAttribute(e, "timeStop", true);
    String forwardHandle = checkAndGetAttribute(e, "forwardHandle", true);
    String displayName = checkAndGetAttribute(e, "displayName", true);
    String style = checkAndGetAttribute(e, "style", false);
    String dashboard = checkAndGetAttribute(e, "dashboard", false);
    TimeDevice d = new TimeDevice(BaseDevice.DEFAULTCYCLETIME * 20);
    d.id = id;
    if (!timeId.contains(d.id) && !"".equals(d.id)) {
      timeId.add(d.id);
    } else {
      throw new Exception("Double ID/Invalid:" + d.id + " for TimeDevice");
    }
    d.deviceHandle = (timeCounter + Types.TYPE_TIME) * -1;
    timeCounter++;
    d.categorie = categorieName;
    d.displayName = displayName;
    // Format e.g. 23:00:00 => HH:mm:ss
    d.timeStart = timeStart;
    d.timeStop = timeStop;
    d.style = (style.equals("") ? "time" : style);
    checkForValidOutput(forwardHandle);
    d.addOutputs(forwardHandle);
    if (dashboard.equals("true")) {
      d.dashboard = true;
    }
    devices.add(d);
  }

  private int dht22Counter = 0;

  void parseDhtDevice(List<BaseDevice> devices, Element e, String nodeType) throws Exception {
    String categorieName = getCategoryName(e);
    String id = checkAndGetAttribute(e, "id", true);
    String command = checkAndGetAttribute(e, "command", true);
    String params = checkAndGetAttribute(e, "params", true);
    String writable = checkAndGetAttribute(e, "writable", true);
    String displayName = checkAndGetAttribute(e, "displayName", true);
    String style = checkAndGetAttribute(e, "style", false);
    String dashboard = checkAndGetAttribute(e, "dashboard", false);
    DHT22Device d = new DHT22Device(BaseDevice.DEFAULTCYCLETIME * 6000);
    d.id = id;
    if (!dht22Id.contains(d.id) && !"".equals(d.id)) {
      dht22Id.add(d.id);
    } else {
      throw new Exception("Double ID/Invalid:" + d.id + " for DhTDevice");
    }
    d.deviceHandle = (dht22Counter + Types.TYPE_DHT22) * -1;
    dht22Counter++;
    d.categorie = categorieName;
    d.displayName = displayName;
    d.addExec(command, params, (writable.equals("true")));
    d.style = (style.equals("") ? "dht22" : style);
    if (dashboard.equals("true")) {
      d.dashboard = true;
    }
    devices.add(d);
  }

  private int heatingPumpCounter = 0;

  void parseHeatingDevice(List<BaseDevice> devices, Element e, String nodeType) throws Exception {
    String categorieName = getCategoryName(e);
    String id = checkAndGetAttribute(e, "id", true);
    String timeStart = checkAndGetAttribute(e, "timeStart", true);
    String timeStop = checkAndGetAttribute(e, "timeStop", true);
    String forwardHandle = checkAndGetAttribute(e, "forwardHandle", true);
    String displayName = checkAndGetAttribute(e, "displayName", true);
    String style = checkAndGetAttribute(e, "style", false);
    String dashboard = checkAndGetAttribute(e, "dashboard", false);
    String concurrentTemperatures = checkAndGetAttribute(e, "concurrentTemperatures", false);
    String active = checkAndGetAttribute(e, "active", false);
    HeatingPumpDevice d = new HeatingPumpDevice(BaseDevice.DEFAULTCYCLETIME * 900);
    d.id = id;
    if (!heatingPumpId.contains(d.id) && !"".equals(d.id)) {
      heatingPumpId.add(d.id);
    } else {
      throw new Exception("Double ID/Invalid:" + d.id + " for HeatingPumpDevice");
    }
    checkForValidSensorTemperatures(concurrentTemperatures);
    d.deviceHandle = (heatingPumpCounter + Types.TYPE_HEATINGPUMP) * -1;
    heatingPumpCounter++;
    d.categorie = categorieName;
    d.displayName = displayName;
    d.timeStart = timeStart;
    d.timeStop = timeStop;
    checkForValidOutput(forwardHandle);
    d.addOutputs(forwardHandle);
    if (!"".equals(concurrentTemperatures)) {
      d.addConcurrentTemperatureIds(concurrentTemperatures);
    }
    if (active.equals("true")) {
      d.activ = true;
    }
    d.style = (style.equals("") ? "heating" : style);
    if (dashboard.equals("true")) {
      d.dashboard = true;
    }
    devices.add(d);
  }

  private int ventilationCounter = 0;

  void parseVentilationDevice(List<BaseDevice> devices, Element e, String nodeType) throws Exception {
    String categorieName = getCategoryName(e);
    String id = checkAndGetAttribute(e, "id", true);
    String forwardHandle = checkAndGetAttribute(e, "forwardHandle", true);
    String displayName = checkAndGetAttribute(e, "displayName", true);
    String style = checkAndGetAttribute(e, "style", false);
    String dashboard = checkAndGetAttribute(e, "dashboard", false);
    String sollvalue = checkAndGetAttribute(e, "sollvalue", false);
    String timeStart = checkAndGetAttribute(e, "timeStart", false);
    String timeStop = checkAndGetAttribute(e, "timeStop", false);
    String concurrentTemperatures = checkAndGetAttribute(e, "concurrentTemperatures", false);
    String active = checkAndGetAttribute(e, "active", false);
    VentilationDevice d = new VentilationDevice(BaseDevice.DEFAULTCYCLETIME * 1200);
    d.id = id;
    if (!ventilationId.contains(d.id) && !"".equals(d.id)) {
      ventilationId.add(d.id);
    } else {
      throw new Exception("Double ID/Invalid:" + d.id + " for VentilationDevice");
    }
    d.deviceHandle = (ventilationCounter + Types.TYPE_VENTILATION) * -1;
    ventilationCounter++;
    d.categorie = categorieName;
    d.displayName = displayName;
    d.timeStart = timeStart;
    d.timeStop = timeStop;
    d.sollvalue = Integer.parseInt(sollvalue);
    checkForValidOutput(forwardHandle);
    d.addOutputs(forwardHandle);
    if (!"".equals(concurrentTemperatures)) {
      d.addConcurrentTemperatureIds(concurrentTemperatures);
    }
    if (active.equals("true")) {
      d.activ = true;
    }
    d.style = (style.equals("") ? "dht22" : style);
    if (dashboard.equals("true")) {
      d.dashboard = true;
    }
    devices.add(d);
  }

  private int shadingCounter = 0;

  void parseShadingDevice(List<BaseDevice> devices, Element e, String nodeType) throws Exception {
    String categorieName = getCategoryName(e);
    String id = checkAndGetAttribute(e, "id", true);
    String forwardHandle = checkAndGetAttribute(e, "forwardHandle", true);
    String displayName = checkAndGetAttribute(e, "displayName", true);
    String style = checkAndGetAttribute(e, "style", false);
    String dashboard = checkAndGetAttribute(e, "dashboard", false);
    String sollvalue = checkAndGetAttribute(e, "sollvalue", false);
    String timeStart = checkAndGetAttribute(e, "timeStart", false);
    String timeStop = checkAndGetAttribute(e, "timeStop", false);
    String durationactive = checkAndGetAttribute(e, "durationactive", false);
    String durationpostActive = checkAndGetAttribute(e, "durationpostactive", false);
    String durationreactive = checkAndGetAttribute(e, "durationreactive", false);
    String concurrentTemperatures = checkAndGetAttribute(e, "concurrentTemperatures", false);
    String active = checkAndGetAttribute(e, "active", false);
    ShadingDevice d = new ShadingDevice(BaseDevice.DEFAULTCYCLETIME * 18000);
    d.id = id;
    if (!shadingId.contains(d.id) && !"".equals(d.id)) {
      shadingId.add(d.id);
    } else {
      throw new Exception("Double ID/Invalid:" + d.id + " for ShadingDevice");
    }
    checkForValidSensorTemperatures(concurrentTemperatures);
    d.deviceHandle = (shadingCounter + Types.TYPE_SHADING) * -1;
    shadingCounter++;
    d.categorie = categorieName;
    d.displayName = displayName;
    d.timeStart = timeStart;
    d.timeStop = timeStop;
    d.sollvalueTemp = Integer.parseInt(sollvalue);
    d.durationActive = Integer.parseInt(durationactive);
    d.durationPostActive = Integer.parseInt(durationpostActive);
    d.durationReactive = Integer.parseInt(durationreactive);
    checkForValidOutput(forwardHandle);
    d.addOutputs(forwardHandle);
    if (!"".equals(concurrentTemperatures)) {
      d.addConcurrentTemperatureIds(concurrentTemperatures);
    }
    if (active.equals("true")) {
      d.activ = true;
    }
    d.style = (style.equals("") ? "shading" : style);
    if (dashboard.equals("true")) {
      d.dashboard = true;
    }
    devices.add(d);
  }

  void parseAnalogInDevice(List<BaseDevice> devices, Element e, String nodeType) throws Exception {
    String categorieName = getCategoryName(e);
    String id = checkAndGetAttribute(e, "id", true);
    String busAddress = checkAndGetAttribute(e, "busAddress", true);
    String displayName = checkAndGetAttribute(e, "displayName", true);
    String style = checkAndGetAttribute(e, "style", false);
    String math = checkAndGetAttribute(e, "math", false);
    String label = checkAndGetAttribute(e, "label", true);
    AnalogInDevice d = new AnalogInDevice(BaseDevice.DEFAULTCYCLETIME * 1200);
    d.id = id;
    d.busAddress = Integer.parseInt(busAddress);
    if (!analogInId.contains(d.id) && !"".equals(d.id)) {
      analogInId.add(d.id);
    } else {
      throw new Exception("Double ID/Invalid:" + d.id + " for ShadingDevice");
    }
    if (!analogInBus.contains(d.busAddress)) {
      analogInBus.add(d.busAddress);
    } else {
      throw new Exception("Double AnalogInBusAddress:" + d.busAddress);
    }
    d.deviceHandle = (d.busAddress + Types.TYPE_ANALOGIN) * -1;
    d.categorie = categorieName;
    d.displayName = displayName;
    d.style = (style.equals("") ? "analogin" : style);
    d.setMath(math);
    d.label = label;
    devices.add(d);
  }

  private void parseNode(List<BaseDevice> devices, Element e, String nodeType) throws Exception {
    String nodeName = e.getNodeName();
    NodeList nl = e.getChildNodes();
    if (nodeName.equalsIgnoreCase(nodeType) && nodeName.equalsIgnoreCase("output")) {
      parseOutputDevice(devices, e, nodeType);
    } else if (nodeName.equalsIgnoreCase(nodeType) && nodeName.equalsIgnoreCase("dimmer")) {
      parseDimmerDevice(devices, e, nodeType);
    } else if (nodeName.equalsIgnoreCase(nodeType) && nodeName.equalsIgnoreCase("input")) {
      parseInputDevice(devices, e, nodeType);
    } else if (nodeName.equalsIgnoreCase(nodeType) && nodeName.equalsIgnoreCase("temperature")) {
      parseTemperatureDevice(devices, e, nodeType);
    } else if (nodeName.equalsIgnoreCase(nodeType) && nodeName.equalsIgnoreCase("sensorTemperature")) {
      parseSensorTemperatureDevice(devices, e, nodeType);
    } else if (nodeName.equalsIgnoreCase(nodeType) && nodeName.equalsIgnoreCase("scene")) {
      parseSceneDevice(devices, e, nodeType);
    } else if (nodeName.equalsIgnoreCase(nodeType) && nodeName.equalsIgnoreCase("time")) {
      parseTimeDevice(devices, e, nodeType);
    } else if (nodeName.equalsIgnoreCase(nodeType) && nodeName.equalsIgnoreCase("dht22")) {
      parseDhtDevice(devices, e, nodeType);
    } else if (nodeName.equalsIgnoreCase(nodeType) && nodeName.equalsIgnoreCase("heatingpump")) {
      parseHeatingDevice(devices, e, nodeType);
    } else if (nodeName.equalsIgnoreCase(nodeType) && nodeName.equalsIgnoreCase("ventilation")) {
      parseVentilationDevice(devices, e, nodeType);
    } else if (nodeName.equalsIgnoreCase(nodeType) && nodeName.equalsIgnoreCase("shading")) {
      parseShadingDevice(devices, e, nodeType);
    } else if (nodeName.equalsIgnoreCase(nodeType) && nodeName.equalsIgnoreCase("analogin")) {
      parseAnalogInDevice(devices, e, nodeType);
    }
    for (int i = 0; i < nl.getLength(); i++) {
      if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
        Element el = (Element) nl.item(i);
        parseNode(devices, el, nodeType);
      }
    }
  }

  public List<BaseDevice> initConfigFile(String filename) throws Exception {

    List<BaseDevice> devices = new ArrayList<>();
    File file = new File(filename);
    if (file.exists()) {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new File(filename));

      parseNode(devices, document.getDocumentElement(), "output");
      parseNode(devices, document.getDocumentElement(), "dimmer");
      parseNode(devices, document.getDocumentElement(), "input");
      parseNode(devices, document.getDocumentElement(), "temperature");
      parseNode(devices, document.getDocumentElement(), "sensorTemperature");
      parseNode(devices, document.getDocumentElement(), "scene");
      parseNode(devices, document.getDocumentElement(), "time");
      parseNode(devices, document.getDocumentElement(), "dht22");
      parseNode(devices, document.getDocumentElement(), "heatingpump");
      parseNode(devices, document.getDocumentElement(), "ventilation");
      parseNode(devices, document.getDocumentElement(), "shading");
      parseNode(devices, document.getDocumentElement(), "analogin");

    }
    return devices;
  }

  public static void main(String[] args) {
    DeviceXMLParser xmpP = new DeviceXMLParser();
    try {
      List<BaseDevice> bd = xmpP.initConfigFile("/home/deployment/Documents/GitHub/XHomeautomation-Service/src/main/resources/data/homeautomation.xml");
      System.out.println("Ready");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
