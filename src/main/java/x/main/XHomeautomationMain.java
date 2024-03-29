package x.main;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import x.DeviceUtils.DeviceListUtils;
import x.MessageHandling.MessageHandler;
import x.bus.ModbusMasterHomeautomation;
import x.Devices.BaseDevice;
import x.Devices.RemoteDevice;
import x.Devices.RetainDevice;
import x.bus.BaseBus;
import x.bus.ModbusMasterMemograph;
import x.retain.RetainProcess;
import x.utils.DeviceXMLParser;
import x.utils.SystemInfo;

@SpringBootApplication(scanBasePackages = {"x.rest", "x.websocket", "x.retain"})
@EntityScan("x.retain.database")
@EnableJpaRepositories("x.retain.database")
public class XHomeautomationMain implements ApplicationRunner {

  Logger logger = LoggerFactory.getLogger(XHomeautomationMain.class);

  public static final String VERSION = "2.2.9";

  @Autowired
  RetainProcess retainProcess;

  // Bus config
  private Thread busThread = null;
  private BaseBus bb = null;

  public void initBus(Properties prop, boolean simulation, int cycleTimeMS) {

    MessageHandler.getInstance();

    String bus = prop.getProperty("bus");
    logger.info("Bus-Configuration: " + bus);
    boolean busValid = false;

    if (bus.equalsIgnoreCase("modbus")) {
      String modbusIP = prop.getProperty("ip");
      bb = new ModbusMasterHomeautomation(modbusIP, simulation, cycleTimeMS);
      busValid = true;
    } else if (bus.equalsIgnoreCase("modbus_memograph")) {
      String modbusIP = prop.getProperty("ip");
      bb = new ModbusMasterMemograph(modbusIP, simulation, cycleTimeMS);
      busValid = true;
    }

    if (busValid && bb != null) {
      busThread = new Thread(new Runnable() {
        @Override
        public void run() {
          logger.info("Starting Bus: " + bus);
          bb.start();
        }
      });
      busThread.setPriority(Thread.MAX_PRIORITY);
      busThread.start();
    }

  }

  public boolean getDevices(String filename) {
    boolean returnvalue = false;
    try {

      DeviceXMLParser xmlParser = new DeviceXMLParser();
      List<BaseDevice> deviceList = xmlParser.initConfigFile(filename);
      if (deviceList != null && deviceList.size() > 0) {
        returnvalue = true;
        DeviceListUtils.getInstance().setDeviceList(deviceList);
        for (BaseDevice o : DeviceListUtils.getInstance().getDeviceList()) {
          o.idleDevice();
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return returnvalue;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    String[] argsv = args.getSourceArgs();
    for (String s : argsv) {
      logger.info("arg = " + s);
    }
    if (argsv.length >= 1) {
      logger.info("Version: " + XHomeautomationMain.VERSION);
      SystemInfo.getInstance().setVersion(VERSION);
      Properties prop = new Properties();
      BufferedInputStream stream;
      try {
        stream = new BufferedInputStream(new FileInputStream(argsv[0]));
        prop.load(stream);
        String configXML = prop.getProperty("configXml");
        String simulationString = prop.getProperty("simulation");
        boolean simulation = false;
        if (simulationString.equals("true")) {
          logger.info("Simulation activated");
          simulation = true;
        }
        String cycleTimeString = prop.getProperty("cycleTime");
        int cycleTimeMS = Integer.parseInt(cycleTimeString);
        String remoteURLString = prop.getProperty("remoteURL");
        String remoteAccessSecondsString = prop.getProperty("remoteAccessSeconds");
        int remoteTimeAccess = Integer.parseInt(remoteAccessSecondsString) * 1000;
        String remoteAccessString = prop.getProperty("remoteAccess");
        boolean remoteAccess = false;
        if (remoteAccessString.equals("true")) {
          logger.info("RemoteAccess activated: " + remoteURLString);
          remoteAccess = true;
        }
        String remoteJWTString = prop.getProperty("remoteJWT");
        boolean parseDeviceList = getDevices(configXML);
        if (parseDeviceList == true) {
          RetainDevice rd = new RetainDevice(true, retainProcess);
          rd.idleDevice();
          RemoteDevice r = new RemoteDevice(remoteAccess, remoteURLString, remoteTimeAccess, remoteJWTString);
          r.idleDevice();
          initBus(prop, simulation, cycleTimeMS);
        } else {
          logger.error("Error reading Devicelist...");
        }
      } catch (Exception ex) {
        logger.error("Error reading PropertiesFile");
      }
    } else {
      logger.error("Wrong number of Arguments: " + argsv.length);
    }
  }

  public static void main(String[] args) {
    SpringApplicationBuilder builder = new SpringApplicationBuilder(XHomeautomationMain.class);
    builder.headless(false).run(args);
  }
}
