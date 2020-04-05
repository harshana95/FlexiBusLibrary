package com.fugex.flexibus.flixbuslibrary.Models;

public class Seat {

    public String seatID;
    public String seatUserID;
    public int seatState;

    public Seat(String seatID, String seatUserID, int seatState) {
        this.seatID = seatID;
        this.seatUserID = seatUserID;
        this.seatState = seatState;
    }

}
