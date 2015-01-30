package com.labs.okey.commonride.model;

import java.util.Date;

/**
 * Created by c1306948 on 27/01/2015.
 */
public class RideAnnotated { //extends Ride {

    @com.google.gson.annotations.SerializedName("id")
    public String Id;

    @com.google.gson.annotations.SerializedName("from")
    public String from;

    @com.google.gson.annotations.SerializedName("to")
    public String to;

    @com.google.gson.annotations.SerializedName("free_places")
    public int freePlaces;

    @com.google.gson.annotations.SerializedName("when_starts")
    public Date whenStarts;

    @com.google.gson.annotations.SerializedName("first_name")
    public String first_name;

    @com.google.gson.annotations.SerializedName("last_name")
    public String last_name;

    @com.google.gson.annotations.SerializedName("picture_url")
    public String picture_url;

    @com.google.gson.annotations.SerializedName("email")
    public String email;
}
