package x.DeviceEffects;

public class BaseEffect {

  long startTime = 0;
  int delay = 0;
  public boolean idle = true;
  public String[] outputs = null;

  public BaseEffect(int delay, String outputs) {
    this.delay = delay;
    this.outputs = outputs.split(";");
  }

  synchronized public void initEffect(long startTime) {
    this.idle = false;
    this.startTime = startTime;
  }

  synchronized public void resetEffect() {
    this.idle = true;
    this.startTime = 0;
  }

  synchronized public void trigger(boolean value) {
  }

}
