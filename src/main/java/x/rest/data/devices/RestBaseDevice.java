package x.rest.data.devices;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;

public class RestBaseDevice {

  private String nameCategory = "";
  private String handleDevice = "";
  private String idDevice = "";
  private String typeDevice = "";
  private String displayNameDevice = "";
  private String styleDevice = "";
  private String sollvalue = "";
  private String value = "";
  private String activ = "false";
  private String temperature = "";
  private String humanity = "";
  private String runningState = "false";
  private String forceActiv = "false";
  private ArrayList<RestParam> params = new ArrayList<>();

  public String getNameCategory() {
    return nameCategory;
  }

  public void setNameCategory(String nameCategory) {
    this.nameCategory = nameCategory;
  }

  public String getHandleDevice() {
    return handleDevice;
  }

  public void setHandleDevice(String handleDevice) {
    this.handleDevice = handleDevice;
  }

  public String getIdDevice() {
    return idDevice;
  }

  public void setIdDevice(String idDevice) {
    this.idDevice = idDevice;
  }

  public String getTypeDevice() {
    return typeDevice;
  }

  public void setTypeDevice(String typeDevice) {
    this.typeDevice = typeDevice;
  }

  public String getDisplayNameDevice() {
    return displayNameDevice;
  }

  public void setDisplayNameDevice(String displayNameDevice) {
    this.displayNameDevice = displayNameDevice;
  }

  public String getStyleDevice() {
    return styleDevice;
  }

  public void setStyleDevice(String styleDevice) {
    this.styleDevice = styleDevice;
  }

  public String getSollvalue() {
    return sollvalue;
  }

  public void setSollvalue(String sollvalue) {
    this.sollvalue = sollvalue;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getActiv() {
    return activ;
  }

  public void setActiv(String activ) {
    this.activ = activ;
  }

  public String getTemperature() {
    return temperature;
  }

  public void setTemperature(String temperature) {
    this.temperature = temperature;
  }

  public String getHumanity() {
    return humanity;
  }

  public void setHumanity(String humanity) {
    this.humanity = humanity;
  }

  public String getRunningState() {
    return runningState;
  }

  public void setRunningState(String runningState) {
    this.runningState = runningState;
  }

  public String getForceActiv() {
    return forceActiv;
  }

  public void setForceActiv(String forceActiv) {
    this.forceActiv = forceActiv;
  }

  @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
  public ArrayList<RestParam> getParams() {
    return params;
  }

  public void setParams(ArrayList<RestParam> params) {
    this.params = params;
  }

}
