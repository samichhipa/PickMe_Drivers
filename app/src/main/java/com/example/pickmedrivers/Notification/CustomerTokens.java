package com.example.pickmedrivers.Notification;

public class CustomerTokens {
    String token_id, customer_id;

    public CustomerTokens() {
    }

    public CustomerTokens(String token_id, String customer_id) {
        this.token_id = token_id;
        this.customer_id = customer_id;
    }

    public String getToken_id() {
        return token_id;
    }

    public void setToken_id(String token_id) {
        this.token_id = token_id;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }
}
