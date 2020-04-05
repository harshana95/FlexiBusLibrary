package com.fugex.flexibus.flexibuslibrary.Models;

public class PersonDetails {

    private String bookingID, userID, transactionID, ID, fname, lname;
    private String age;

    public PersonDetails() {
    }

    public PersonDetails(String userID, String fname, String lname, final String age) {
        this.userID = userID;
        this.fname = fname;
        this.lname = lname;
        this.age = age;
    }

    public String getAge() {
        return age;
    }


    public String getBookingID() {
        return bookingID;
    }

    public String getFname() {
        return fname;
    }

    public String getID() {
        return ID;
    }

    public String getLname() {
        return lname;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public String getUserID() {
        return userID;
    }

    public void setAge(final String age) {
        this.age = age;
    }


    public void setBookingID(final String bookingID) {
        this.bookingID = bookingID;
    }

    public void setFname(final String fname) {
        this.fname = fname;
    }

    public void setID(final String ID) {
        this.ID = ID;
    }

    public void setLname(final String lname) {
        this.lname = lname;
    }

    public void setTransactionID(final String transactionID) {
        this.transactionID = transactionID;
    }

    public void setUserID(final String userID) {
        this.userID = userID;
    }
}
