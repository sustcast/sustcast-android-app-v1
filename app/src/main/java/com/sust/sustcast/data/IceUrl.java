package com.sust.sustcast.data;

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