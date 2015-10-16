package pl.rcponline.nfc.model;

import java.io.Serializable;

public class Employee implements Serializable {

    public static final String TAG = "Employee";
    private static final long serialVersionUID = -7406082437623008161L;

    private long id;
    private String firstname;
    private String name;
    private int permission;

    public Employee(){}

    public Employee(long id, String firstname, String name, int permission) {
        this.id = id;
        this.firstname = firstname;
        this.name = name;
        this.permission = permission;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }
}
