package x.DeviceUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {

  SimpleDateFormat dateFormat = null;
  SimpleDateFormat dateFormatd = null;
  SimpleDateFormat dateFormatt = null;
  SimpleDateFormat dateFormatts = null;
  Calendar now = null;
  Calendar xmlDateStart = null;
  Calendar xmlDateStop = null;
  Calendar dateTemp = null;

  public DateUtils() {
    this.dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    this.dateFormatd = new SimpleDateFormat("dd.MM");
    this.dateFormatt = new SimpleDateFormat("HH:mm:ss");
    this.dateFormatts = new SimpleDateFormat("HH:mm");
  }

  public boolean checkStatus(String timeStart, String timeStop) {
    try {
      if (!timeStart.equals("") && !timeStop.equals("")) {
        now = Calendar.getInstance();
        xmlDateStart = Calendar.getInstance();
        xmlDateStop = Calendar.getInstance();
        xmlDateStart.setTime(dateFormat.parse(dateFormatd.format(now.getTime()) + "." + now.get(Calendar.YEAR) + " " + timeStart));
        xmlDateStop.setTime(dateFormat.parse(dateFormatd.format(now.getTime()) + "." + now.get(Calendar.YEAR) + " " + timeStop));

        if (xmlDateStop.compareTo(xmlDateStart) < 0) {
          if (now.compareTo(xmlDateStop) < 0) {
            now.add(Calendar.DATE, 1);
          }
          xmlDateStop.add(Calendar.DATE, 1);
        }

        return now.after(xmlDateStart) && now.before(xmlDateStop);
      } else {
        return false;
      }
    } catch (ParseException ex) {
      return false;
    }
  }

  public String[] getCompleteDateString() {
    String[] data = new String[2];
    if (xmlDateStart != null) {
      data[0] = dateFormat.format(xmlDateStart.getTime());
    }
    if (xmlDateStop != null) {
      data[1] = dateFormat.format(xmlDateStop.getTime());
    }
    return data;
  }

  public String increaseTime(String currentTime, int s) {
    String returnvalue = currentTime;
    Calendar temp = Calendar.getInstance();
    try {
      temp.setTime(dateFormat.parse(dateFormatd.format(Calendar.getInstance().getTime()) + "." + Calendar.getInstance().get(Calendar.YEAR) + " " + currentTime));
      temp.add(Calendar.SECOND, s);
      returnvalue = dateFormatt.format(temp.getTime());
    } catch (ParseException ex) {
    }
    return returnvalue;
  }

  public String getHourMinuteString() {
    return dateFormatts.format(Calendar.getInstance().getTime());
  }

  public String getTimeStampString() {
    return dateFormat.format(Calendar.getInstance().getTime());
  }
}
