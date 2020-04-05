package com.fugex.flexibus.flexibuslibrary.Dialogs;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.DialogFragment;

import com.fugex.flexibus.flexibuslibrary.Helpers.ConstantValues;
import com.fugex.flexibus.flexibuslibrary.Helpers.ContentHolder;
import com.fugex.flexibus.flexibuslibrary.Helpers.DatabaseHandler;
import com.fugex.flexibus.flexibuslibrary.Helpers.DatabaseHandler.OnParseSeatsCompleteListener;
import com.fugex.flexibus.flexibuslibrary.Helpers.DatabaseHandler.OnSuccessCustomListener;
import com.fugex.flexibus.flexibuslibrary.Helpers.Functions;
import com.fugex.flexibus.flexibuslibrary.Models.Booking;
import com.fugex.flexibus.flexibuslibrary.Models.Bus;
import com.fugex.flexibus.flexibuslibrary.Models.Schedule;
import com.fugex.flexibus.flexibuslibrary.Models.Seat;
import com.fugex.flexibus.flexibuslibrary.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReserveFragment extends DialogFragment implements OnClickListener {

    private final String TAG = "ReserveFragment";


    private final int seatSize = 100;
    private final int seatGaping = 10;

    ViewGroup layoutBase;
    Button btn_done;
    List<TextView> seatViewList = new ArrayList<>();


    private int mPersonCount = -1;
    private Bus mBus;
    private Schedule mSchedule;
    private String mBusID, mScheduleID;
    private int mJourneyStart, mJourneyEnd;


    private OnFragmentInteractionListener mListener;

    private Context mContext;

    public static DatabaseHandler mDatabaseHandler = DatabaseHandler.getInstance();

    public interface OnFragmentInteractionListener {

        void fragmentOnProgress();

        void fragmentHideProgress();

        void onSeatClicked(View view);

        void onReserveFragmentDetach();

    }


    public ReserveFragment() {
        // Required empty public constructor
    }

    public static ReserveFragment newInstance(String busid, String scheduleID, int journeyStart,
                                              int jouneyEnd, int count) {
        ReserveFragment fragment = new ReserveFragment();
        Bundle args = new Bundle();
        args.putInt(ConstantValues.PERSON_COUNT.name(), count);
        args.putString(ConstantValues.BUS_ID.name(), busid);
        args.putString(ConstantValues.JOURNEY_ID.name(), scheduleID);
        args.putInt(ConstantValues.JOURNEY_START.name(), journeyStart);
        args.putInt(ConstantValues.JOURNEY_END.name(), jouneyEnd);
        fragment.setArguments(args);
        return fragment;
    }

    // when clicked on the fragment
    @Override
    public void onClick(final View view) {
        mListener.onSeatClicked(view);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPersonCount = getArguments().getInt(ConstantValues.PERSON_COUNT.name());
            mScheduleID = getArguments().getString(ConstantValues.JOURNEY_ID.name());
            mJourneyStart = getArguments().getInt(ConstantValues.JOURNEY_START.name());
            mJourneyEnd = getArguments().getInt(ConstantValues.JOURNEY_END.name());
            mBusID = getArguments().getString(ConstantValues.BUS_ID.name());

        }
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btn_done = view.findViewById(R.id.btn_done);
        btn_done.setVisibility(View.GONE);
        mListener.fragmentOnProgress();
        btn_done.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                dismiss();
            }
        });
        mDatabaseHandler.mScheduleDatabaseHandler.getSchedule(mScheduleID, new OnSuccessCustomListener<Schedule>() {
            @Override
            public void onSuccess(final Schedule data) {
                mSchedule = data;
                mDatabaseHandler.mBusDatabaseHandler.getBus(mBusID,
                        new OnSuccessCustomListener<Bus>() {
                            @Override
                            public void onSuccess(final Bus data) {
                                mBus = data;
                                Log.i(TAG, mBus.getID() + " " + mBus.getRouteName() + " " + mBus.getSeats());
                                mDatabaseHandler.mBookingDatabaseHandler
                                        .parseAvailableSeats(mBus, mSchedule, mJourneyStart, mJourneyEnd)
                                        .setCustomEventListener(
                                                new OnParseSeatsCompleteListener() {
                                                    @Override
                                                    public void onSeatStatesAquired(
                                                            final HashMap<String, Seat> seatStates,
                                                            final ArrayList<Booking> bookings) {
                                                        //clear on hold seats if there are in the db
                                                        for (Booking booking : bookings) {
                                                            if (booking.getUserID()
                                                                    .equals(mDatabaseHandler.getAuth().getUid())
                                                                    && booking.getSeatState()
                                                                    == Booking.SEAT_ONHOLD) {
                                                                mDatabaseHandler.mBookingDatabaseHandler
                                                                        .deleteBooking(booking);
                                                            }
                                                        } // TODO this will not hold the seat when selecting!!!
                                                        ContentHolder.selectedSeats
                                                                .clear(); // check selected seats from scratch
                                                        // check if on-hold seat is a already selected seat
                                                        for (String key : seatStates.keySet()) {
                                                            Seat seat = seatStates.get(key);
                                                            if (seat.seatState == Booking.SEAT_ONHOLD) {
                                                                if (seat.seatUserID
                                                                        .equals(mDatabaseHandler.getAuth()
                                                                                .getUid())) {
                                                                    // on hold seat is current user's seat
                                                                    seat.seatState = Booking.SEAT_SELECTED;
                                                                    ContentHolder.selectedSeats.add(seat.seatID);
                                                                }
                                                            } else if (seat.seatState == Booking.SEAT_RESERVED_TMP) {
                                                                if (seat.seatUserID
                                                                        .equals(mDatabaseHandler.getAuth()
                                                                                .getUid())) {
                                                                    seat.seatState = Booking.SEAT_AVAILABLE;
                                                                } else {
                                                                    seat.seatState = Booking.SEAT_RESERVED;
                                                                }
                                                            }
                                                            seatStates.put(key, seat);
                                                        }
                                                        updateLayout(view, seatStates);
                                                    }
                                                });


                            }

                            @Override
                            public void onFailure() {
                                Log.e(TAG, "Getting bus failed for bus: " + mBusID);
                            }
                        });
            }

            @Override
            public void onFailure() {
                Log.e(TAG, "Getting schedule failed for schedule: " + mScheduleID);
            }
        });

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reserve, container, false);
        mContext = view.getContext();
        return view;
    }

    private void updateLayout(View view, HashMap<String, Seat> seatState) {
        layoutBase = view.findViewById(R.id.layoutSeat);
        LinearLayout layoutSeat = new LinearLayout(mContext); // TODO BUG: getContext is null 'SOMETIMES'!
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        layoutSeat.setLayoutParams(params);
        layoutSeat.setOrientation(LinearLayout.VERTICAL);
        layoutSeat.setPadding(2 * seatGaping, 2 * seatGaping, 2 * seatGaping, 2 * seatGaping);
        int[][] seatNumbersMatrix = Functions.decodeSeatLayoutString(mBus.getSeatLayout());
        for (int i = 0; i < seatNumbersMatrix.length; i++) {
            if (getContext() == null) {
                return;
            }
            LinearLayout layoutRow = new LinearLayout(getContext());
            layoutRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams paramsRow = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    0);
            paramsRow.weight = 1;
            layoutRow.setLayoutParams(paramsRow);
            for (int j = 0; j < seatNumbersMatrix[0].length; j++) {
                int id = seatNumbersMatrix[i][j];
                TextView textView = new TextView(getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, seatSize);
                layoutParams.weight = 1;
                layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                textView.setLayoutParams(layoutParams);
                textView.setPadding(0, 0, 0, 2 * seatGaping);
                textView.setId(id);
                textView.setGravity(Gravity.CENTER);
                textView.setText(String.valueOf(id));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                TextViewCompat
                        .setAutoSizeTextTypeUniformWithConfiguration(textView, 5, 15, 1, TypedValue.COMPLEX_UNIT_DIP);
                if (seatState.containsKey(String.valueOf(seatNumbersMatrix[i][j]))) {
                    Seat seat = seatState.get(String.valueOf(seatNumbersMatrix[i][j]));

                    if (seat.seatState == Booking.SEAT_AVAILABLE) {
                        textView.setBackgroundResource(R.drawable.ic_seats_available);
                        textView.setTextColor(Color.BLACK);
                        textView.setTag(Booking.SEAT_AVAILABLE);
                    } else if (seat.seatState == Booking.SEAT_BOOKED) {
                        textView.setBackgroundResource(R.drawable.ic_seats_booked);
                        textView.setTextColor(Color.WHITE);
                        textView.setTag(Booking.SEAT_BOOKED);
                    } else if (seat.seatState == Booking.SEAT_RESERVED) {
                        textView.setBackgroundResource(R.drawable.ic_seats_reserved);
                        textView.setTextColor(Color.WHITE);
                        textView.setTag(Booking.SEAT_RESERVED);
                    } else if (seat.seatState == Booking.SEAT_ONHOLD) {
                        textView.setBackgroundResource(R.drawable.ic_seats_onhold);
                        textView.setTextColor(Color.WHITE);
                        textView.setTag(Booking.SEAT_ONHOLD);
                    } else if (seat.seatState == Booking.SEAT_SELECTED) {
                        textView.setBackgroundResource(R.drawable.ic_seats_selected);
                        textView.setTextColor(Color.BLACK);
                        textView.setTag(Booking.SEAT_SELECTED);
                    }
                    seatViewList.add(textView);
                    textView.setOnClickListener(this);
                } else {
                    textView.setBackgroundColor(Color.TRANSPARENT);
                    textView.setText("");
                }
                layoutRow.addView(textView);
            }
            layoutSeat.addView(layoutRow);

        }
        layoutBase.addView(layoutSeat);
        btn_done.setVisibility(View.VISIBLE);
        layoutBase.setVisibility(View.VISIBLE);
        mListener.fragmentHideProgress();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.fragmentHideProgress();
        mListener.onReserveFragmentDetach();
        mListener = null;
    }

}
