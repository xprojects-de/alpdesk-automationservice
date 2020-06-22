package x.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JsonUtils {

  private final HashMap<Object, Object> outputMap = new HashMap<>();

  public void putObject(Object key, Object value) {
    outputMap.put(key, value);
  }

  public String createJsonStringFromObject() {
    String json = "";
    try {
      json = new ObjectMapper().writeValueAsString(outputMap);
    } catch (JsonProcessingException ex) {
      json = "";
    }
    return json;
  }

  public static String createJsonStringFromObject(Object o) {
    String json = "";
    try {
      json = new ObjectMapper().writeValueAsString(o);
    } catch (JsonProcessingException ex) {
      json = "";
    }
    return json;
  }

  public Map parseJson(String text) {
    Map o = null;
    try {
      ObjectMapper tmp = new ObjectMapper();
      o = tmp.readValue(text, Map.class);
    } catch (IOException ex) {
      o = null;
    }
    return o;
  }

  public static void main(String[] args) {
    JsonUtils ju = new JsonUtils();
    ju.putObject("Key", "Value");
    String s = ju.createJsonStringFromObject();
    System.out.println("s = " + s);
    Map resultMap = ju.parseJson("{\"person\":{\"name\":\"Guillaume\",\"age\":33,\"pets\":[\"dog\",\"cat\"]}}");
    if (resultMap != null) {
      Set<String> keySet = resultMap.keySet();
      for (String key : keySet) {
        Object obj = resultMap.get(key);
        System.out.println("key = " + key);
        System.out.println("obj = " + obj);
      }
    }
  }
}
