package x.bus;

import java.util.Random;
import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.BitVector;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import x.DeviceUtils.DeviceListUtils;
import x.Devices.AnalogInDevice;
import x.Devices.OutputDevice;

/**
 * Following FC are supported : 03: Read Holding Register, 16: Write Multiple *
 * Register und 06 Write Single Register
 *
 */
public class ModbusMasterMemograph extends BaseBus {

  private final Logger logger = LoggerFactory.getLogger(ModbusMasterMemograph.class);

  private int cycleTime = 75;
  private long worstCycleTime = 75;
  long startTime = 0;
  long elapsedTime = 0;
  int cyclicCounter = 0;
  boolean modBusInit = false;
  private String modbusHost = "192.168.1.1";
  private final int modbusPort = Modbus.DEFAULT_PORT;
  ModbusTCPMaster modbusMaster = null;
  BitVector bvOutput = null;
  BitVector bvInput = null;
  private boolean simulation = false;
  private static final int modbusUnitID = 1;

  public ModbusMasterMemograph(String modbusIP, boolean simulation, int cycleTimeMS) {
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
        Thread.sleep(5000);
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
        startTime = System.currentTimeMillis();

        if (cyclicCounter % 50 == 0) {
          try {
            writeModbusDigital();
          } catch (ModbusException ex) {
            logger.error(ex.getMessage());
            connectModbus(true);
          }
        }

        if (cyclicCounter % 50 == 0) {
          try {
            readModbusMultiRegister();
          } catch (ModbusException ex) {
            logger.error(ex.getMessage());
            connectModbus(true);
          }
        }

        if (cyclicCounter % 50 == 0) {
          updateDeviceHandleStatusForWebSocket(cycleTime, elapsedTime, worstCycleTime);
        }

        if (cyclicCounter >= 1000) {
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

  private void writeModbusDigital() throws ModbusException {
    if (simulation == false) {
      short outputs = 0x0000;
      /**
       * 0000000000000000 => set Output 1 and Output 4 => 0000000000001001
       */
      for (Object deviceHandle : DeviceListUtils.getInstance().getDeviceList()) {
        if (deviceHandle instanceof OutputDevice) {
          int bAddress = ((OutputDevice) deviceHandle).busAddress;
          if (bAddress < 16) {
            if (((OutputDevice) deviceHandle).isValue()) {
              outputs |= ((1 << bAddress) & 0xFFFF);
            }
          }
        }
      }
      SimpleRegister[] r = new SimpleRegister[1];
      r[0] = new SimpleRegister(outputs);
      modbusMaster.writeMultipleRegisters(1240, r);
    }
  }

  private void readModbusMultiRegister() throws ModbusException {
    if (simulation == false) {
      for (Object deviceHandle : DeviceListUtils.getInstance().getDeviceList()) {
        if (deviceHandle instanceof AnalogInDevice) {
          InputRegister iReg[] = modbusMaster.readMultipleRegisters(((AnalogInDevice) deviceHandle).busAddress, 6);
          Float f = Float.intBitsToFloat((iReg[1].getValue() << 16) + iReg[2].getValue());
          ((AnalogInDevice) deviceHandle).sendMessage(f);
        }
      }
    } else {
      Random r = new Random();
      for (Object deviceHandle : DeviceListUtils.getInstance().getDeviceList()) {
        if (deviceHandle instanceof AnalogInDevice) {
          ((AnalogInDevice) deviceHandle).sendMessage(200 + r.nextInt(500));
        }
      }
    }
  }
}
