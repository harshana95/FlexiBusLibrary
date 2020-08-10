package com.fugex.flexibus.flexibuslibrary.Models;

import java.util.Date;

public class Schedule {

    private String routeName;
    private String busID, ID, conductorID;
    private Date date;
    private boolean isActive;
    private boolean isCompleted;

    public Schedule() {
    }

    public Schedule(String routeName, String busID, String conductorID, Date date) {
        this.routeName = routeName;
        this.busID = busID;
        this.conductorID = conductorID;
        this.date = date;
        this.isActive = false;
        this.isCompleted = false;
    }

    public Date getDate() {
        return date;
    }

    public String getID() {
        return ID;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setActive(final boolean active) {
        isActive = active;
    }

    public void setBusID(final String busID) {
        this.busID = busID;
    }

    public void setCompleted(final boolean completed) {
        isCompleted = completed;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public void setID(final String ID) {
        this.ID = ID;
    }

    public void setRouteName(final String routeName) {
        this.routeName = routeName;
    }

    public void setConductorID(final String conductorID) {
        this.conductorID = conductorID;
    }

    public String getBusID() {
        return busID;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getConductorID() {
        return conductorID;
    }
}
