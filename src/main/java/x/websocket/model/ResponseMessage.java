package x.websocket.model;

public class ResponseMessage {

  private boolean error = true;
  private String mesg;

  public boolean getError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public String getMesg() {
    return mesg;
  }

  public void setMesg(String mesg) {
    this.mesg = mesg;
  }

}
