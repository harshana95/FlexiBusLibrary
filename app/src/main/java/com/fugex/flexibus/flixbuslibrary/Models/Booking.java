package com.fugex.flexibus.flixbuslibrary.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Booking {

    // TODO make enum?!
    public static final int SEAT_SELECTED = -99;
    public static final int SEAT_AVAILABLE = 0;
    public static final int SEAT_ONHOLD = 1;
    public static final int SEAT_RESERVED_TMP = 2;
    public static final int SEAT_RESERVED = 3;
    public static final int SEAT_BOOKED = 4;

    private String ID;
    private String busID;
    private String userID;
    private String personID;
    private String scheduleID;
    private Date scheduleDate;
    private int journeyStart, journeyEnd;
    private int boarding, dropping;
    private String seat;
    private int seatState;
    private boolean isCheckedIn;
    private @ServerTimestamp
    Date timeStamp;
    private Date expireTime;

    public Booking() {
    }

    public Booking(String busID, String scheduleID, String userID,
            Date scheduleDate, int journeyStart, int journeyEnd, int boarding, int dropping, String seat,
            int seatState) {
        this.busID = busID;
        this.userID = userID;
        this.scheduleID = scheduleID;
        this.scheduleDate = scheduleDate;
        this.journeyStart = journeyStart;
        this.journeyEnd = journeyEnd;
        this.boarding = boarding;
        this.dropping = dropping;
        this.seat = seat;
        this.seatState = seatState;
        this.isCheckedIn = false;
    }

    public int getBoarding() {
        return boarding;
    }

    public int getDropping() {
        return dropping;
    }

    public String getPersonID() {
        return personID;
    }

    public Date getScheduleDate() {
        return scheduleDate;
    }

    public String getScheduleID() {
        return scheduleID;
    }

    public boolean getIsCheckedIn() {
        return isCheckedIn;
    }

    public void setBoarding(final int boarding) {
        this.boarding = boarding;
    }

    public void setIsCheckedIn(final boolean checkedIn) {
        isCheckedIn = checkedIn;
    }

    public void setDropping(final int dropping) {
        this.dropping = dropping;
    }

    public void setPersonID(final String personID) {
        this.personID = personID;
    }

    public void setScheduleDate(final Date scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public void setScheduleID(final String scheduleID) {
        this.scheduleID = scheduleID;
    }

    public boolean validateBooking() {
        return Timestamp.now().compareTo(new Timestamp(expireTime)) < 0;

    }


    public String getBusID() {
        return busID;
    }

    public String getID() {
        return ID;
    }

    public int getJourneyStart() {
        return journeyStart;
    }

    public int getJourneyEnd() {
        return journeyEnd;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public String getSeat() {
        return seat;
    }

    public int getSeatState() {
        return seatState;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public String getUserID() {
        return userID;
    }

    public void setID(final String ID) {
        this.ID = ID;
    }

    public void setJourneyStart(final int journeyStart) {
        this.journeyStart = journeyStart;
    }

    public void setJourneyEnd(final int journeyEnd) {
        this.journeyEnd = journeyEnd;
    }

    public void setExpireTime(final Date expireTime) {
        this.expireTime = expireTime;
    }

    public void setSeat(final String seat) {
        this.seat = seat;
    }

    public void setUserID(final String userID) {
        this.userID = userID;
    }

    public void setSeatState(final int seatState) {
        this.seatState = seatState;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setBusID(final String busID) {
        this.busID = busID;
    }


}
