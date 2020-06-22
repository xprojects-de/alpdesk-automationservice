package x.rest.data;

public class StatusResponse {

  private String error = "";
  private String echo = "";

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getEcho() {
    return echo;
  }

  public void setEcho(String echo) {
    this.echo = echo;
  }

}
