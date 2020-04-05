package com.fugex.flexibus.flixbuslibrary.Models;

import android.util.Log;
import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Transaction implements Serializable {

    //todo add timstamp? check bus expired?
    private double total;
    private double transactionFee;
    private double baseFare;
    private double seatReservationCost;
    private String busID, userID, scheduleID, ID;
    private String email, contact;
    private ArrayList<String> bookingIds, personIds;
    private int journeyStart = -1, journeyEnd = -1;
    private int boarding = -1, dropping = -1;
    private boolean isCheckedIn;
    private Date scheduleDate;
    private @ServerTimestamp
    Date timestamp;

    public Transaction() {
    }

    public Transaction(String userID, Schedule schedule, String email, String contact, Bus bus, double baseFare,
            double transactionFee) { // todo add other fees
        if (!schedule.getBusID().equals(bus.getID())) {
            throw new AssertionError();
        }
        this.scheduleDate = schedule.getDate();
        this.isCheckedIn = false;
        this.userID = userID;
        this.scheduleID = schedule.getID();
        this.email = email;
        this.contact = contact;
        bookingIds = new ArrayList<>();
        personIds = new ArrayList<>();
        this.busID = bus.getID();
        this.seatReservationCost = bus.getSeatReservationCost();
        this.baseFare = baseFare;
        this.transactionFee = transactionFee;
        total = 0;
        total += transactionFee;
    }

    public double addTicket(Booking booking, PersonDetails personDetails) {
        if (getBoarding() == -1 || getDropping() == -1 || getJourneyStart() == -1 || getJourneyEnd() == -1) {
            setBoarding(booking.getBoarding());
            setDropping(booking.getDropping());
            setJourneyStart(booking.getJourneyStart());
            setJourneyEnd(booking.getJourneyEnd());
        } else if (getBoarding() != booking.getBoarding() || getDropping() != booking.getDropping() ||
                getJourneyStart() != booking.getJourneyStart() || getJourneyEnd() != booking.getJourneyEnd()) {
            Log.e("Transaction", "Wrong booking added to transaction");
            return 0;
        }
        bookingIds.add(booking.getID());
        personIds.add(personDetails.getID());
        double ticketFare = total;
        // add ticket price
        total += baseFare;
        if (booking.getSeatState() == Booking.SEAT_BOOKED) {
            total += seatReservationCost;
        }
        ticketFare = total - ticketFare;
        return ticketFare;
    }

    public int getBoarding() {
        return boarding;
    }

    public String getContact() {
        return contact;
    }

    public int getDropping() {
        return dropping;
    }

    public String getEmail() {
        return email;
    }

    public String getID() {
        return ID;
    }

    public int getJourneyEnd() {
        return journeyEnd;
    }

    public int getJourneyStart() {
        return journeyStart;
    }

    public Date getScheduleDate() {
        return scheduleDate;
    }

    public String getScheduleID() {
        return scheduleID;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public boolean isCheckedIn() {
        return isCheckedIn;
    }

    public void setBoarding(final int boarding) {
        this.boarding = boarding;
    }

    public void setCheckedIn(final boolean checkedIn) {
        isCheckedIn = checkedIn;
    }

    public void setContact(final String contact) {
        this.contact = contact;
    }

    public void setDropping(final int dropping) {
        this.dropping = dropping;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setJourneyEnd(final int journeyEnd) {
        this.journeyEnd = journeyEnd;
    }

    public void setJourneyStart(final int journeyStart) {
        this.journeyStart = journeyStart;
    }

    public void setScheduleDate(final Date scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public void setScheduleID(final String scheduleID) {
        this.scheduleID = scheduleID;
    }

    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }


    public long totalStripeInLong() {
        return (long) (total) * 100;
    }

    public ArrayList<String> getBookingIds() {
        return bookingIds;
    }

    public ArrayList<String> getPersonIds() {
        return personIds;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getSeatReservationCost() {
        return seatReservationCost;
    }

    public String getBusID() {
        return busID;
    }

    public double getTotal() {
        return total;
    }

    public double getTransactionFee() {
        return transactionFee;
    }

    public String getUserID() {
        return userID;
    }

    public void setBusID(final String busID) {
        this.busID = busID;
    }

    public void setID(final String ID) {
        this.ID = ID;
    }

    public void setTotal(final double total) {
        this.total = total;
    }

    public void setTransactionFee(final double transactionFee) {
        this.transactionFee = transactionFee;
    }

    public void setBaseFare(final double baseFare) {
        this.baseFare = baseFare;
    }

    public void setBookingIds(final ArrayList<String> bookingIds) {
        this.bookingIds = bookingIds;
    }

    public void setPersonIds(final ArrayList<String> personIds) {
        this.personIds = personIds;
    }

    public void setSeatReservationCost(final double seatReservationCost) {
        this.seatReservationCost = seatReservationCost;
    }

    public void setUserID(final String userID) {
        this.userID = userID;
    }
}
