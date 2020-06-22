package x.rest.data.devices;

public class Record {

  private String id = "";
  private String dimension = "";
  private String legend = "";
  private String values = "";
  private String date = "";

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDimension() {
    return dimension;
  }

  public void setDimension(String dimension) {
    this.dimension = dimension;
  }

  public String getLegend() {
    return legend;
  }

  public void setLegend(String legend) {
    this.legend = legend;
  }

  public String getValues() {
    return values;
  }

  public void setValues(String values) {
    this.values = values;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

}
