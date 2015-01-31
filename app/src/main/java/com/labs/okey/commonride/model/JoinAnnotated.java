package com.labs.okey.commonride.model;

import java.util.Date;

/**
 * Created by Oleg on 28-Jan-15.
 */
public class JoinAnnotated {

    @com.google.gson.annotations.SerializedName("id")
    public String Id;

    @com.google.gson.annotations.SerializedName("passenger_id")
    public String passengerId;

    @com.google.gson.annotations.SerializedName("first_name")
    public String first_name;

    @com.google.gson.annotations.SerializedName("last_name")
    public String last_name;

    @com.google.gson.annotations.SerializedName("picture_url")
    public String picture_url;

    //@com.google.gson.annotations.SerializedName("ride_id")
    public String ride_id;

    @com.google.gson.annotations.SerializedName("when_joined")
    public Date whenJoined;

    @com.google.gson.annotations.SerializedName("email")
    public String email;
}
