package com.sust.sustcast.data;

import java.io.Serializable;

public class User implements Serializable {
    public String uid;
    public String userName;
    public String emailAddress;
    public String phoneNumber;
    public String department;
    private boolean isAuthenticated;

    public User() {
    }

    public User(String userName, String emailAddress, String phoneNumber, String department) {
        this.userName = userName;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.department = department;
    }

    public boolean getAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }
}
