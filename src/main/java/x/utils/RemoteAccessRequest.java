package x.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import x.DeviceUtils.DeviceListUtils;
import x.DeviceUtils.Types;
import x.Devices.BaseDevice;
import x.Devices.OutputDevice;
import x.Devices.TemperatureDevice;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import x.Devices.AnalogInDevice;
import x.Devices.DHT22Device;
import x.Devices.HeatingPumpDevice;
import x.Devices.SceneDevice;
import x.Devices.SensorTemperatureDevice;
import x.Devices.VentilationDevice;
import x.MessageHandling.MessageHandler;

public class RemoteAccessRequest {

  private final String urlString;
  private String jwt = "";

  public RemoteAccessRequest(String urlString, String jwt) {
    this.urlString = urlString;
    this.jwt = jwt;
  }

  private void getStatefulDeviceProperties(Object deviceHandleObject, int deviceHandle, HashMap<Object, Object> tmpMap) {
    ArrayList<Object> list = new ArrayList<>();
    Field[] fields = deviceHandleObject.getClass().getFields();
    for (Field f : fields) {
      PropertyInfo p = (PropertyInfo) f.getAnnotation(PropertyInfo.class);
      if (p != null) {
        if (p.stateful() || p.editable() || p.type() == Types.TYPE_PROPERTIEINFO_INFO) {
          int handle = p.handle();
          String displayName = p.displayName();
          String value = FieldParser.convertField(f, deviceHandleObject);
          HashMap<Object, Object> m = new HashMap<>();
          m.put("handle", handle);
          m.put("displayName", displayName);
          m.put("value", value);
          m.put("stateful", p.stateful());
          m.put("editable", p.editable());
          list.add(handle, m);
        }
      }
    }
    if (list.size() > 0) {
      tmpMap.put("properties", list);
    }
  }

  private AutomationRequest generateCommitMessage() {
    AutomationRequest atRequest = new AutomationRequest();
    atRequest.setPlugin("automation");
    AutomationRequestParams atRequestParams = new AutomationRequestParams();
    atRequestParams.setMethod("commit");
    for (Object deviceHandle : DeviceListUtils.getInstance().getDeviceList()) {
      HashMap<Object, Object> tmpMap = new HashMap<>();
      if (deviceHandle instanceof OutputDevice) {
        tmpMap.put("type", Types.TYPE_OUTPUT);
      } else if (deviceHandle instanceof TemperatureDevice) {
        tmpMap.put("type", Types.TYPE_TEMPERATURE);
      } else if (deviceHandle instanceof SensorTemperatureDevice) {
        tmpMap.put("type", Types.TYPE_SENSOR);
      } else if (deviceHandle instanceof DHT22Device) {
        tmpMap.put("type", Types.TYPE_DHT22);
      } else if (deviceHandle instanceof HeatingPumpDevice) {
        tmpMap.put("type", Types.TYPE_HEATINGPUMP);
      } else if (deviceHandle instanceof VentilationDevice) {
        tmpMap.put("type", Types.TYPE_VENTILATION);
      } else if (deviceHandle instanceof AnalogInDevice) {
        tmpMap.put("type", Types.TYPE_ANALOGIN);
      } else if (deviceHandle instanceof SceneDevice) {
        tmpMap.put("type", Types.TYPE_SCENE);
      }
      if (tmpMap.size() > 0) {
        tmpMap.put("categorie", ((BaseDevice) deviceHandle).categorie);
        tmpMap.put("name", ((BaseDevice) deviceHandle).displayName);
        getStatefulDeviceProperties(deviceHandle, ((BaseDevice) deviceHandle).deviceHandle, tmpMap);
        atRequestParams.getParams().put(((BaseDevice) deviceHandle).deviceHandle, tmpMap);
      }
    }
    atRequest.setData(atRequestParams);
    return atRequest;
  }

  private void changeDeviceHandle(LinkedHashMap<String, Object> changes) {
    try {
      int devicehandle = (int) changes.get("devicehandle");
      int pHandle = (int) changes.get("propertiehandle");
      String pValue = null;
      if (changes.get("value") instanceof Integer) {
        pValue = changes.get("value") + "";
      } else if (changes.get("value") instanceof String) {
        pValue = (String) changes.get("value");
      }
      if (pValue != null) {
        for (Object deviceHandle : DeviceListUtils.getInstance().getDeviceList()) {
          if (((BaseDevice) deviceHandle).deviceHandle == devicehandle) {
            if (deviceHandle instanceof OutputDevice) {
              ((BaseDevice) deviceHandle).sendMessage((pValue.equals("1") ? true : false));
            } else {
              MessageHandler.getInstance().messageToDevice("p" + devicehandle + "_" + pHandle + "#" + pValue);
            }
            break;
          }
        }
      }
    } catch (Exception ex) {

    }
  }

  public synchronized boolean execute() throws Exception {
    boolean returnResult = false;
    try {
      AutomationRequest committRequest = generateCommitMessage();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", "Bearer " + this.jwt);
      HttpEntity<AutomationRequest> entity = new HttpEntity<AutomationRequest>(committRequest, headers);
      RestTemplate restTemplate = new RestTemplate();
      AutomationResponse response = restTemplate.exchange(this.urlString, HttpMethod.POST, entity, AutomationResponse.class).getBody();
      AutomationResponseData responseData = response.getData();
      if (responseData.isError() == false) {
        returnResult = true;
        Object changes = responseData.getChanges();
        if (changes instanceof LinkedHashMap) {
          Set<String> keys = ((LinkedHashMap) changes).keySet();
          for (String k : keys) {
            Object items = (LinkedHashMap<String, Object>) ((LinkedHashMap) changes).get(k);
            if (items instanceof LinkedHashMap) {
              changeDeviceHandle((LinkedHashMap<String, Object>) items);
            }
          }
          Thread.sleep(750);
          returnResult = execute();
        }
      }
    } catch (Exception ex) {
      returnResult = false;
    }
    return returnResult;
  }

  static class AutomationRequest {

    private String plugin;
    private AutomationRequestParams data;

    public String getPlugin() {
      return plugin;
    }

    public void setPlugin(String plugin) {
      this.plugin = plugin;
    }

    public AutomationRequestParams getData() {
      return data;
    }

    public void setData(AutomationRequestParams data) {
      this.data = data;
    }

  }

  static class AutomationRequestParams {

    private String method;
    private HashMap<Object, Object> params = new HashMap<>();

    public String getMethod() {
      return method;
    }

    public void setMethod(String method) {
      this.method = method;
    }

    public HashMap<Object, Object> getParams() {
      return params;
    }

    public void setParams(HashMap<Object, Object> params) {
      this.params = params;
    }

  }

  static class AutomationResponse {

    private String username;
    private String alpdesk_token;
    private String plugin;
    private AutomationResponseData data;

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getAlpdesk_token() {
      return alpdesk_token;
    }

    public void setAlpdesk_token(String alpdesk_token) {
      this.alpdesk_token = alpdesk_token;
    }

    public String getPlugin() {
      return plugin;
    }

    public void setPlugin(String plugin) {
      this.plugin = plugin;
    }

    public AutomationResponseData getData() {
      return data;
    }

    public void setData(AutomationResponseData data) {
      this.data = data;
    }

  }

  static class AutomationResponseData {

    private boolean error;
    private Object changes;

    public boolean isError() {
      return error;
    }

    public void setError(boolean error) {
      this.error = error;
    }

    public Object getChanges() {
      return changes;
    }

    public void setChanges(Object changes) {
      this.changes = changes;
    }

  }

}
