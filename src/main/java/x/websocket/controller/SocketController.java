package x.websocket.controller;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import x.MessageHandling.MessageHandler;
import x.utils.JsonBuilder;
import x.websocket.model.AsyncStatusMessage;
import x.websocket.model.ConnectRequest;
import x.websocket.model.ConnectResponse;
import x.websocket.model.ConnectionManager;
import x.websocket.model.RequestMessage;

@Controller
public class SocketController {

  private final Logger logger = LoggerFactory.getLogger(SocketController.class);

  @Autowired
  @Qualifier("ConnectionManager")
  ConnectionManager connectionManager;

  @MessageMapping("/connect")
  @SendTo("/reply/connect")
  public ConnectResponse connect(SimpMessageHeaderAccessor accessor, ConnectRequest request) throws Exception {
    return new ConnectResponse(connectionManager.addConnection(UUID.randomUUID()));
  }

  @MessageMapping("/disconnect/{connectionId}")
  public void disconnect(@DestinationVariable String connectionId) throws Exception {
    connectionManager.destroyConnection(UUID.fromString(connectionId));
  }

  @MessageMapping("/watchdog/{connectionId}")
  public void watchdog(@DestinationVariable String connectionId) throws Exception {
    connectionManager.watchdog(UUID.fromString(connectionId));
  }

  @MessageMapping("/init/{connectionId}")
  @SendTo("/reply/init/{connectionId}")
  public JsonBuilder.DeviceHandleList init(@DestinationVariable String connectionId) {
    logger.debug("Received Init");
    JsonBuilder.DeviceHandleList value = new JsonBuilder.DeviceHandleList();
    if (connectionManager.isConnectionValid(UUID.fromString(connectionId))) {
      JsonBuilder jsonBuilder = new JsonBuilder();
      value = jsonBuilder.fromDeviceHandleList();
      connectionManager.setInitDone(UUID.fromString(connectionId));
    }
    return value;
  }

  @MessageMapping("/message/{connectionId}")
  @SendTo("/reply/{connectionId}")
  public AsyncStatusMessage message(@DestinationVariable String connectionId, RequestMessage req) {
    logger.debug("Received: " + req.getMessage());
    AsyncStatusMessage reply = new AsyncStatusMessage();
    reply.setError(AsyncStatusMessage.ERROR);
    if (connectionManager.isConnectionValid(UUID.fromString((connectionId)))) {
      reply.setError(AsyncStatusMessage.NOERROR);
      reply.setValue(req.getMessage());
      reply.setKind(AsyncStatusMessage.STATUS);
      //reply.setKind(AsyncStatusMessage.INFO);
      if (req.getMessage().startsWith("!")) {
        String[] messageIdentifier = req.getMessage().substring(1).split("#");
        if (messageIdentifier.length == 2) {
          //messageToSend = jsonBuilder.fromString(JsonBuilder.INFO, JsonBuilder.NOERROR, messageIdentifier[0], messageIdentifier[1]);
          MessageHandler.getInstance().messageToDevice(req.getMessage().substring(1));
        }
      }
    }

    return reply;
  }

}
