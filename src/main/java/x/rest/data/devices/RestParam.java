package x.rest.data.devices;

public class RestParam {

  private String handle = "";
  private String editable = "false";
  private String visibleValue = "";
  private String type = "";
  private String value = "";
  private String displayName = "";

  public String getHandle() {
    return handle;
  }

  public void setHandle(String handle) {
    this.handle = handle;
  }

  public String getEditable() {
    return editable;
  }

  public void setEditable(String editable) {
    this.editable = editable;
  }

  public String getVisibleValue() {
    return visibleValue;
  }

  public void setVisibleValue(String visibleValue) {
    this.visibleValue = visibleValue;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

}
