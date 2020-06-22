package x.Devices;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.time.Duration;
import java.util.Random;
import x.DeviceUtils.PropertyChangedEvent;
import x.DeviceUtils.RecorderItem;

public class BaseDevice {

    private static final ActorSystem ACTORSYSTEM = ActorSystem.create("homeautomationsystem");

    private ActorRef deviceActor = null;

    public static final int DEFAULTCYCLETIME = 50;
    private int cycleTime = BaseDevice.DEFAULTCYCLETIME;

    public int deviceHandle = 0;
    public String displayName = "";
    public String style = "";
    public String categorie = "";
    public String id = "";
    public boolean dashboard = false;
    protected RecorderItem ri = null;

    public BaseDevice(int cycleTime) {
        Random rN = new Random();
        this.id = System.currentTimeMillis() + "_" + System.nanoTime() + "_" + rN.nextInt(10000);
        this.cycleTime = cycleTime;
    }

    public BaseDevice(String id, int cycleTime) {
        this.id = id;
        this.cycleTime = cycleTime;
    }

    public void idleDevice() {
        if (deviceActor == null) {
            //deviceActor = system.actorOf(Props.create(DeviceActor.class, () -> new DeviceActor(id, cycleTime)).withDispatcher("default-dispatcher"), id);
            deviceActor = ACTORSYSTEM.actorOf(Props.create(DeviceActor.class, () -> new DeviceActor(id, cycleTime)), id);
            afterIdle();
        }
    }

    public void afterIdle() {
    }

    public int getCycleTime() {
        return this.cycleTime;
    }

    public RecorderItem getRecorderItem() {
        return ri;
    }

    public void receiveMessage(Object message) {
    }

    public void receiveIdle() {
    }

    void propertyChangedEvent(PropertyChangedEvent event) {
        try {
            event.execute(this);
        } catch (Exception ex) {
        }
    }

    public boolean sendPropertyChangedEvent(PropertyChangedEvent pcv) {
        boolean value = false;
        try {
            if (deviceActor != null) {
                deviceActor.tell(pcv, null);
                value = true;
            }
        } catch (Exception ex) {
            value = false;
        }
        return value;
    }

    public boolean sendMessage(Object message) {
        boolean value = false;
        try {
            if (deviceActor != null) {
                deviceActor.tell(message, null);
                value = true;
            }
        } catch (Exception ex) {
            value = false;
        }
        return value;
    }

    private class DeviceActor extends AbstractActor {

        final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
        private int cycleTime = BaseDevice.DEFAULTCYCLETIME;
        private String id = "";

        public DeviceActor(String id, int cycleTime) {
            this.id = id;
            this.cycleTime = cycleTime;
            getContext().setReceiveTimeout(Duration.ofMillis(cycleTime));
        }

        @Override
        public void preStart() {
            //log.info(id + " started with timeout " + cycleTime);
        }

        @Override
        public void postStop() {
            //log.info(id + " stopped");
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(ReceiveTimeout.class, message -> {
                        receiveIdle();
                    })
                    .matchAny(message -> {
                        if (message instanceof PropertyChangedEvent) {
                            propertyChangedEvent((PropertyChangedEvent) message);
                        } else {
                            receiveMessage(message);
                        }
                    })
                    .build();
        }

    }

}
