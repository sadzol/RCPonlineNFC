package pl.rcponline.nfc.model;

import java.io.Serializable;

import pl.rcponline.nfc.Const;

public class Event implements Serializable {

    private static final String TAG = "Event";
    private static final long serialVersionUID = -7406082437623008161L;

    long id;
    private int  type, source, status;
    private String identificator, location, comment, datetime, deviceCode;
    private Employee employee;

    public Event() {
    }

    public Event(int type, int source, int status, String identificator, String location, String comment, String datetime, Employee employee, String deviceCode) {
        this.type = type;
        this.source = source;
        this.status = status;
        this.identificator = identificator;
        this.location = location;
        this.comment = comment;
        this.datetime = datetime;
        this.employee = employee;
        this.deviceCode = deviceCode;
    }


    public long getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getTypeName() {
        int TypeIdArr = getType() - 1;
        return Const.EVENT_TYPE[TypeIdArr];
    }

    public int getSource() {
        return source;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getLocation() {
        return location;
    }

    public String getComment() {
        return comment;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public String getIdentificator() {
        return identificator;
    }

    public void setIdentificator(String identificator) {
        this.identificator = identificator;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }
}
