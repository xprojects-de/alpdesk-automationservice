package x.bus;

import java.util.Random;
import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.BitVector;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import x.DeviceUtils.DeviceListUtils;
import x.DeviceUtils.TransferObject;
import x.Devices.AnalogInDevice;
import x.Devices.InputDevice;
import x.Devices.OutputDevice;
import x.Devices.TemperatureDevice;
import x.Devices.DimmerDevice;
import x.Devices.SensorTemperatureDevice;

public class ModbusMasterHomeautomation extends BaseBus {

  private final Logger logger = LoggerFactory.getLogger(ModbusMasterHomeautomation.class);

  private int cycleTime = 75;
  private long worstCycleTime = 75;
  long startTime = 0;
  long elapsedTime = 0;
  int cyclicCounter = 0;
  boolean modBusInit = false;
  boolean sendUpdateInfo = true;
  private String modbusHost = "192.168.1.1";
  private final int modbusPort = Modbus.DEFAULT_PORT;
  ModbusTCPMaster modbusMaster = null;
  BitVector bvOutput = null;
  BitVector bvInput = null;
  private boolean simulation = false;
  public static final int modbusDigitalInputOffset = 0;
  public static final int modbusDigitalOutputOffset = 0x200;
  public static final int modbusAnalogOutputOffset = 0x200;
  private static final int modbusUnitID = 1;

  public ModbusMasterHomeautomation(String modbusIP, boolean simulation, int cycleTimeMS) {
    this.modbusHost = modbusIP;
    this.simulation = simulation;
    this.cycleTime = cycleTimeMS;
    this.worstCycleTime = (long) cycleTimeMS;
    initModbus();
  }

  private void initModbus() {
    if (this.simulation == false) {
      this.modbusMaster = new ModbusTCPMaster(this.modbusHost, this.modbusPort);
    } else {
      logger.info("ModbusTCPMaster startet in Simulation!");
    }

  }

  private void connectModbus(boolean reconnect) {

    if (reconnect && simulation == false) {
      modbusMaster.disconnect();
      try {
        Thread.sleep(10000);
      } catch (InterruptedException ex) {
        logger.error(ex.getMessage());
      }
      modbusMaster = null;
      this.modbusMaster = new ModbusTCPMaster(this.modbusHost, this.modbusPort);
    }

    if (modbusMaster != null) {
      try {
        modbusMaster.connect();
        modBusInit = true;
      } catch (Exception ex) {
        logger.error(ex.getMessage());
      }
    } else if (simulation) {
      modBusInit = true;
    }
  }

  @Override
  public void start() {

    connectModbus(false);

    if (modBusInit == true) {
      while (true) {

        sendUpdateInfo = true;

        startTime = System.currentTimeMillis();

        try {
          readModbusDigitalInputs();
        } catch (Exception ex) {
          logger.error(ex.getMessage());
          connectModbus(true);
          sendUpdateInfo = false;
        }

        try {
          writeModbusDigitalOutputs();
        } catch (Exception ex) {
          logger.error(ex.getMessage());
          connectModbus(true);
          sendUpdateInfo = false;
        }

        try {
          writeModbusDimmerAnalogOutputs();
        } catch (Exception ex) {
          logger.error(ex.getMessage());
          connectModbus(true);
          sendUpdateInfo = false;
        }

        if (cyclicCounter % 150 == 0) {
          try {
            readModbusAnalogInputs();
          } catch (Exception ex) {
            logger.error(ex.getMessage());
            connectModbus(true);
            sendUpdateInfo = false;
          }
        }

        if (cyclicCounter % 50 == 0 && sendUpdateInfo) {
          updateDeviceHandleStatusForWebSocket(cycleTime, elapsedTime, worstCycleTime);
        }

        if (cyclicCounter >= 1000 && sendUpdateInfo) {
          cyclicCounter = 0;
          logger.debug("INFO <" + cycleTime + "ms><" + elapsedTime + "ms><" + worstCycleTime + "ms>");
        }

        elapsedTime = System.currentTimeMillis() - startTime;
        if (elapsedTime < cycleTime) {
          try {
            Thread.sleep(cycleTime - elapsedTime);
          } catch (InterruptedException ex) {
            logger.error(ex.getMessage());
          }
        } else {
          if (elapsedTime > worstCycleTime) {
            worstCycleTime = elapsedTime;
          }
        }

        cyclicCounter++;
      }
    } else {
      logger.error("Error init Modbus!!");
    }
  }

  private void readModbusDigitalInputs() throws Exception {
    if (simulation == false) {
      int max = DeviceListUtils.getInstance().getMaxInputBusAddress();
      if (max >= 0) {
        int bitCount = max + 1;
        bvInput = modbusMaster.readInputDiscretes(ModbusMasterHomeautomation.modbusDigitalInputOffset, bitCount);
        for (int address = 0; address < bitCount; address++) {
          for (Object deviceHandle : DeviceListUtils.getInstance().getDeviceList()) {
            if (deviceHandle instanceof InputDevice) {
              if (((InputDevice) deviceHandle).busAddress == address) {
                if (((InputDevice) deviceHandle).isLockedByVisu() == false && ((InputDevice) deviceHandle).isValue() != bvInput.getBit(address)) {
                  ((InputDevice) deviceHandle).sendMessage(bvInput.getBit(address));
                }
                break;
              }
            }
          }
        }
      }
    }
  }

  private void writeModbusDigitalOutputs() throws Exception {
    if (simulation == false) {
      int max = DeviceListUtils.getInstance().getMaxOutputBusAddress();
      if (max >= 0) {
        int bitCount = max + 1;
        bvOutput = modbusMaster.readCoils(ModbusMasterHomeautomation.modbusDigitalOutputOffset, bitCount);
        for (int address = 0; address < bitCount; address++) {
          for (Object deviceHandle : DeviceListUtils.getInstance().getDeviceList()) {
            if (deviceHandle instanceof OutputDevice) {
              if (((OutputDevice) deviceHandle).busAddress == address) {
                if (bvOutput.getBit(address) != ((OutputDevice) deviceHandle).isValue()) {
                  modbusMaster.writeCoil(ModbusMasterHomeautomation.modbusUnitID, ((OutputDevice) deviceHandle).busAddress, ((OutputDevice) deviceHandle).isValue());
                }
                break;
              }
            }
          }
        }
      }
    }
  }

  private void readModbusAnalogInputs() throws Exception {
    if (simulation == false) {
      int max = DeviceListUtils.getInstance().getMaxAnalogInBusAddress();
      if (max >= 0) {
        int bitCount = max + 1;
        InputRegister iReg[] = modbusMaster.readInputRegisters(0, bitCount);
        for (int address = 0; address < bitCount; address++) {
          for (Object deviceHandle : DeviceListUtils.getInstance().getDeviceList()) {
            if (deviceHandle instanceof TemperatureDevice) {
              if (((TemperatureDevice) deviceHandle).busAddress == address) {
                ((TemperatureDevice) deviceHandle).sendMessage(iReg[address].getValue());
                break;
              }
            } else if (deviceHandle instanceof SensorTemperatureDevice) {
              if (((SensorTemperatureDevice) deviceHandle).busAddress == address) {
                ((SensorTemperatureDevice) deviceHandle).sendMessage(iReg[address].getValue());
                break;
              }
            } else if (deviceHandle instanceof AnalogInDevice) {
              if (((AnalogInDevice) deviceHandle).busAddress == address) {
                ((AnalogInDevice) deviceHandle).sendMessage(iReg[address].getValue());
                break;
              }
            }
          }
        }
      }
    } else {
      Random r = new Random();
      for (Object deviceHandle : DeviceListUtils.getInstance().getDeviceList()) {
        if (deviceHandle instanceof TemperatureDevice) {
          ((TemperatureDevice) deviceHandle).sendMessage(r.nextInt(300));
        } else if (deviceHandle instanceof SensorTemperatureDevice) {
          ((SensorTemperatureDevice) deviceHandle).sendMessage(200 + r.nextInt(500));
        } else if (deviceHandle instanceof AnalogInDevice) {
          ((AnalogInDevice) deviceHandle).sendMessage(200 + r.nextInt(500));
        }
      }
    }
  }

  private void writeModbusDimmerAnalogOutputs() throws Exception {
    for (Object deviceHandle : DeviceListUtils.getInstance().getDeviceList()) {
      if (deviceHandle instanceof DimmerDevice) {
        if (((DimmerDevice) deviceHandle).isStateChanged()) {
          ((DimmerDevice) deviceHandle).sendMessage(new TransferObject(false));
          int value = ((DimmerDevice) deviceHandle).getLiveValue();
          if (value > DimmerDevice.MAX_VALUE) {
            value = DimmerDevice.MAX_VALUE;
          } else if (value < DimmerDevice.MIN_VALUE) {
            value = DimmerDevice.MIN_VALUE;
          }
          SimpleRegister outregister = new SimpleRegister(value);
          if (simulation == false) {
            modbusMaster.writeSingleRegister(((DimmerDevice) deviceHandle).busAddress, outregister);
            // Read Back
            //InputRegister iReg[] = modbusMaster.readInputRegisters(XModbusMaster.modbusAnalogOutputOffset, ((DimmerDevice) deviceHandle).busAddress + 1);
          }
          logger.debug("SPS-DIMMERVALUE <" + outregister.getValue() + ">");
        }
      }
    }
  }
}
