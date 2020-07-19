package com.example.pickmedrivers.Notification;

public class DriverTokens {

    String token_id,driver_id;

    public DriverTokens(String token_id, String driver_id) {
        this.token_id = token_id;
        this.driver_id = driver_id;
    }

    public String getToken_id() {
        return token_id;
    }

    public void setToken_id(String token_id) {
        this.token_id = token_id;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }
}
