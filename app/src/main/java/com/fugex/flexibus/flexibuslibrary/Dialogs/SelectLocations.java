package com.fugex.flexibus.flexibuslibrary.Dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.fugex.flexibus.flexibuslibrary.Helpers.DatabaseHandler;
import com.fugex.flexibus.flexibuslibrary.Models.City;
import com.fugex.flexibus.flexibuslibrary.Models.MyLocation;
import com.fugex.flexibus.flexibuslibrary.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import java.util.ArrayList;


public class SelectLocations extends DialogFragment {

    private GoogleMap mMap;
    private LatLng center;
    TextView tvCityName;
    EditText stopName;
    Button btnSetLocation, btnAddCity;
    DatabaseHandler mDatabaseHandler = DatabaseHandler.getInstance();
    ArrayList<String> busStops = new ArrayList<>();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String routeName;
    private ArrayList<String> cityNames;
    int idx = 0;

    public SelectLocations() {
        // Required empty public constructor
    }

    public static SelectLocations newInstance(ArrayList<String> cityName, String routeName) {
        SelectLocations fragment = new SelectLocations();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_PARAM1, cityName);
        args.putString(ARG_PARAM2, routeName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_DialogMinWidth);
        if (getArguments() != null) {
            cityNames = getArguments().getStringArrayList(ARG_PARAM1);
            routeName = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_locations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvCityName = view.findViewById(R.id.tv_location_name);
        btnSetLocation = view.findViewById(R.id.btn_set_location);
        btnAddCity = view.findViewById(R.id.btn_add_city);
        stopName = view.findViewById(R.id.et_stop_name);
        tvCityName.setText(cityNames.get(0));
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setOnCameraMoveListener(new OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        center = mMap.getCameraPosition().target;

                    }
                });
                btnSetLocation.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        String stop = stopName.getText().toString();
                        if (!busStops.contains(stop)) {
                            busStops.add(stop);
                        }
                        String id = cityNames.get(idx) + "-" + stop;
                        MyLocation location = new MyLocation(id, center.longitude, center.latitude);
                        mDatabaseHandler.mCityDatabaseHandler.addCityBusStopLocation(location);
                        stopName.setText("");
                    }
                });
                btnAddCity.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        City city = new City();
                        city.setName(cityNames.get(idx));
                        ArrayList<String> tmp = new ArrayList<>();
                        tmp.add(routeName);
                        city.setRoutes(tmp);
                        city.setBusStopsInCity(busStops);
                        busStops.clear();
                        CollectionReference ref = mDatabaseHandler.mCityDatabaseHandler.getCityRefTo();
                        ref.document(cityNames.get(idx)).set(city).addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(final Void aVoid) {
                                        Toast.makeText(getContext(), "City added", Toast.LENGTH_LONG).show();
                                        stopName.setText("");
                                        idx++;
                                        if (idx < cityNames.size()) {
                                            tvCityName.setText(cityNames.get(idx));
                                        } else {
                                            dismiss();
                                        }

                                    }
                                });
                    }
                });

            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
