package x.DeviceUtils;

import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import x.Devices.BaseDevice;
import x.utils.PropertyInfo;

public class PropertyChangedEvent {

  private final Logger logger = LoggerFactory.getLogger(PropertyChangedEvent.class);

  private final Field f;
  private final PropertyInfo p;
  private final String value;
  private final boolean byDatabase;

  public PropertyChangedEvent(Field f, PropertyInfo p, String value, boolean byDatabase) {
    this.f = f;
    this.p = p;
    this.value = value;
    this.byDatabase = byDatabase;
  }

  public void execute(BaseDevice d) throws Exception {
    if (byDatabase == true) {
      executeByDatabase(d);
    } else {
      executeByValue(d);
    }
  }

  private void executeByDatabase(BaseDevice d) throws Exception {
    switch (p.type()) {
      case Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE: {
        logger.debug("DB-PROPERTY TYPE_PROPERTIEINFO_CHANGESOLLVALUE <" + d.id + "><" + p.displayName() + "/" + p.handle() + "> => <" + value + ">");
        if (f.getType().isAssignableFrom(Integer.TYPE)) {
          f.set(d, Integer.parseInt(value));
        } else if (f.getType().isAssignableFrom(String.class)) {
          f.set(d, value);
        }
        break;
      }
      case Types.TYPE_PROPERTIEINFO_TOGGLEACTIVATION: {
        if (f.getType().isAssignableFrom(Boolean.TYPE)) {
          f.set(d, value.equalsIgnoreCase("ON"));
          logger.debug("DB-PROPERTY TYPE_PROPERTIEINFO_TOGGLEACTIVATION <" + d.id + "><" + p.displayName() + "/" + p.handle() + "> => <" + value.equalsIgnoreCase("ON") + ">");
        }
        break;
      }
      default:
        break;
    }
  }

  private void executeByValue(BaseDevice d) throws Exception {
    switch (p.type()) {
      case Types.TYPE_PROPERTIEINFO_CHANGESOLLVALUE: {
        logger.debug("PROPERTY TYPE_PROPERTIEINFO_CHANGESOLLVALUE <" + d.id + "><" + p.displayName() + "/" + p.handle() + "> => <" + value + ">");
        switch (value) {
          case "+": {
            if (f.getType().isAssignableFrom(Integer.TYPE)) {
              int temp = (int) f.get(d);
              temp++;
              f.set(d, temp);
            } else if (f.getType().isAssignableFrom(String.class)) {
              String temp = (String) f.get(d);
              if (p.propertyType() == Types.PROPERTYTYPE_TIME) {
                if (!temp.equals("-")) {
                  DateUtils dUtils = new DateUtils();
                  temp = dUtils.increaseTime(temp, p.stepValue());
                }
              }
              f.set(d, temp);
            }
            break;
          }
          case "-": {
            if (f.getType().isAssignableFrom(Integer.TYPE)) {
              int temp = (int) f.get(d);
              temp--;
              f.set(d, temp);
            } else if (f.getType().isAssignableFrom(String.class)) {
              String temp = (String) f.get(d);
              if (p.propertyType() == Types.PROPERTYTYPE_TIME) {
                if (!temp.equals("-")) {
                  DateUtils dUtils = new DateUtils();
                  temp = dUtils.increaseTime(temp, -(p.stepValue()));
                }
              }
              f.set(d, temp);
            }
            break;
          }
        }
        break;
      }
      case Types.TYPE_PROPERTIEINFO_TOGGLEACTIVATION: {
        if (f.getType().isAssignableFrom(Boolean.TYPE)) {
          boolean temp = (boolean) f.get(d);
          temp = temp != true;
          f.set(d, temp);
          logger.debug("PROPERTY TYPE_PROPERTIEINFO_TOGGLEACTIVATION <" + d.id + "><" + p.displayName() + "/" + p.handle() + "> => <" + temp + ">");
        }
        break;
      }
      default:
        break;
    }
  }
}
