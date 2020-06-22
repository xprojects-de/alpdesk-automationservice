package x.websocket.model;

import java.util.UUID;

public class ConnectResponse {

  private String connectionId;

  public ConnectResponse(UUID uuid) {
    this.connectionId = uuid.toString();
  }
  
  public String getConnectionId() {
    return connectionId;
  }

  public void setConnectionId(String connectionId) {
    this.connectionId = connectionId;
  }

}
