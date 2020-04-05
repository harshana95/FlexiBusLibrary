package com.fugex.flexibus.flixbuslibrary.Models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class MyLocation {

    private String ID;
    private double longitude, latitude;
    private @ServerTimestamp
    Date timeStamp;

    public MyLocation() {
    }

    public MyLocation(String ID, double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.ID = ID;
    }

    public String getID() {
        return ID;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setID(final String ID) {
        this.ID = ID;
    }

    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }

    public void setTimeStamp(final Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
