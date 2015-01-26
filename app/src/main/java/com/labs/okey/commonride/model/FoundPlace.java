package com.labs.okey.commonride.model;

/**
 * Created by Oleg on 26-Jan-15.
 */
public class FoundPlace {

    private String Description;
    @Override
    public String toString(){
        return Description;
    }

    public void setDescription(String description){
        Description = description;
    }
    public String getDescription() {
        return Description;
    }

    private String PlaceID;
    public void setPlaceID(String placeID){
        PlaceID = placeID;
    }
    public String getPlaceID(){
        return PlaceID;    }
}
