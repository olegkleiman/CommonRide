package com.labs.okey.commonride.model;

import java.util.Date;

/**
 * Created by Oleg on 23-Jan-15.
 */
public class Join {
    @com.google.gson.annotations.SerializedName("id")
    public String Id;

    @com.google.gson.annotations.SerializedName("ride_id")
    public String rideId;

    @com.google.gson.annotations.SerializedName("passenger_id")
    private String passengerId;
    public String getPassengerId() {
        return passengerId;
    }

    @com.google.gson.annotations.SerializedName("when_joined")
    public Date whenJoined;

}
