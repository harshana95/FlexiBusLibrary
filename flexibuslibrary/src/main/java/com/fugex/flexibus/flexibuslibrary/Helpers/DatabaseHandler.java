package com.fugex.flexibus.flexibuslibrary.Helpers;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fugex.flexibus.flexibuslibrary.Models.Booking;
import com.fugex.flexibus.flexibuslibrary.Models.Bus;
import com.fugex.flexibus.flexibuslibrary.Models.City;
import com.fugex.flexibus.flexibuslibrary.Models.MyLocation;
import com.fugex.flexibus.flexibuslibrary.Models.PersonDetails;
import com.fugex.flexibus.flexibuslibrary.Models.Route;
import com.fugex.flexibus.flexibuslibrary.Models.Schedule;
import com.fugex.flexibus.flexibuslibrary.Models.Seat;
import com.fugex.flexibus.flexibuslibrary.Models.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class DatabaseHandler {

    private static String TAG = "DatabaseHandler";
    private FirebaseAuth mAuth;
    private FirebaseMessaging mMessaging;
    private DatabaseReference mDatabaseRef;
    private FirebaseFirestore mFireStore;
    private StorageReference mStorageRef;
    private Date mDateTime;

    public BookingDatabaseHandler mBookingDatabaseHandler;
    public BusDatabaseHandler mBusDatabaseHandler;
    public RouteDatabaseHandler mRouteDatabaseHandler;
    public TransactionDatabaseHandler mTransactionDatabaseHandler;
    public CityDatabaseHandler mCityDatabaseHandler;
    public ScheduleDatabaseHandler mScheduleDatabaseHandler;
    public UserFeedBackHandler mUserFeedBackHandler;

    private static DatabaseHandler mDatabaseHandler;

    // Only one thread can execute this at a time
    public static synchronized DatabaseHandler getInstance() {
        if (mDatabaseHandler == null) {
            mDatabaseHandler = new DatabaseHandler();
        }
        return mDatabaseHandler;
    }

    public class ScheduleDatabaseHandler {

        public CollectionReference getScheduleRef() {
            return getFireStore().collection("schedules");
        }

        public void activateSchedule(final Schedule schedule, final OnSuccessCustomListener<Boolean> listener) {
            Log.i(TAG, "Activating schedule started for: " + schedule.getID());
            getScheduleRef().document(schedule.getID()).set(schedule).addOnSuccessListener(
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(final Void aVoid) {
                            Log.i(TAG, "Activating schedule success for: " + schedule.getID());
                            listener.onSuccess(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Activating schedule failed for: " + schedule.getID());
                    Log.e(TAG, e.toString());
                    listener.onFailure();
                }
            });
        }

        public void finishCurrentSchedule(final Schedule schedule, final OnSuccessCustomListener<Boolean> listener) {
            Log.i(TAG, "Finishing current schedule started for schedule: " + schedule.getID());
            schedule.setActive(false);
            schedule.setCompleted(true);
            getScheduleRef().document(schedule.getID()).set(schedule).addOnSuccessListener(
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(final Void aVoid) {
                            Log.i(TAG, "Finishing current schedule success for: " + schedule.getID());
                            listener.onSuccess(true);
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Finishing current schedule failed for : " + schedule.getID());
                    Log.e(TAG, e.toString());
                    listener.onFailure();
                }
            });
        }

        public void getSchedule(final String scheduleID, final OnSuccessCustomListener<Schedule> listener) {
            Log.i(TAG, "Getting schedule started for: " + scheduleID);
            CollectionReference ref = getScheduleRef();
            ref.document(scheduleID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(final DocumentSnapshot documentSnapshot) {
                    Schedule schedule = documentSnapshot.toObject(Schedule.class);
                    Log.i(TAG, "Getting schedule success for: " + schedule.getID());
                    listener.onSuccess(schedule);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Getting schedule failed for: " + scheduleID);
                    Log.e(TAG, e.toString());
                    listener.onFailure();
                }
            });
        }


        public void getScheduleOnGivenPeriodWithRoutes(Date start, Date end, final ArrayList<String> routes,
                                                       final OnSuccessCustomListener<ArrayList<Schedule>> listener) {
            Log.i(TAG, "Getting schedules on " + start + " to " + end + " with routes " + routes.toString());
            if (routes.size() == 0) {
                listener.onSuccess(new ArrayList<Schedule>());
                return;
            }
            CollectionReference ref = getScheduleRef();
            if (Timestamp.now().toDate().after(start)) {
                start = new Date(Timestamp.now().toDate().getTime() +
                        TimeUnit.MINUTES.toMillis(ConstantValues.SCHEDULE_START_TIME_OFFSET.getValue()));
            }
            ref.whereGreaterThan("date", start).whereLessThan("date", end).whereIn("routeName", routes).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(final QuerySnapshot querySnapshot) {
                            ArrayList<Schedule> schedules = (ArrayList<Schedule>) querySnapshot.toObjects(Schedule.class);
                            Log.i(TAG, schedules.size() + " schedules found on given period with routes " + routes.toString());
                            listener.onSuccess(schedules);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Getting schedules on given period with routes " + routes.toString() + " failed");
                    Log.e(TAG, e.toString());
                    listener.onFailure();
                }
            });
        }

        class SortByTime implements Comparator<Schedule> {

            @Override
            public int compare(final Schedule o1, final Schedule o2) {
                long o3 = o1.getDate().getTime();
                return (int) (o1.getDate().getTime() - o2.getDate().getTime());
            }
        }

        public void getCurrentSchedule(final String busID, final OnSuccessCustomListener<Schedule> listener) {
            Log.i(TAG, "Getting current schedule started for: " + busID);
            Calendar date = Calendar.getInstance();
            date.setTime(Timestamp.now().toDate());
            date.add(Calendar.MINUTE, -ConstantValues.SCHEDULE_ACTIVATE_OFFSET.getValue());
            Date startOfDay = date.getTime();
            date.add(Calendar.MINUTE, 2 * ConstantValues.SCHEDULE_ACTIVATE_OFFSET.getValue());
            Date endOfDay = date.getTime();
            getScheduleRef().whereEqualTo("busID", busID).whereEqualTo("completed", false)
                    .whereGreaterThanOrEqualTo("date", startOfDay)
                    .whereLessThanOrEqualTo("date", endOfDay)
                    .addSnapshotListener(
                            new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable final QuerySnapshot queryDocumentSnapshots,
                                                    @Nullable final FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.e(TAG, "Getting current schedule failed fot: " + busID);
                                        Log.e(TAG, e.toString());
                                        listener.onFailure();
                                    } else {
                                        ArrayList<Schedule> loadedSchedules = (ArrayList<Schedule>) queryDocumentSnapshots
                                                .toObjects(Schedule.class);
                                        Log.i(TAG, loadedSchedules.size() + " current schedules found for: " + busID);
                                        if (loadedSchedules.size() != 0) {
                                            Collections.sort(loadedSchedules, new SortByTime());
                                            listener.onSuccess(loadedSchedules.get(0));
                                        } else {
                                            listener.onSuccess(null);
                                        }
                                    }
                                }
                            });

        }

        public void getActiveSchedule(final String busID, final OnSuccessCustomListener<Schedule> listener) {
            Log.i(TAG, "Getting active schedule started for: " + busID);
            getScheduleRef().whereEqualTo("busID", busID).whereEqualTo("active", true)
                    .orderBy("date").limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(final QuerySnapshot querySnapshot) {
                    ArrayList<Schedule> loadedSchedules = (ArrayList<Schedule>) querySnapshot
                            .toObjects(Schedule.class);
                    if (loadedSchedules.size() != 0) {
                        Log.i(TAG, loadedSchedules.size() + "(>1) active schedules found for bus " + busID);
                        if (loadedSchedules.size() > 1) {
                        }
                        listener.onSuccess(loadedSchedules.get(0));
                    } else {
                        listener.onSuccess(null);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Getting active schedule failed for: " + busID);
                    Log.e(TAG, e.toString());
                    listener.onFailure();
                }
            });

        }

        public void getAllSchedulesOnThisDayOfBus(final String busID,
                                                  final OnSuccessCustomListener<ArrayList<Schedule>> listener) {
            Log.i(TAG, "Getting all schedules on this day started for bus: " + busID);
            Calendar date = Calendar.getInstance();
            date.setTime(Timestamp.now().toDate());
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            Date startOfDay = date.getTime();
            date.set(Calendar.HOUR_OF_DAY, 23);
            date.set(Calendar.MINUTE, 59);
            date.set(Calendar.SECOND, 59);
            Date endOfDay = date.getTime();
            Log.i(TAG, "Getting schedules on " + startOfDay + " to " + endOfDay + " with busID " + busID);

            CollectionReference ref = getScheduleRef();
            ref.whereGreaterThan("date", startOfDay).whereLessThan("date", endOfDay).whereEqualTo("busID", busID)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable final QuerySnapshot queryDocumentSnapshots,
                                            @Nullable final FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.e(TAG, "Getting all schedules on this day failed for bus:" + busID);
                                Log.e(TAG, e.toString());
                                listener.onFailure();
                            } else {
                                ArrayList<Schedule> schedules = (ArrayList<Schedule>) queryDocumentSnapshots.toObjects(Schedule.class);
                                Log.i(TAG, schedules.size() + "schedules found on this day for bus:" + busID);
                                listener.onSuccess(schedules);
                            }
                        }
                    });

        }

        public void getAllIncompleteSchedulesOnThisDayOfBus(String busID,
                                                            final OnSuccessCustomListener<ArrayList<Schedule>> listener) {
            Log.i(TAG, "Getting all incomplete schedules on this day started for bus: " + busID);
            Calendar date = Calendar.getInstance();
            date.setTime(Timestamp.now().toDate());
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            Date startOfDay = date.getTime();
            date.set(Calendar.HOUR_OF_DAY, 23);
            date.set(Calendar.MINUTE, 59);
            date.set(Calendar.SECOND, 59);
            Date endOfDay = date.getTime();
            Log.i(TAG, "Getting incomplete schedules on " + startOfDay + " to " + endOfDay + " with busID " + busID);
            getScheduleRef().whereEqualTo("busID", busID).whereEqualTo("completed", false)
                    .whereGreaterThanOrEqualTo("date", startOfDay)
                    .whereLessThanOrEqualTo("date", endOfDay)
                    .addSnapshotListener(
                            new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable final QuerySnapshot queryDocumentSnapshots,
                                                    @Nullable final FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.e(TAG, "Getting all incomplete schedules on this day failed for bus");
                                        Log.e(TAG, e.toString());
                                        listener.onFailure();
                                    } else {
                                        ArrayList<Schedule> loadedSchedules = (ArrayList<Schedule>) queryDocumentSnapshots
                                                .toObjects(Schedule.class);
                                        Log.i(TAG, loadedSchedules.size() + " incomplete schedules found");
                                        if (loadedSchedules.size() != 0) {
                                            Collections.sort(loadedSchedules, new SortByTime());
                                            listener.onSuccess(loadedSchedules);
                                        } else {
                                            listener.onSuccess(null);
                                        }
                                    }
                                }
                            });

        }

        public void getAllSchedulesOfConductorInTime(final String conductorID, Date start, Date end,
                                                     final OnSuccessCustomListener<ArrayList<Schedule>> listener){
            Log.i(TAG, "Getting schedules on " + start + " to " + end + " with conductorID " + conductorID);

            CollectionReference ref = getScheduleRef();
            ref.whereGreaterThan("date", start).whereLessThan("date", end).whereEqualTo("conductorID", conductorID)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable final QuerySnapshot queryDocumentSnapshots,
                                            @Nullable final FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.e(TAG, "Failed getting schedules for conductor:" + conductorID);
                                Log.e(TAG, e.toString());
                                listener.onFailure();
                            } else {
                                ArrayList<Schedule> schedules = (ArrayList<Schedule>) queryDocumentSnapshots.toObjects(Schedule.class);
                                Log.i(TAG, schedules.size() + "schedules found for conductor:" + conductorID);
                                listener.onSuccess(schedules);
                            }
                        }
                    });
        }

        public void addSchedule(Schedule schedule, final OnSuccessCustomListener<Boolean> listener) {
            String scheduleID = getScheduleRef().document().getId();
            Log.i(TAG, "Adding schedule started for schedule: " + scheduleID);
            schedule.setID(scheduleID);
            getScheduleRef().document(scheduleID).set(schedule).addOnSuccessListener(
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(final Void aVoid) {
                            Log.i(TAG, "Adding schedule success");
                            listener.onSuccess(true);
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Adding schedule failed");
                    Log.e(TAG, e.toString());
                    listener.onFailure();
                }
            });
        }

    }

    public class TransactionDatabaseHandler {

        public String initTransactionId() {
            return getFireStore().collection("transaction").document().getId();
        }


        public void getMyTransactions(final String userid,
                                      final OnSuccessCustomListener<ArrayList<Transaction>> listener) {
            Log.i(TAG, "Getting transactions started for user " + userid + " current user " + getAuth().getUid());
            getFireStore().collection("transaction").whereEqualTo("userID", userid).
                    get().addOnSuccessListener(
                    new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(final QuerySnapshot querySnapshot) {
                            ArrayList<Transaction> transactions = (ArrayList<Transaction>) querySnapshot
                                    .toObjects(Transaction.class);
                            Log.i(TAG, transactions.size() + " transactions found for user " + userid + " current user " + getAuth().getUid());
                            listener.onSuccess(transactions);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Getting transactions failed for user " + userid + " current user " + getAuth().getUid());
                    Log.e(TAG, e.toString());
                    listener.onFailure();
                }
            });
        }


        public Task<Void> addTransaction(Transaction transaction) {
            if (transaction.getID() == null) {
                throw new NullPointerException(
                        "Transaction ID is null. Please initialize transaction before booking seats");
            }
            if (transaction.getBookingIds().size() == 0) {
                throw new RuntimeException("Transaction has no booking!");
            }
            Log.i(TAG, "Adding transaction " + transaction.toString());
            return getFireStore().collection("transaction").document(transaction.getID()).set(transaction);
        }


        public void getTransaction(final String transactionID, final OnSuccessCustomListener<Transaction> listener) {
            Log.i(TAG, "Getting transaction with transactionID " + transactionID);
            getFireStore().collection("transaction").whereEqualTo("id", transactionID)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable final QuerySnapshot queryDocumentSnapshots,
                                            @Nullable final FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.e(TAG, "Getting transaction failed for: " + transactionID);
                                Log.e(TAG, e.toString());
                                listener.onFailure();
                            } else {
                                ArrayList<Transaction> transactions = (ArrayList<Transaction>) queryDocumentSnapshots
                                        .toObjects(Transaction.class);
                                Log.i(TAG, transactions.size() + "transactions found for:" + transactionID);
                                listener.onSuccess(transactions.get(0));
                            }
                        }
                    });
        }

        public void getAllTransactions(final ArrayList<Schedule> schedules,
                                       final OnSuccessCustomListener<ArrayList<Transaction>> listener) {
            Log.i(TAG, "Getting all transactions started for all schedules");
            if (schedules != null && !schedules.isEmpty()) {
                ArrayList<String> scheduleIDs = new ArrayList<>();
                for (Schedule schedule : schedules) {
                    scheduleIDs.add(schedule.getID());
                }
                getFireStore().collection("transaction").whereIn("scheduleID", scheduleIDs)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable final QuerySnapshot querySnapshot,
                                                @Nullable final FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.e(TAG, "Getting transaction failed for all schedules");
                                    Log.e(TAG, e.toString());
                                    listener.onFailure();
                                } else {
                                    ArrayList<Transaction> transactions = (ArrayList<Transaction>) querySnapshot
                                            .toObjects(Transaction.class);
                                    Log.i(TAG, transactions.size() + "transactions found for all schedules");
                                    listener.onSuccess(transactions);
                                }
                            }
                        });
            } else {
                listener.onSuccess(new ArrayList<Transaction>());
            }
        }

        public void getTransactions(final Schedule schedule,
                                    final OnSuccessCustomListener<ArrayList<Transaction>> listener) {
            Log.i(TAG, "Getting transactions started for schedule: " + schedule.getID());
            getFireStore().collection("transaction").whereEqualTo("scheduleID", schedule.getID())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable final QuerySnapshot querySnapshot,
                                            @Nullable final FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.e(TAG, "Getting transactions failed for schedule: " + schedule.getID());
                                Log.e(TAG, e.toString());
                                listener.onFailure();
                            } else {
                                ArrayList<Transaction> transactions = (ArrayList<Transaction>) querySnapshot
                                        .toObjects(Transaction.class);
                                Log.i(TAG, transactions.size() + "transactions found for schedule: " + schedule.getID());
                                listener.onSuccess(transactions);
                            }
                        }
                    });
        }

        public void setCheckedIn(Transaction transaction) {
            transaction.setCheckedIn(true);
            getFireStore().collection("transaction").document(transaction.getID()).set(transaction);

        }

    }

    public class BookingDatabaseHandler {

        OnParseSeatsCompleteListener parseSeatsListener;

        public void setCustomEventListener(OnParseSeatsCompleteListener eventListener) {
            parseSeatsListener = eventListener;
        }

        void checkBookingsAfterAdding(final ArrayList<Booking> bookings,
                                      final OnSuccessFailureCustomListener<ArrayList<Booking>> listener) {
            final CollectionReference ref = getFireStore().collection("booking");
            final ArrayList<String> seats = new ArrayList<>();
            final String busID = bookings.get(0).getBusID();
            int seatState = bookings.get(0).getSeatState();
            for (Booking booking : bookings) {
                seats.add(booking.getSeat());
                if (!busID.equals(booking.getBusID()) || seatState != booking.getSeatState()) {
                    Log.e(TAG, "Failed to add set of bookings. BusIDs or seats states do not match");
                    return;
                }
            }
            if (seatState == Booking.SEAT_ONHOLD) {
                ref.whereEqualTo("busID", busID).whereIn("seat", seats).get()
                        .addOnSuccessListener(
                                new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(final QuerySnapshot querySnapshot) {
                                        ArrayList<Booking> bookingsOnDB = (ArrayList<Booking>) querySnapshot
                                                .toObjects(Booking.class);
                                        boolean failed = false;
                                        for (String seat : seats) {
                                            ArrayList<Booking> seatBookings = new ArrayList<>();
                                            for (Booking booking : bookingsOnDB) {
                                                if (booking.getSeat().equals(seat)) {
                                                    seatBookings.add(booking);
                                                }
                                            }
                                            int onHoldSeatCount = 0;
                                            for (Booking b : seatBookings) {
                                                if (b.getSeatState() == Booking.SEAT_ONHOLD) {
                                                    onHoldSeatCount++;
                                                }
                                            }
                                            if (onHoldSeatCount == 1) {
                                                Log.i(TAG, "booking commit successful for seat " + seat);
                                            } else {
                                                Log.e(TAG, "booking commit failed. deleting. found " +
                                                        onHoldSeatCount + " bookings for seat " + seat + " in bus "
                                                        + busID);
                                                for (Booking booking : bookings) {
                                                    if (booking.getSeat().equals(seat)) {
                                                        deleteBooking(booking);
                                                        break;
                                                    }
                                                }
                                                failed = true;
                                            }
                                        }
                                        if (failed) {
                                            listener.onFailure();
                                        } else {
                                            listener.onSuccess(bookings);
                                        }

                                    }
                                });
            } else {
                listener.onSuccess(bookings);
            }
        }

        public void addBookings(final ArrayList<Booking> bookings, long lifetimeInMillis,
                                final OnSuccessFailureCustomListener<ArrayList<Booking>> listener) {
            if (bookings == null || bookings.size() == 0) {
                listener.onSuccess(new ArrayList<Booking>());
                return;
            }
            CollectionReference ref = getFireStore().collection("booking");
            Date now = Timestamp.now().toDate();
            long expire;
            if (lifetimeInMillis < 0) {
                expire = now.getTime() + TimeUnit.DAYS.toMillis(62);
            } else {
                expire = now.getTime() + lifetimeInMillis;
            }
            // Get a new write batch
            WriteBatch batch = getFireStore().batch();
            for (Booking booking : bookings) {
                booking.setExpireTime(new Date(expire));
                if (booking.getID() == null) {
                    String bookingID = ref.document().getId();
                    booking.setID(bookingID);
                }
                final String bookingID = booking.getID();
                batch.set(ref.document(bookingID), booking);

            }
            // Commit the batch
            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    checkBookingsAfterAdding(bookings, listener);
                }
            });
        }

        public Booking addBooking(final Booking booking, long lifetimeInMillis,
                                  final OnSuccessFailureCustomListener<Booking> listener) {
            Date now = Timestamp.now().toDate();
            long expire;
            if (lifetimeInMillis < 0) {
                expire = now.getTime() + TimeUnit.DAYS.toMillis(62);
            } else {
                expire = now.getTime() + lifetimeInMillis;
            }
            booking.setExpireTime(new Date(expire));
            final CollectionReference ref = getFireStore().collection("booking");
            if (booking.getID() == null) {
                String bookingID = ref.document().getId();
                booking.setID(bookingID);
            }
            final String bookingID = booking.getID();
            Log.i(TAG, "Adding booking. " + booking.toString());
            ref.document(bookingID).set(booking).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(final Void aVoid) {
                    ArrayList<Booking> bookings = new ArrayList<>();
                    bookings.add(booking);
                    checkBookingsAfterAdding(bookings, new OnSuccessFailureCustomListener<ArrayList<Booking>>() {
                        @Override
                        public void onSuccess(final ArrayList<Booking> data) {
                            if (listener != null) {
                                listener.onSuccess(data.get(0));
                            }
                        }

                        @Override
                        public void onFailure() {
                            if (listener != null) {
                                listener.onFailure();
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error committing booking", e);
                    if (listener != null) {
                        listener.onFailure();
                    }
                }
            });
            return booking;
        }

        public Booking addBooking(Booking booking, PersonDetails personDetails, long lifetimeInMillis) {
            if (booking.getSeatState() != Booking.SEAT_BOOKED && booking.getSeatState() != Booking.SEAT_RESERVED) {
                throw new RuntimeException("Trying to send personal details to a non BOOKED/RESERVED booking!");
            }
            // TODO check email and transaction id... ++ others
            CollectionReference ref = getFireStore().collection("person");
            final String personID = ref.document().getId();
            personDetails.setID(personID);
            booking.setPersonID(personID);
            // adding booking
            String bookingId = getFireStore().collection("booking").document().getId();
            booking.setID(bookingId);
            addBooking(booking, lifetimeInMillis, null);
            personDetails.setBookingID(bookingId);
            // adding person details
            Log.i(TAG, "Adding person. " + personDetails.toString());
            ref.document(personID).set(personDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(final Void aVoid) {
                    Log.i(TAG, "Person details commit successful");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull final Exception e) {
                    Log.w(TAG, "person details commit failed");
                }
            });
            return booking;
        }

        public void getBooking(String bookingID, final OnSuccessCustomListener<Booking> listener) {
            Log.i(TAG, "Getting booking " + bookingID);
            getFireStore().collection("booking").document(bookingID).get().addOnSuccessListener(
                    new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(final DocumentSnapshot documentSnapshot) {
                            final Booking booking = documentSnapshot.toObject(Booking.class);
                            if (booking == null) {
                                throw new NullPointerException("Booking failed to load");
                            }
                            if (booking.validateBooking()) {
                                listener.onSuccess(booking);
                            } else {
                                deleteBooking(booking).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(final Void aVoid) {
                                        Log.i(TAG, "Deleted booking due to time out " + booking.getID());
                                    }
                                });
                                listener.onSuccess(null);
                            }
                        }
                    });
        }

        public void getBookings(ArrayList<String> bookingIDs,
                                final OnSuccessCustomListener<ArrayList<Booking>> listener) {
            Log.i(TAG, "Getting bookings " + bookingIDs.toString());
            if (bookingIDs.size() == 0) {
                listener.onSuccess(new ArrayList<Booking>());
                return;
            }
            getFireStore().collection("booking").whereIn("id", bookingIDs).get().addOnSuccessListener(
                    new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(final QuerySnapshot querySnapshot) {
                            ArrayList<Booking> bookings = (ArrayList<Booking>) querySnapshot.toObjects(Booking.class);
                            ArrayList<Booking> validBookings = new ArrayList<>();
                            for (final Booking booking : bookings) {
                                if (booking == null) {
                                    throw new NullPointerException("Booking failed to load");
                                }
                                if (booking.validateBooking()) {
                                    validBookings.add(booking);
                                } else {
                                    deleteBooking(booking).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(final Void aVoid) {
                                            Log.i(TAG, "Deleted booking due to time out " + booking.getID());
                                        }
                                    });
                                }
                            }
                            listener.onSuccess(validBookings);
                        }
                    });
        }

        public void getAllBookings(Schedule schedule,
                                   final OnSuccessCustomListener<ArrayList<Booking>> listener) {
            Log.i(TAG, "Retrieving all bookings on bus " + schedule.getBusID() + " at "
                    + schedule.getDate());
            getFireStore().collection("booking").whereEqualTo("scheduleID", schedule.getID())
                    .get().addOnSuccessListener(
                    new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(final QuerySnapshot querySnapshot) {
                            ArrayList<Booking> loadedBookings = (ArrayList<Booking>) querySnapshot
                                    .toObjects(Booking.class);
                            ArrayList<Booking> validBookings = new ArrayList<>();
                            for (final Booking booking : loadedBookings) {
                                if (booking.validateBooking()) {
                                    validBookings.add(booking);
                                } else {
                                    deleteBooking(booking)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(final Void aVoid) {
                                                    Log.i(TAG, "Deleted booking due to time out " + booking.getID());
                                                }
                                            });
                                }
                            }
                            listener.onSuccess(validBookings);
                        }
                    });
        }

        // todo: Add Bus as a parameter and load only bookings of a particular bus
        public void getAllBookingsForDay(final OnSuccessCustomListener<ArrayList<Booking>> listener) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Timestamp.now().toDate());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date startDate = calendar.getTime();
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date endDate = calendar.getTime();
            Log.i(TAG,
                    "Retrieving all bookings for the date from" + startDate.toString() + " to " + endDate.toString());
            getFireStore().collection("booking")
                    .whereGreaterThanOrEqualTo("scheduleDate", startDate)
                    .whereLessThanOrEqualTo("scheduleDate", endDate)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable final QuerySnapshot querySnapshots,
                                            @Nullable final FirebaseFirestoreException e) {
                            ArrayList<Booking> loadedBookings = (ArrayList<Booking>) querySnapshots
                                    .toObjects(Booking.class);
                            ArrayList<Booking> validBookings = new ArrayList<>();
                            for (final Booking booking : loadedBookings) {
                                if (booking.validateBooking()) {
                                    if (booking.getSeatState() >= Booking.SEAT_RESERVED) {
                                        validBookings.add(booking);
                                    }
                                } else {
                                    deleteBooking(booking)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(final Void aVoid) {
                                                    Log.i(TAG, "Deleted booking due to time out " + booking.getID());
                                                }
                                            });
                                }
                            }
                            listener.onSuccess(validBookings);
                        }
                    });
        }

        public BookingDatabaseHandler parseAvailableSeats(final Bus mBus, final Schedule schedule,
                                                          final int mJourneyStart,
                                                          final int mJourneyEnd) {
            final BookingDatabaseHandler task = new BookingDatabaseHandler();
            getAllBookings(schedule,
                    new OnSuccessCustomListener<ArrayList<Booking>>() {
                        @Override
                        public void onFailure() {
                            Log.i(TAG, "Parsing available seats for bus " + mBus + " schedule " + schedule + " failed.");
                        }

                        @Override
                        public void onSuccess(final ArrayList<Booking> dataList) {
                            final HashMap<String, Seat> seatStates = new HashMap<>();
                            for (int i = 1; i <= mBus.getSeats(); i++) {
                                seatStates
                                        .put(String.valueOf(i),
                                                new Seat(String.valueOf(i), "", Booking.SEAT_AVAILABLE));
                            }
                            int start, end, tmpStart, tmpEnd;
                            if (mJourneyEnd > mJourneyStart) {
                                tmpEnd = mJourneyEnd;
                                tmpStart = mJourneyStart;
                            } else {
                                tmpEnd = mJourneyStart;
                                tmpStart = mJourneyEnd;
                            }
                            for (Booking booking : dataList) {
                                if (booking == null) {
                                    continue;
                                }
                                start = booking.getJourneyStart();
                                end = booking.getJourneyEnd();
                                if (start > end) {
                                    int tmp = start;
                                    start = end;
                                    end = tmp;
                                }
                                if (!(start >= tmpEnd || tmpStart >= end)) { // seat un available
                                    seatStates.put(booking.getSeat(), new Seat(booking.getSeat(), booking.getUserID(),
                                            booking.getSeatState()));
                                }
                            }
                            task.parseSeatsListener.onSeatStatesAquired(seatStates, dataList);

                        }
                    });
            return task;
        }

        public Task<Void> deleteBooking(final Booking booking) {
            if (booking.getPersonID() != null) {
                getFireStore().collection("person").document(booking.getPersonID()).delete();
            }
            return getFireStore().collection("booking").document(booking.getID()).delete();

        }

        public void getMyBookings(final String userid, final OnSuccessCustomListener<ArrayList<Booking>> listener) {
            Log.i(TAG, "Retrieving bookings of user " + userid);
            getFireStore().collection("booking").whereEqualTo("userid", userid).
                    get().addOnSuccessListener(
                    new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(final QuerySnapshot querySnapshot) {
                            ArrayList<Booking> loadedBookings = (ArrayList<Booking>) querySnapshot
                                    .toObjects(Booking.class);
                            ArrayList<Booking> validBookings = new ArrayList<>();
                            for (final Booking booking : loadedBookings) {
                                if (booking.validateBooking()) {
                                    validBookings.add(booking);
                                } else {
                                    deleteBooking(booking)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(final Void aVoid) {
                                                    Log.i(TAG, "Deleted booking due to time out " + booking.getID());
                                                }
                                            });
                                }
                            }
                            Log.i(TAG, "Valid bookings found for " + userid + " are " + validBookings.size());
                            listener.onSuccess(validBookings);
                        }
                    });
        }

        public void getPersonDetails(final Booking booking, final OnSuccessCustomListener<PersonDetails> listener) {
            Log.i(TAG, "Getting person details of booking " + booking.toString());
            CollectionReference ref = getFireStore().collection("person");
            ref.document(booking.getPersonID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(final DocumentSnapshot documentSnapshot) {
                    listener.onSuccess(documentSnapshot.toObject(PersonDetails.class));
                }
            });
        }

        public void getPersonDetails(final ArrayList<Booking> bookings,
                                     final OnSuccessCustomListener<ArrayList<PersonDetails>> listener) {
            Log.i(TAG, "Getting person details of bookings " + bookings.toString());
            CollectionReference ref = getFireStore().collection("person");
            ArrayList<String> personalIDs = new ArrayList<>();
            for (Booking booking : bookings) {
                personalIDs.add(booking.getPersonID());
            }
            if (personalIDs.size() == 0) { // probably because no internet!
                listener.onSuccess(new ArrayList<PersonDetails>());
                return;
            }
            ref.whereIn("id", personalIDs).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(final QuerySnapshot querySnapshot) {
                    listener.onSuccess((ArrayList<PersonDetails>) querySnapshot.toObjects(PersonDetails.class));
                }
            });
        }

        public void setBookingsCheckedIn(ArrayList<Booking> bookings) {
            for (Booking booking : bookings) {
                booking.setIsCheckedIn(true);
                getFireStore().collection("booking").document(booking.getID()).set(booking);
            }
        }
    }

    public class RouteDatabaseHandler {

        OnSuccessCustomListener<Double> successListener;

        public CollectionReference getRouteRefTo() {
            return getFireStore().collection("routes");
        }

        public void getRoute(String route, final OnSuccessCustomListener<Route> listener) {
            Log.i(TAG, "Getting route " + route);
            route = Functions.getRouteFromEncodedRoute(route);
            getRouteRefTo().whereEqualTo("routeName", route).get().addOnSuccessListener(
                    new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(final QuerySnapshot querySnapshot) {
                            listener.onSuccess(querySnapshot.toObjects(Route.class).get(0));
                        }
                    });
        }

        public void getRoutes(ArrayList<String> routes, final OnSuccessCustomListener<ArrayList<Route>> listener) {
            Log.i(TAG, "Getting routes " + routes.toString());
            if (routes.size() == 0) {
                listener.onSuccess(new ArrayList<Route>());
                return;
            }
            getRouteRefTo().whereIn("routeName", routes).get().addOnSuccessListener(
                    new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(final QuerySnapshot querySnapshot) {
                            listener.onSuccess((ArrayList<Route>) querySnapshot.toObjects(Route.class));
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    listener.onFailure();
                }
            });
        }

        public String addRoute(final Route route, final double[][] prices, final Fragment fragment) {
            Log.i(TAG, "Adding route " + route);
            final CollectionReference ref = getRouteRefTo();
            final String id = route.getRouteName();
            // check if prices table is symmetric
            for (int i = 0; i < prices.length; i++) {
                for (int j = 0; j < prices.length; j++) {
                    if (prices[i][j] > prices[j][i]) {
                        prices[j][i] = prices[i][j];
                    } else {
                        prices[i][j] = prices[j][i];
                    }
                }
            }
            // add route details to cities
            final CollectionReference cityref = mCityDatabaseHandler.getCityRefTo();
            cityref.whereIn("name", route.getStops()).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(final QuerySnapshot queryDocumentSnapshots) {
                            List<City> citiesResult = queryDocumentSnapshots
                                    .toObjects(City.class); // contains cities that are in route
                            if (citiesResult.size() != route.getStops().size())
                                throw new AssertionError();
                            for (String stopName : route.getStops()) {
                                for (City city : citiesResult) {
                                    if (city.getName().equals(stopName)) {
                                        if (city.getRoutes().contains(route.getRouteName())) {
                                            // Nothing to do
                                        } else {
                                            // add route to current route list
                                            ArrayList<String> tmp = city.getRoutes();
                                            tmp.add(route.getRouteName());
                                            city.setRoutes(tmp);
                                            cityref.document(city.getName()).set(city);
                                        }
                                    }
                                }

                                }
                            }

                    });
            // add basic route details
            ref.document(id).set(route).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(final Void aVoid) {
                    Log.d(TAG, "Route commit successful!");
                    // now add price details
                    for (int i = 0; i < prices.length; i++) {
                        HashMap<String, Double> map = new HashMap<>();
                        for (int j = 0; j < prices.length; j++) {
                            map.put("" + j, prices[i][j]);
                        }
                        ref.document(id).collection("priceTable").document(i + "").set(map);
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error committing bus", e);
                }
            });
            return id;
        }


        public RouteDatabaseHandler getTicketPrice(String routeName, int boardingPoint, final int droppingPoint) {
            Log.i(TAG, "Getting ticket price " + routeName + " " + boardingPoint + " to " + droppingPoint);
            final String dropping, boarding;
            dropping = String.valueOf(droppingPoint);
            boarding = String.valueOf(boardingPoint);
            final RouteDatabaseHandler task = new RouteDatabaseHandler();
            getRouteRefTo().document(routeName).collection("priceTable").
                    document(boarding).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(final DocumentSnapshot documentSnapshot) {
                    double price = -1;
                    if (documentSnapshot.contains(dropping)) {
                        price = documentSnapshot.getDouble(dropping);
                    }
                    task.successListener.onSuccess(price);
                }
            });
            return task;
        }

        public ArrayList<Date> getStartEndTimes(Route route, Date startTime, int boardingPoint,
                                                final int droppingPoint)
                throws ParseException {
            ArrayList<Date> ret = new ArrayList<>();
            DateFormat sdf = new SimpleDateFormat("HHmm", Locale.US);
            ArrayList<String> times = route.getTime();
            // 0530 is 0 when getTime is called :P
            long offsetLong = 330 * 60 * 1000;
            Date boarding = sdf.parse(times.get(boardingPoint));
            Date dropping = sdf.parse(times.get(droppingPoint));
            Date total = sdf.parse(times.get(times.size() - 1));
            long boardingOffsetLong = boarding.getTime() + offsetLong;
            long droppingOffsetLong = dropping.getTime() + offsetLong;
            long totalLong = total.getTime() + offsetLong;
            if (droppingPoint > boardingPoint) {
                ret.add(new Date(startTime.getTime() + boardingOffsetLong));
                ret.add(new Date(startTime.getTime() + droppingOffsetLong));
            } else {
                ret.add(new Date(startTime.getTime() + totalLong - boardingOffsetLong));
                ret.add(new Date(startTime.getTime() + totalLong - droppingOffsetLong));
            }
            return ret;
        }

        public void setOnSuccessCustomListener(OnSuccessCustomListener<Double> listener) {
            this.successListener = listener;
        }

    }

    public class BusDatabaseHandler {

        public CollectionReference getBusRefTo() {
            return getFireStore().collection("buses");
        }

        public void addBusLocation(final MyLocation location) {
            Log.i(TAG, "Adding bus location " + location);
            CollectionReference ref = getFireStore().collection("buslocation");
            ref.document(location.getID()).set(location).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(final Void aVoid) {
                    Log.i(TAG, "Adding bus location success" + location);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull final Exception e) {
                    Log.e(TAG, "Adding bus location failed" + location);
                    Log.e(TAG, e.toString());
                }
            });
        }

        public void getBusLocation(final String busID, final OnSuccessCustomListener<MyLocation> listener) {
            Log.i(TAG, "Getting bus location of " + busID);
            CollectionReference ref = getFireStore().collection("buslocation");
            ref.document(busID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(final DocumentSnapshot documentSnapshot) {
                    MyLocation location = documentSnapshot.toObject(MyLocation.class);
                    Log.i(TAG, "Getting bus location of " + busID + " success");
                    listener.onSuccess(location);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Getting bus location of " + busID + " failed");
                    Log.e(TAG, e.toString());
                    listener.onFailure();
                }
            });
        }

        public String addBus(final Bus bus) {
            Log.i(TAG, "Adding bus " + bus.toString());
            CollectionReference ref = getBusRefTo();
            String id = ref.document().getId();
            bus.setID(id);
            ref.document(id).set(bus).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(final Void aVoid) {
                    ArrayList<Bus> tmp = new ArrayList<>();
                    tmp.add(bus);
                    ContentHolder.updateBusesList(tmp);
                    Log.i(TAG, "Adding bus success for " + bus.getBusName());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Adding bus failed for " + bus.getBusName());
                    Log.e(TAG, e.toString());
                }
            });
            return id;
        }

        public void getBus(final String busID, final OnSuccessCustomListener<Bus> listener) {
            Log.i(TAG, "Getting bus for" + busID);
            List<String> tmp = new ArrayList<>();
            tmp.add(busID);
            final ArrayList<Bus> loadedBuses = ContentHolder.getBusesList(tmp);
            if (loadedBuses != null) {
                listener.onSuccess(loadedBuses.get(0));
            } else {
                getBusRefTo().whereEqualTo("id", busID).get().addOnSuccessListener(
                        new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(final QuerySnapshot querySnapshot) {
                                ArrayList<Bus> loadedBuses = (ArrayList<Bus>) querySnapshot.toObjects(Bus.class);
                                ContentHolder.updateBusesList(loadedBuses);
                                if (loadedBuses.size() == 1) {
                                    Log.i(TAG, "Getting bus success for" + busID);
                                    listener.onSuccess(loadedBuses.get(0));
                                } else {
                                    Log.i(TAG,
                                            "ERR: multiple/null results when retrieving 1 bus " + busID + " results "
                                                    + loadedBuses.size());
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull final Exception e) {
                        Log.e(TAG, "Getting bus failed for " + busID);
                        Log.e(TAG, e.toString());
                        listener.onFailure();
                    }
                });
            }
        }

        public void getBuses(final List<String> busIDs, final OnSuccessCustomListener<ArrayList<Bus>> listener) {
            Log.i(TAG, "Getting buses " + busIDs.toString());
            final ArrayList<Bus> loadedBuses = ContentHolder.getBusesList(busIDs);
            if (loadedBuses != null) {
                listener.onSuccess(loadedBuses);
            } else {
                if (busIDs.size() == 0) {
                    listener.onSuccess(new ArrayList<Bus>());
                    return;
                }
                getBusRefTo().whereIn("id", busIDs).get().addOnSuccessListener(
                        new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(final QuerySnapshot querySnapshot) {
                                ArrayList<Bus> loadedBuses = (ArrayList<Bus>) querySnapshot.toObjects(Bus.class);
                                Log.i(TAG, loadedBuses.size() + " buses found for busIDs " + busIDs.size());
                                ContentHolder.updateBusesList(loadedBuses);
                                listener.onSuccess(loadedBuses);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Getting buses failed for " + busIDs.size());
                        Log.e(TAG, e.toString());
                        listener.onFailure();
                    }
                });
            }
        }

    }

    public class CityDatabaseHandler {

        public void addCityBusStopLocation(MyLocation location) {
            Log.i(TAG, "Add city bus stop location " + location);
            CollectionReference ref = getFireStore().collection("location");
            ref.document(location.getID()).set(location).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(final Void aVoid) {
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull final Exception e) {
                }
            });
        }

        public void getCityBusStopLocation(City city, final OnSuccessCustomListener<MyLocation> listener) {
            Log.i(TAG, "Getting " + city.getName() + "city bus stop location");
            String stopName = city.getName();
            CollectionReference ref = getFireStore().collection("location");
            ref.document(stopName).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(final DocumentSnapshot documentSnapshot) {
                    MyLocation location = documentSnapshot.toObject(MyLocation.class);
                    listener.onSuccess(location);
                }
            });
        }

        public void getAllCities(final OnSuccessCustomListener<ArrayList<City>> listener) {
            Log.w(TAG, "Collecting all cities!!!");
            final ArrayList<City> loadedCities = ContentHolder.getCitiesList();
            if (loadedCities != null) {
                listener.onSuccess(loadedCities);
            } else {
                getCityRefTo().get().addOnSuccessListener(
                        new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(final QuerySnapshot querySnapshot) {
                                ArrayList<City> loadedCities = (ArrayList<City>) querySnapshot
                                        .toObjects(City.class);
                                ContentHolder.updateCitiesList(loadedCities);
                                listener.onSuccess(loadedCities);
                            }
                        });
            }
        }

        public void getCities(List<String> cityNames, final OnSuccessCustomListener<ArrayList<City>> listener) {
            Log.i(TAG, "Collecting cities, " + cityNames.toString());
            final ArrayList<City> loadedCities = ContentHolder.getCitiesList(cityNames);
            if (loadedCities != null) {
                listener.onSuccess(loadedCities);
            } else {
                if (cityNames.size() == 0) {
                    listener.onSuccess(new ArrayList<City>());
                    return;
                }
                getCityRefTo().whereIn("name", cityNames).get().addOnSuccessListener(
                        new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(final QuerySnapshot querySnapshot) {
                                ArrayList<City> loadedCities = (ArrayList<City>) querySnapshot.toObjects(City.class);
                                ContentHolder.updateCitiesList(loadedCities);
                                listener.onSuccess(loadedCities);
                            }
                        });
            }
        }

        public CollectionReference getCityRefTo() {
            return getFireStore().collection("cities");
        }
    }

    public class UserFeedBackHandler {

        public void addFeedback(float stars, String message, Transaction transaction, String ID) {
            // transaction to get userid and tripid
            CollectionReference ref = getFireStore().collection("userfeedback");
            DocumentReference doc;
            if (ID == null) {
                doc = ref.document();
            } else {
                doc = ref.document(ID);
            }
            Map<String, Object> docData = new HashMap<>();
            docData.put("ID", doc.getId());
            docData.put("stars", stars);
            docData.put("message", message);
            docData.put("scheduleID", transaction.getScheduleID());
            docData.put("userID", transaction.getUserID());
            doc.set(docData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(final Void aVoid) {
                    Log.i(TAG, "Feedback successfully sent");
                }
            });

        }

        public void getFeedback(Transaction transaction, final OnSuccessCustomListener<Bundle> listener) {
            // transaction to get userid and tripid
            CollectionReference ref = getFireStore().collection("userfeedback");
            ref.whereEqualTo("scheduleID", transaction.getScheduleID())
                    .whereEqualTo("userID", transaction.getUserID())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(final QuerySnapshot querySnapshot) {
                            if (querySnapshot.size() == 1) {
                                DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                double stars = (double) documentSnapshot.get("stars");
                                String msg = (String) documentSnapshot.get("message");
                                Bundle bundle = new Bundle();
                                bundle.putString("ID", (String) documentSnapshot.get("ID"));
                                bundle.putString("message", msg);
                                bundle.putFloat("stars", Double.valueOf(stars).floatValue());
                                listener.onSuccess(bundle);

                            } else {
                                listener.onSuccess(null);
                            }
                        }
                    });
        }
    }

    public interface OnParseSeatsCompleteListener {

        void onSeatStatesAquired(HashMap<String, Seat> seatStates, ArrayList<Booking> bookings);
    }

    public interface OnSuccessCustomListener<T> {

        void onSuccess(T data);

        void onFailure();
    }

    public interface OnSuccessFailureCustomListener<T> {

        void onSuccess(T data);

        void onFailure();
    }


    public DatabaseHandler() {
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mFireStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mMessaging = FirebaseMessaging.getInstance();
        mDateTime = Timestamp.now().toDate();
        mBookingDatabaseHandler = new BookingDatabaseHandler();
        mBusDatabaseHandler = new BusDatabaseHandler();
        mRouteDatabaseHandler = new RouteDatabaseHandler();
        mCityDatabaseHandler = new CityDatabaseHandler();
        mTransactionDatabaseHandler = new TransactionDatabaseHandler();
        mScheduleDatabaseHandler = new ScheduleDatabaseHandler();
        mUserFeedBackHandler = new UserFeedBackHandler();
    }

    public FirebaseFirestore getFireStore() {
        return mFireStore;
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public DatabaseReference getDatabaseRef() {
        return mDatabaseRef;
    }

    public StorageReference getStorageRef() {
        return mStorageRef;
    }

    public FirebaseMessaging getMessaging() {
        return mMessaging;
    }

    public Date getmDateTime() {
        return mDateTime;
    }
}
