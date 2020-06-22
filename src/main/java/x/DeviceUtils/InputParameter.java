package x.DeviceUtils;

import x.DeviceEffects.BaseEffect;

public class InputParameter {

  public int type;
  public BaseEffect effect = null;

  public InputParameter(int type, BaseEffect effect) {
    this.type = type;
    this.effect = effect;
  }
}
