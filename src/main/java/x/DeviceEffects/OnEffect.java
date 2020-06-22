package x.DeviceEffects;

import x.DeviceUtils.DeviceListUtils;
import x.Devices.OutputDevice;

public class OnEffect extends BaseEffect {

  int duration = 0;
  long startTimeDuration = 0;
  boolean durationValid = false;

  public OnEffect(int duration, int delay, String outputs) {
    super(delay, outputs);
    this.duration = duration;
  }

  @Override
  synchronized public void initEffect(long startTime) {
    super.initEffect(startTime);
    durationValid = false;
    startTimeDuration = 0;
  }

  @Override
  synchronized public void trigger(boolean value) {
    if (idle == false) {
      if (value == true && (System.currentTimeMillis() - startTime) >= delay) {
        for (String output : outputs) {
          OutputDevice o = DeviceListUtils.getInstance().getOutputById(output);
          if (o.isValue() == false) {
            o.sendMessage(true);
          }
        }
        if (duration != 0) {
          if (startTimeDuration == 0) {
            startTimeDuration = System.currentTimeMillis();
            durationValid = true;
          }
        } else {
          idle = true;
        }
      }
      if (durationValid) {
        if ((System.currentTimeMillis() - startTimeDuration) >= duration) {
          for (String output : outputs) {
            OutputDevice o = DeviceListUtils.getInstance().getOutputById(output);
            if (o.isValue() == true) {
              o.sendMessage(false);
            }
          }
          startTimeDuration = 0;
          durationValid = false;
          idle = true;
        }
      }
    }
  }

}
