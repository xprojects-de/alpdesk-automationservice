package x.retain.database;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Retainvariables {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private int devicehandle = 0;
  private int propertyhandle = 0;
  private String propertyvalue = "";
  private String propertyname = "";
  private LocalDateTime tstamp;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public int getDevicehandle() {
    return devicehandle;
  }

  public void setDevicehandle(int devicehandle) {
    this.devicehandle = devicehandle;
  }

  public int getPropertyhandle() {
    return propertyhandle;
  }

  public void setPropertyhandle(int propertyhandle) {
    this.propertyhandle = propertyhandle;
  }

  public String getPropertyvalue() {
    return propertyvalue;
  }

  public void setPropertyvalue(String propertyvalue) {
    this.propertyvalue = propertyvalue;
  }

  public String getPropertyname() {
    return propertyname;
  }

  public void setPropertyname(String propertyname) {
    this.propertyname = propertyname;
  }

  public LocalDateTime getTstamp() {
    return tstamp;
  }

  public void setTstamp(LocalDateTime tstamp) {
    this.tstamp = tstamp;
  }

}
