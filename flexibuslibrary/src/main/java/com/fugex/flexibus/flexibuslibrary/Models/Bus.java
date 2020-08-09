package com.fugex.flexibus.flexibuslibrary.Models;

public class Bus {

    public enum Type {NORMAL, AC, SEMI_LUX, LUX}

    private String ID;
    private String busName, agencyName, plateNumber;
    private String routeName;
    private String startCity;
    private String destinationCity;
    private int seats;
    private String seatLayout;
    private int seatReservationCost;
    private String busType;

    public Bus() {
    }

    public Bus(String busName, String agencyName, String plateNumber, String routeName,
            String startCity, String destinationCity, String seatLayout, int seatReservationCost, String busType) {
        this.busName = busName;
        this.agencyName = agencyName;
        this.plateNumber = plateNumber;
        this.routeName = routeName;
        this.seatLayout = seatLayout;
        this.seatReservationCost = seatReservationCost;
        this.startCity = startCity;
        this.destinationCity = destinationCity;
        this.busType = busType;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public String getBusType() {
        return busType;
    }


    public void setAgencyName(final String agencyName) {
        this.agencyName = agencyName;
    }

    public String getID() {
        return ID;
    }

    public String getBusName() {
        return busName;
    }

    public String getRouteName() {
        return routeName;
    }

    public int getSeatReservationCost() {
        return seatReservationCost;
    }

    public String getStartCity() {
        return startCity;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public int getSeats() {
        return seats;
    }

    public String getSeatLayout() {
        return seatLayout;
    }

    public void setBusName(final String busName) {
        this.busName = busName;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setBusType(final String busType) {
        this.busType = busType;
    }

    public void setID(final String ID) {
        this.ID = ID;
    }

    public void setPlateNumber(final String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public void setSeatReservationCost(final int seatReservationCost) {
        this.seatReservationCost = seatReservationCost;
    }

    public void setSeats(final int seats) {
        this.seats = seats;
    }
}
