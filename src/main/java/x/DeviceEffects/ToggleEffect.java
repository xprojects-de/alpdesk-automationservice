package x.DeviceEffects;

import x.DeviceUtils.DeviceListUtils;
import x.Devices.OutputDevice;

public class ToggleEffect extends BaseEffect {

  public ToggleEffect(int delay, String outputs) {
    super(delay, outputs);
  }

  @Override
  synchronized public void trigger(boolean value) {
    if (idle == false) {
      if (value == true && (System.currentTimeMillis() - startTime) >= delay) {
        for (String output : outputs) {
          OutputDevice o = DeviceListUtils.getInstance().getOutputById(output);
          if (o.isValue() == false) {
            o.sendMessage(true);
          } else {
            o.sendMessage(false);
          }
        }
        idle = true;
      }
    }
  }
}
