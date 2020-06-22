package x.websocket.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import x.MessageHandling.MessageHandler;

@Controller
@Configuration
@EnableScheduling
@Component("ConnectionManager")
public class ConnectionManager {

  private final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
  private final ArrayList<Status> connections = new ArrayList<>();

  @Autowired
  private SimpMessagingTemplate webSocket;

  public ConnectionManager() {
    MessageHandler.getInstance().setConnectionManager(this);
  }

  public UUID addConnection(UUID ipAddress) {
    synchronized (connections) {
      boolean found = false;
      for (Status s : connections) {
        if (s.getUuid().equals(ipAddress)) {
          found = true;
          break;
        }
      }
      if (found == false) {
        connections.add(new Status(ipAddress));
        logger.debug("add WS-Connection: " + ipAddress.toString());
      } else {
        logger.debug("still in List WS-Connection: " + ipAddress.toString());
      }
    }

    return ipAddress;
  }

  public void destroyConnection(UUID uuid) {
    synchronized (connections) {
      for (Status s : connections) {
        if (s.getUuid().equals(uuid)) {
          connections.remove(s);
          logger.debug("destroyed WS-Connection: " + uuid.toString());
          logger.debug("Open Connections: " + connections.size());
          break;
        }
      }
      cleanConnections();
    }

  }

  public void watchdog(UUID uuid) {
    synchronized (connections) {
      for (Status s : connections) {
        if (s.getUuid().equals(uuid)) {
          s.triggerWatchdog();
          logger.debug("Trigger Watchdog for WS-Connection: " + uuid.toString());
          break;
        }
      }
      cleanConnections();
    }
  }

  public boolean isConnectionValid(UUID uuid) {
    boolean value = false;
    for (Status s : connections) {
      if (s.getUuid().equals(uuid)) {
        value = true;
        logger.debug("valid WS-Connection: " + uuid.toString());
        break;
      }
    }
    return value;
  }

  public void setInitDone(UUID uuid) {
    for (Status s : connections) {
      if (s.getUuid().equals(uuid)) {
        s.setInitDone(true);
        logger.debug("set Init done WS-Connection: " + uuid.toString());
        break;
      }
    }
  }

  public void cleanConnections() {
    synchronized (connections) {
      Iterator<Status> it = connections.iterator();
      while (it.hasNext()) {
        Status s = it.next();
        if (s.isWatchdogTimeout()) {
          logger.debug("destroy WS-Connection: " + s.getUuid() + " because of Watchdog");
          it.remove();
          logger.debug("Open Connections: " + connections.size());
        }
      }
    }
  }

  public ArrayList<Status> getConnections() {
    return connections;
  }

  public void sendMessage(UUID u, Object message) {
    webSocket.convertAndSend("/reply/" + u.toString(), message);
  }

  public static class Status {

    private UUID uuid;
    private boolean initDone = false;
    private long watchDogTime = 0;
    private long watchdogTimeout = 20000;

    public Status(UUID uuid) {
      this.uuid = uuid;
      this.watchDogTime = System.currentTimeMillis();
    }

    public UUID getUuid() {
      return uuid;
    }

    public void setUuid(UUID uuid) {
      this.uuid = uuid;
    }

    public boolean isInitDone() {
      return initDone;
    }

    public void setInitDone(boolean initDone) {
      this.initDone = initDone;
    }

    public void triggerWatchdog() {
      this.watchDogTime = System.currentTimeMillis();
    }

    public boolean isWatchdogTimeout() {
      return ((System.currentTimeMillis() - watchdogTimeout) > watchDogTime);
    }

  }

}
