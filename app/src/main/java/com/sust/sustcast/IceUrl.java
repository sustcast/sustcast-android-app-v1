package com.sust.sustcast;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class IceUrl {

    private int limit;
    private int numlisteners;
    private String url;

    public IceUrl(int limit, int numlisteners, String url) {
        this.limit = limit;
        this.numlisteners = numlisteners;
        this.url = url;
    }

    public IceUrl() {
    }

//    // Default constructor required for calls to
//    // DataSnapshot.getValue(User.class)
//    public IceUrl(Object value) {
//    }

    public int getLimit() {
        return limit;
    }

    public int getNumlisteners() {
        return numlisteners;
    }

    public String getUrl() {
        return url;
    }
}