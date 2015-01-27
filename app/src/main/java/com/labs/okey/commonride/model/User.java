package com.labs.okey.commonride.model;

/**
 * Created by c1306948 on 27/01/2015.
 */
public class User {

    @com.google.gson.annotations.SerializedName("id")
    public String Id;

    @com.google.gson.annotations.SerializedName("first_name")
    public String first_name;

    @com.google.gson.annotations.SerializedName("last_name")
    public String last_name;

    @com.google.gson.annotations.SerializedName("registration_id")
    public String registration_id;

    @com.google.gson.annotations.SerializedName("picture_url")
    public String picture_url;

    @com.google.gson.annotations.SerializedName("email")
    public String email;
}
