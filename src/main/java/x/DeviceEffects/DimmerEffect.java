package x.DeviceEffects;

import x.DeviceUtils.DeviceListUtils;
import x.Devices.DimmerDevice;

public class DimmerEffect extends BaseEffect {

    boolean bridgeValid = false;

    public DimmerEffect(int delay, String outputs) {
        super(delay, outputs);
    }

    @Override
    synchronized public void initEffect(long startTime) {
        super.initEffect(startTime);
        bridgeValid = false;
    }

    @Override
    synchronized public void trigger(boolean value) {
        if (idle == false) {
            if (value == true && (System.currentTimeMillis() - startTime) >= delay) {
                for (String output : outputs) {
                    DimmerDevice o = DeviceListUtils.getInstance().getDimmerById(output);
                    if (o.isValue() != value) {
                        o.sendMessage(value);
                    }
                }
                bridgeValid = true;
            } else if (bridgeValid && value == false) {
                for (String output : outputs) {
                    DimmerDevice o = DeviceListUtils.getInstance().getDimmerById(output);
                    if (o.isValue() != value) {
                        o.sendMessage(value);
                    }
                }
                bridgeValid = false;
                idle = true;
            }
        }
    }
}
