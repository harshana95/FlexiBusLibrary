package com.fugex.flexibus.flixbuslibrary.Models;

import java.util.ArrayList;

public class City {

    private String name;
    private ArrayList<String> routes;
    private ArrayList<String> busStopsInCity;

    public ArrayList<String> getBusStopsInCity() {
        return busStopsInCity;
    }

    public String getName() {
        return name;
    }

    public void setBusStopsInCity(final ArrayList<String> busStopsInCity) {
        this.busStopsInCity = busStopsInCity;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public ArrayList<String> getRoutes() {
        return routes;
    }

    public void setRoutes(final ArrayList<String> routes) {
        this.routes = routes;
    }
}
