package com.example.pickmedrivers.Model;

public class Drivers {

    String id,name,password,driver_phone,driver_email,car_type;


    public Drivers() {
    }

    public Drivers(String id, String name, String password, String driver_phone, String driver_email, String car_type) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.driver_phone = driver_phone;
        this.driver_email = driver_email;
        this.car_type = car_type;
    }

    public String getCar_type() {
        return car_type;
    }

    public void setCar_type(String car_type) {
        this.car_type = car_type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriver_phone() {
        return driver_phone;
    }

    public void setDriver_phone(String driver_phone) {
        this.driver_phone = driver_phone;
    }

    public String getDriver_email() {
        return driver_email;
    }

    public void setDriver_email(String driver_email) {
        this.driver_email = driver_email;
    }
}
