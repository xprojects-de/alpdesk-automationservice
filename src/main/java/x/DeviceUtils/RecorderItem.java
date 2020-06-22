package x.DeviceUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecorderItem {

  private final Logger logger = LoggerFactory.getLogger(RecorderItem.class);
  public static final int LEGENDTYPE_DATE_HOUT_MINUTE = 1;

  private int legendType = 0;
  private int maxEntries = 20;
  private long triggerTime = 1000;
  private int timeLaps = 0;
  private Queue<Item> itemsFifo = null;
  private DateUtils du = null;
  private boolean active = false;
  private boolean firstRun = true;

  public RecorderItem(int maxEntries, int legendType, int timeLaps) {
    this.maxEntries = maxEntries;
    this.legendType = legendType;
    this.timeLaps = timeLaps;
    this.itemsFifo = new LinkedList<>();
    this.du = new DateUtils();
    this.triggerTime = System.currentTimeMillis();
    this.active = false;
  }

  public int getMaxEntries() {
    return maxEntries;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
    if (active == false && itemsFifo != null && itemsFifo.size() > 0) {
      itemsFifo.clear();
    }
  }

  synchronized public ArrayList<ResultItem> getAllItems() {
    ArrayList<ResultItem> items = new ArrayList<>();
    if (active && itemsFifo != null) {
      Object[] tempItems = itemsFifo.toArray();
      if (tempItems != null) {
        for (Object o : tempItems) {
          if (o != null) {
            ResultItem ri = new ResultItem(((Item) o).getX(), ((Item) o).getY(), ((Item) o).getTimeStamp(), ((Item) o).getDateTime());
            items.add(ri);
          }
        }
      }
    }
    return items;
  }

  synchronized public void clearItems() {
    if (itemsFifo != null) {
      itemsFifo.clear();
    }
  }

  synchronized public void addItem(InsertItem temp, String parentId) {
    if (active && checkValid()) {
      if (itemsFifo != null) {
        if (itemsFifo.size() >= maxEntries) {
          itemsFifo.poll();
        }
        Item i = new Item(temp.getY());
        itemsFifo.add(i);
        logger.debug("RECORD <" + parentId + ">");
      }
    }
  }

  private boolean checkValid() {
    boolean returnvalue = false;
    if (System.currentTimeMillis() >= (triggerTime + timeLaps) || firstRun) {
      returnvalue = true;
      firstRun = false;
      triggerTime = System.currentTimeMillis();
    }
    return returnvalue;
  }

  public static class InsertItem {

    private Object[] y = null;

    public InsertItem(Object[] y) {
      this.y = y;
    }

    public Object[] getY() {
      return y;
    }
  }

  public static class ResultItem {

    private Object[] y = null;
    private String x = "";
    private long timeStamp = 0;
    private String dateTime = "";

    public ResultItem(String x, Object[] y, long timeStamp, String dateTime) {
      this.x = x;
      this.y = y;
      this.timeStamp = timeStamp;
      this.dateTime = dateTime;
    }

    public String getX() {
      return x;
    }

    public Object[] getY() {
      return y;
    }

    public int getYDimension() {
      return y.length;
    }

    public String getYasString() {
      String value = "";
      if (y != null && y.length > 0) {
        int counter = 0;
        for (Object o : y) {
          if (counter > 0) {
            value += "|";
          }
          String writeValue = "";
          if (o instanceof Boolean) {
            writeValue = ((Boolean) o == true ? "1" : "0");
          } else {
            writeValue = "" + o;
          }
          value += writeValue;
          counter++;
        }
      }
      return value;
    }

    public long getTimeStamp() {
      return timeStamp;
    }

    public String getDateTime() {
      return dateTime;
    }
  }

  private class Item {

    private Object[] y = null;
    private String x = "";
    private long timeStamp = 0;
    private String dateTime = "";

    public Item(Object[] y) {
      this.y = y;
      switch (legendType) {
        case RecorderItem.LEGENDTYPE_DATE_HOUT_MINUTE: {
          x = du.getHourMinuteString();
          break;
        }
        default:
          x = du.getHourMinuteString();
          break;
      }
      this.timeStamp = System.currentTimeMillis();
      this.dateTime = du.getTimeStampString();
    }

    public String getX() {
      return x;
    }

    public Object[] getY() {
      return y;
    }

    public long getTimeStamp() {
      return timeStamp;
    }

    public String getDateTime() {
      return dateTime;
    }

  }
}
