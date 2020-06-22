package x.rest.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import x.rest.data.devices.RestBaseDevice;

public class DashboardResponse {

  private int error = 0;
  private String tstamp = "";
  private ArrayList<RestBaseDevice> devices = new ArrayList<>();

  public int getError() {
    return error;
  }

  public void setError(int error) {
    this.error = error;
  }

  public String getTstamp() {
    return tstamp;
  }

  public void setTstamp(String tstamp) {
    this.tstamp = tstamp;
  }

  @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
  public ArrayList<RestBaseDevice> getDevices() {
    return devices;
  }

  public void setDevices(ArrayList<RestBaseDevice> devices) {
    this.devices = devices;
  }

}
