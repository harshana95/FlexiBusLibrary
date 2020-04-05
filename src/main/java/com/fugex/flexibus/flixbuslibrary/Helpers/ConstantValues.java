package com.fugex.flexibus.flixbuslibrary.Helpers;

public enum ConstantValues {

    PERSON_COUNT("person-count", 0),
    BUS_ID("bus-id", 0),
    JOURNEY_ID("journey-id", 0),
    JOURNEY_START("journey-start", 0),
    JOURNEY_END("journey-end", 0),
    YEAR("year", 0),
    MONTH("month", 0),
    DAY("day", 0),
    ROUTE("route", 0),
    BOARDING_POINT("boarding-point", -1),
    DROPPING_POINT("dropping-point", -1),
    TIMEOUT_ONHOLD_SEATS("timeout-onhold", 15),
    SCHEDULE_START_TIME_OFFSET("start-offset for filter", 15),
    SCHEDULE_ACTIVATE_OFFSET("activate-offset", 45),
    BASE_FARE("base-fare", 0);


    private String name;
    private int value;

    ConstantValues(final String name, final int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }
}
