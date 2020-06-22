package x.utils;

import java.lang.reflect.Field;

public class FieldParser {

  public static String convertField(Field field, Object device) {
    String value = "";
    try {
      if (field.getType().equals(Integer.TYPE)) {
        value = "" + field.getInt(device);
      } else if (field.getType().equals(Long.TYPE)) {
        value = "" + field.getLong(device);
      } else if (field.getType().equals(Boolean.TYPE)) {
        value = "" + field.getBoolean(device);
        if (value.equals("true")) {
          value = "ON";
        } else {
          value = "OFF";
        }
      } else if (field.getType().equals(Float.TYPE)) {
        value = "" + field.getFloat(device);
      } else if (field.getType().equals(String.class)) {
        value = (String) field.get(device);
      }
    } catch (IllegalAccessException | IllegalArgumentException ex) {
      value = "";
    }
    return value;
  }

  public static boolean convertFieldState(Field field, Object device) {
    boolean returnvalue = false;
    try {
      if (field.getType().equals(Integer.TYPE)) {
        int value = field.getInt(device);
        if (value > 0) {
          returnvalue = true;
        }
      } else if (field.getType().equals(Long.TYPE)) {
        long value = field.getLong(device);
        if (value > 0) {
          returnvalue = true;
        }
      } else if (field.getType().equals(Boolean.TYPE)) {
        returnvalue = field.getBoolean(device);
      } else if (field.getType().equals(Float.TYPE)) {
        float value = field.getFloat(device);
        if (value > 0) {
          returnvalue = true;
        }
      } else if (field.getType().equals(String.class)) {
        String value = (String) field.get(device);
        if (!"0".equals(value)) {
          returnvalue = true;
        }
      }
    } catch (IllegalAccessException | IllegalArgumentException ex) {
      returnvalue = false;
    }
    return returnvalue;
  }
}
