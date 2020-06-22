package x.websocket.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;

public class AsyncStatusMessage {

  private int error = 0;
  private int kind = 0;
  private String id = "";
  private String value = "";

  public static int ERROR = 1;
  public static int NOERROR = 0;
  public static int INFO = 0;
  public static int STATUS = 1;
  public static int SET = 2;
  public static int INIT = 3;
  public static int MULTISET = 4;

  private final ArrayList<AsyncStatusMessage> asm = new ArrayList<>();

  public AsyncStatusMessage() {

  }

  public AsyncStatusMessage(int error, int kind, String id, String value) {
    this.error = error;
    this.kind = kind;
    this.id = id;
    this.value = value;
  }

  public int getError() {
    return error;
  }

  public void setError(int error) {
    this.error = error;
  }

  public int getKind() {
    return kind;
  }

  public void setKind(int kind) {
    this.kind = kind;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
  public ArrayList<AsyncStatusMessage> getAsm() {
    return asm;
  }

}
