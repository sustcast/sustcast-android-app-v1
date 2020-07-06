package com.sust.sustcast;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class IceUrl {

    private int limit;
    private int numlistener;
    private String url;

    public IceUrl(int limit, int numlistener, String url) {
        this.limit = limit;
        this.numlistener = numlistener;
        this.url = url;
    }

    public IceUrl() {
    }

    public int getLimit() {
        return limit;
    }

    public int getNumlistener() {
        return numlistener;
    }

    public String getUrl() {
        return url;
    }

}