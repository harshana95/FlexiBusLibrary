package com.fugex.flexibus.flixbuslibrary.Models;

import java.util.ArrayList;

public class Route {

    //TODO add timeTable for each stop
    private String routeName;
    private ArrayList<String> stops, time;

    public Route() {
    }

    public String getRouteName() {
        return routeName;
    }

    public ArrayList<String> getTime() {
        return time;
    }

    public void setRouteName(final String routeName) {
        this.routeName = routeName;
    }

    public ArrayList<String> getStops() {
        return stops;
    }

    public void setStops(final ArrayList<String> cities) {
        this.stops = cities;
    }

    public void setTime(final ArrayList<String> time) {
        this.time = time;
    }
}
