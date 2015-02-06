package com.labs.okey.commonride.model;

/**
 * Created by c1306948 on 27/01/2015.
 */
public class User {

    @com.google.gson.annotations.SerializedName("id")
    public String Id;

    @com.google.gson.annotations.SerializedName("first_name")
    private String first_name;
    public String getFirstName() {
        return first_name;
    }
    public void setFirstName(String value) {
        first_name = value;
    }

    @com.google.gson.annotations.SerializedName("last_name")
    private String last_name;
    public String getLastName() {
        return last_name;
    }
    public void setLastName(String value){
        last_name = value;
    }

    @com.google.gson.annotations.SerializedName("registration_id")
    public String registration_id;

    @com.google.gson.annotations.SerializedName("picture_url")
    public String picture_url;

    @com.google.gson.annotations.SerializedName("email")
    public String email;

    @com.google.gson.annotations.SerializedName("phone")
    public String phone;

    @com.google.gson.annotations.SerializedName("use_phone")
    public Boolean usePhone;

    @com.google.gson.annotations.SerializedName("group")
    public String group;

}
