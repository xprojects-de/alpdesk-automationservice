package x.utils;

public class MathParser {

  public static float parse(int value, String exp) {

    float nValue = value;

    if (exp == null || exp.isEmpty()) {
      return nValue;
    }

    try {
      String[] expParams = exp.split(";");
      if (expParams.length > 0) {
        for (String s : expParams) {
          nValue = doJob((float) nValue, s);
        }
      }
    } catch (Exception ex) {
      nValue = value;
    }

    return nValue;
  }

  private static float doJob(float value, String exp) {

    float nValue = value;

    if (exp.startsWith("+")) {
      nValue = nValue + Float.parseFloat(exp.substring(1));
    } else if (exp.startsWith("-")) {
      nValue = nValue - Float.parseFloat(exp.substring(1));
    } else if (exp.startsWith("/")) {
      float mathValue = Float.parseFloat(exp.substring(1));
      if (mathValue != 0) {
        nValue = nValue / mathValue;
      }
    } else if (exp.startsWith("*")) {
      nValue = nValue * Float.parseFloat(exp.substring(1));
    }

    return nValue;
  }
}
