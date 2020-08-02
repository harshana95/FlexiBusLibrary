package com.fugex.flexibus.flexibuslibrary.Models;

import java.util.ArrayList;

public class City {

    private String name;
    private ArrayList<String> routes;


    public String getName() {
        return name;
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
