package com.fugex.flexibus.flexibuslibrary.Helpers;

import com.fugex.flexibus.flexibuslibrary.Models.*;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

//TODO implement locally loading of data
public class ContentHolder {

    private static DatabaseHandler mDatabaseHandler = DatabaseHandler.getInstance();

    // bookings holders
    public static ArrayList<Booking> bookedBookings = new ArrayList<>();
    public static ArrayList<Booking> onHoldBookings = new ArrayList<>();
    public static ArrayList<Booking> tmpReservedBookings = new ArrayList<>();
    public static ArrayList<Booking> reservedBookings = new ArrayList<>();
    public static HashSet<String> selectedSeats = new HashSet<>();

    private static HashMap<String, City> citiesMap = new HashMap<>();
    private static HashMap<String, Bus> busesMap = new HashMap<>();
    private static Date setTimeCitiesMap = new Date();
    private static Date setTimeBusesMap = new Date();

    public static ArrayList<Bus> getBusesList(List<String> busIDs) {
        if (compareDate(setTimeBusesMap)) {
            return null;
        }
        ArrayList<Bus> buses = new ArrayList<>();
        for (String busID : busIDs) {
            if (busesMap.containsKey(busID)) {
                buses.add(busesMap.get(busID));
            } else {
                return null;
            }
        }
        return buses;
    }
    // UPDATE BUSES ------------------------------------------------------------------------------------------------

    private static boolean compareDate(Date compareToDate) {
        long offset = TimeUnit.MINUTES.toMillis(10);
        Timestamp now = Timestamp.now();
        return (now.toDate().getTime() - compareToDate.getTime()) > offset;
    }
    // UPDATE CITIES -----------------------------------------------------------------------------------------------

    public static ArrayList<Bus> getBusesList() {
        if (compareDate(setTimeBusesMap)) {
            return new ArrayList<>(busesMap.values());
        } else {
            return null;
        }
    }
    // GET BUSES ---------------------------------------------------------------------------------------------------

    public static ArrayList<City> getCitiesList(List<String> cityNames) {
        if (compareDate(setTimeCitiesMap)) {
            return null;
        }
        ArrayList<City> cities = new ArrayList<>();
        for (String cityname : cityNames) {
            if (citiesMap.containsKey(cityname)) {
                cities.add(citiesMap.get(cityname));
            } else {
                return null;
            }
        }
        return cities;

    }

    public static ArrayList<City> getCitiesList() {
        if (compareDate(setTimeCitiesMap)) {
            return new ArrayList<>(citiesMap.values());
        } else {
            return null;
        }
    }
    // GET CITIES ---------------------------------------------------------------------------------------------------

    public static void updateBusesList(ArrayList<Bus> list) {
        for (Bus bus : list) {
            busesMap.put(bus.getID(), bus);
        }
        setTimeBusesMap = Timestamp.now().toDate();
    }

    public static void updateCitiesList(ArrayList<City> list) {
        for (City city : list) {
            citiesMap.put(city.getName(), city);
        }
        setTimeCitiesMap = Timestamp.now().toDate();
    }
    // BOOKING -----------------------------

    public static void deleteOnHoldSeats() {
        for (Booking booking : onHoldBookings) {
            mDatabaseHandler.mBookingDatabaseHandler.deleteBooking(booking);
        }
        selectedSeats.clear();
        onHoldBookings.clear();
    }

    public static void deleteTemporallyReservedSeats() {
        for (Booking booking : ContentHolder.tmpReservedBookings) {
            mDatabaseHandler.mBookingDatabaseHandler.deleteBooking(booking);
        }
        ContentHolder.tmpReservedBookings.clear();
    }

    public static void deleteBookedSeats() {
        for (Booking booking : bookedBookings) {
            mDatabaseHandler.mBookingDatabaseHandler.deleteBooking(booking);
        }
        bookedBookings.clear();
    }


    public static void deleteReservedSeats() {
        for (Booking booking : reservedBookings) {
            mDatabaseHandler.mBookingDatabaseHandler.deleteBooking(booking);
        }
        reservedBookings.clear();
    }
}
