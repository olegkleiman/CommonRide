package com.labs.okey.commonride.model;

import java.util.Date;

/**
 * Created by Oleg on 21-Jan-15.
 */
public class Ride {

    @com.google.gson.annotations.SerializedName("id")
    public String Id;

    @com.google.gson.annotations.SerializedName("user_driver")
    private String driver;
    public String getDriver(){
        return driver;
    }

    @com.google.gson.annotations.SerializedName("from")
    public String from;

    @com.google.gson.annotations.SerializedName("from_lat")
    public String from_lat;

    @com.google.gson.annotations.SerializedName("from_lon")
    public String from_lon;

    @com.google.gson.annotations.SerializedName("to")
    public String to;

    @com.google.gson.annotations.SerializedName("to_lat")
    public String to_lat;

    @com.google.gson.annotations.SerializedName("to_lon")
    public String to_lon;

    @com.google.gson.annotations.SerializedName("free_places")
    public int freePlaces;

    @com.google.gson.annotations.SerializedName("when_published")
    public Date whenPublished;

    @com.google.gson.annotations.SerializedName("when_starts")
    public Date whenStarts;

    @com.google.gson.annotations.SerializedName("notes")
    public String notes;
}
