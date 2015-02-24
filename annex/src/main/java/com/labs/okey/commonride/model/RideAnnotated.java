package com.labs.okey.commonride.model;

import java.util.Date;

/**
 * Created by c1306948 on 27/01/2015.
 */
public class RideAnnotated { //extends Ride {

    @com.google.gson.annotations.SerializedName("id")
    public String Id;

    @com.google.gson.annotations.SerializedName("__version")
    public String version;

    @com.google.gson.annotations.SerializedName("ride_from")
    public String ride_from;

    @com.google.gson.annotations.SerializedName("ride_to")
    public String ride_to;

    @com.google.gson.annotations.SerializedName("free_places")
    public int freePlaces;

    @com.google.gson.annotations.SerializedName("when_starts")
    public Date whenStarts;

    @com.google.gson.annotations.SerializedName("isanonymous")
    public Boolean isAnonymous;

    @com.google.gson.annotations.SerializedName("first_name")
    public String first_name;

    @com.google.gson.annotations.SerializedName("last_name")
    public String last_name;

    @com.google.gson.annotations.SerializedName("picture_url")
    public String picture_url;

    @com.google.gson.annotations.SerializedName("email")
    public String email;

    @com.google.gson.annotations.SerializedName("driver_id")
    public String driverId;

}
