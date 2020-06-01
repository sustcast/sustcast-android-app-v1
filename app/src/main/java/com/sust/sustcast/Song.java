package com.sust.sustcast;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Song {

    public String name;
    public String artist;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public Song() {
    }

    public Song(String name, String email) {
        this.name = name;
        this.artist = email;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }
}