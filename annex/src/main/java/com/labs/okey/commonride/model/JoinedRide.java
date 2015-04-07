package com.labs.okey.commonride.model;

import java.util.Date;

/**
 * Created by Oleg on 07-Apr-15.
 */
public class JoinedRide {
    @com.google.gson.annotations.SerializedName("id")
    public String Id;

    @com.google.gson.annotations.SerializedName("ride_id")
    public String rideId;

    @com.google.gson.annotations.SerializedName("when_joined")
    public Date whenJoined;

    @com.google.gson.annotations.SerializedName("status")
    public String status;

    @com.google.gson.annotations.SerializedName("ride_from")
    public String ride_from;

    @com.google.gson.annotations.SerializedName("ride_to")
    public String ride_to;
}
