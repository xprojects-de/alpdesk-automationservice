package x.utils;

public class SystemInfo {

  private static SystemInfo singleton = null;

  private String version = "";
  private String lastBusUpdate = "";

  public SystemInfo() {
  }

  public static SystemInfo getInstance() {
    if (singleton == null) {
      singleton = new SystemInfo();
    }
    return singleton;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getLastBusUpdate() {
    return lastBusUpdate;
  }

  public void setLastBusUpdate(String lastBusUpdate) {
    this.lastBusUpdate = lastBusUpdate;
  }

}
