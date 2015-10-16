package pl.rcponline.nfc.model;

import java.io.Serializable;

import pl.rcponline.nfc.model.Employee;

public class Identificator  implements Serializable{

    public static final String TAG = "Identificator";
    private static final long serialVersionUID  = -7406082437623008161L;

    private long id;
    private String number;
    private String desc;
    private Employee employee;

    public Identificator(){}

    public Identificator(String number, String desc){
        this.number = number;
        this.desc = desc;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
