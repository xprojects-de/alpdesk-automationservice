package x.Devices;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.DeviceUtils.InputParameter;
import x.DeviceEffects.BaseEffect;
import x.DeviceUtils.Types;
import x.utils.DashboardInfo;
import x.utils.PropertyInfo;

public class SceneDevice extends BaseDevice {

  private final Logger logger = LoggerFactory.getLogger(SceneDevice.class);
  private boolean value = false;
  private long startTime = 0;
  public ArrayList<InputParameter> inputParams = new ArrayList<>();

  @PropertyInfo(handle = 0, displayName = "Label", type = Types.TYPE_PROPERTIEINFO_INFO)
  @DashboardInfo(handle = 0, displayName = "Scene", editable = true)
  public String speechIdent = "";

  @PropertyInfo(handle = 1, editable = true, displayName = "Trigger", type = Types.TYPE_PROPERTIEINFO_TOGGLEACTIVATION, stateful = true, visibleOnREST = true)
  public boolean trigger = false;

  public SceneDevice(int cycleTime) {
    super(cycleTime);
  }

  @Override
  public void receiveMessage(Object message) {
    value = (boolean) message;
    logger.debug("SCENE <" + id + "> => <" + value + "> <" + trigger + ">");
    if (value) {
      startTime = System.currentTimeMillis();
      for (InputParameter param : inputParams) {
        ((BaseEffect) param.effect).resetEffect();
        ((BaseEffect) param.effect).initEffect(startTime);
        ((BaseEffect) param.effect).trigger(value);
      }
    }
    trigger = false;
    super.receiveMessage(message);
  }

  @Override
  public void receiveIdle() {
    for (InputParameter param : inputParams) {
      ((BaseEffect) param.effect).trigger(value);
    }
    super.receiveIdle();
    if (trigger) {
      this.sendMessage(true);
    }
  }
}
