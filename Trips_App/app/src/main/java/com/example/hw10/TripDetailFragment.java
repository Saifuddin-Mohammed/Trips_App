package com.example.hw10;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.hw10.databinding.FragmentTripDetailBinding;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/*
Assignment: InClass10
    Name: Juhi Jayant Jadhav
    Name: Saifuddin Mohammed
    Group No: 05
    File Name: TripDetailFragment.java
 */
/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TripDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripDetailFragment extends Fragment implements OnMapReadyCallback {

    FragmentTripDetailBinding binding;

    private GoogleMap googleMap;
    MapView mMapView;

    FirebaseFirestore db;

    Marker startMarker ;

    Marker endMarker ;

    Location newLocation;

    FusedLocationProviderClient fusedLocationProviderClient;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private Trip mParam1;

    public TripDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment TripDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TripDetailFragment newInstance(Trip param1) {
        TripDetailFragment fragment = new TripDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = (Trip) getArguments().getSerializable(ARG_PARAM1);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        getActivity().setTitle("Trip Details");
        getCurrentLocation();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mListener = (TripDetailInterface) context;
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation() {

        CurrentLocationRequest currentLocationRequest = new CurrentLocationRequest.Builder()
                .setGranularity(Granularity.GRANULARITY_FINE)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setDurationMillis(5000)
                .setMaxUpdateAgeMillis(0)
                .build();

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        fusedLocationProviderClient.getCurrentLocation(currentLocationRequest,cancellationTokenSource.getToken()).addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()) {
                    newLocation = task.getResult();

                    Log.d("test", "TripFrag: " + newLocation);
                    if(newLocation!=null){
                     getActivity().runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             binding.buttonComplete.setEnabled(true);
                         }
                     });
                    }
                } else {
                    Toast.makeText(getContext(), "Could not get accurate location!", Toast.LENGTH_SHORT).show();
                    task.getException().printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        getActivity().setTitle("Trips");
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTripDetailBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView = (MapView) binding.mapView;
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull GoogleMap mMap) {
                        googleMap = mMap;

                        if(mParam1.endLat !=null && mParam1.startLat!=null){
                            LatLng location1 = new LatLng(mParam1.startLat, mParam1.startLng);
                            LatLng location2 = new LatLng(mParam1.endLat, mParam1.endLng);
                            googleMap.addMarker(new MarkerOptions().position(location1).title("Marker Title").snippet("Marker Description"));
                            googleMap.addMarker(new MarkerOptions().position(location2).title("Marker Title").snippet("Marker Description"));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(location1));
                            binding.buttonComplete.setVisibility(View.GONE);
                            binding.textViewDistance.setEnabled(true);
                            Double distanceFinal = Double.valueOf(mParam1.distance);
                            distanceFinal = distanceFinal/1609.344;
                            binding.textViewDistance.setText(distanceFinal+"miles");
                        }
                        else {

                            Log.d("demo", "onMapReady: ");


                            // For dropping a marker at a point on the Map
                            LatLng location = new LatLng(mParam1.startLat, mParam1.startLng);
                            startMarker = googleMap.addMarker(new MarkerOptions().position(location).title("Marker Title").snippet("Marker Description"));

                            // For zooming automatically to the location of the marker
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(12).build();
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                        }
                    }
                });
            }
        });

        return binding.getRoot();
    }





    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);




        binding.textViewDesc.setText(mParam1.desc);
        binding.textViewStatus.setText(mParam1.status);

        if(mParam1.status.equals("On Going")) {
            binding.textViewStatus.setTextColor(Color.parseColor("#FFA500"));
        } else {
            binding.textViewStatus.setTextColor(Color.parseColor("#00FF00"));
        }

        binding.textViewStartTime.setText("Started At: " + mParam1.startDateTime);
        binding.textViewEndTime.setText("Completed At: " + mParam1.endDateTime);

        binding.buttonComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("demo", "onClick:+ (((((((( ");
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

                Map<String, Object> updatedMap = new HashMap<>();
                updatedMap.put("status", "Completed");
                updatedMap.put("endLat", newLocation.getLatitude());
                updatedMap.put("endLng", newLocation.getLongitude());
                updatedMap.put("endDateTime", sdf.format(new Date()));

                float[] results = new float[1];

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Location.distanceBetween(mParam1.startLat, mParam1.startLng, newLocation.getLatitude(), newLocation.getLongitude(), results);
                    }
                });


                updatedMap.put("distance", String.valueOf(results[0]));

                db.collection("trips").document(mParam1.id)
                        .update(updatedMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mMapView.getMapAsync(new OnMapReadyCallback() {
                                            @Override
                                            public void onMapReady(@NonNull GoogleMap mMap) {
                                                Log.d("demo", "onMapReady: ");
                                                googleMap = mMap;

                                                // For dropping a marker at a point on the Map
                                                LatLng location = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
                                                Log.d("demo", "onMapReady: jdbcjgf "+ newLocation.getLatitude() + newLocation.getLongitude());

                                                googleMap.addMarker(new MarkerOptions().position(location).title("Marker Title").snippet("Marker Description"));
                                               // startMarker.remove();
                                                binding.buttonComplete.setVisibility(View.GONE);
                                                binding.textViewDistance.setEnabled(true);
                                                Location.distanceBetween(mParam1.startLat, mParam1.startLng, newLocation.getLatitude(), newLocation.getLongitude(), results);

//
//                                                Double distanceFinal = Double.valueOf(mParam1.distance);
//                                                distanceFinal = distanceFinal/1609.344;
                                                binding.textViewDistance.setText(results[0]+ "miles");

                                                binding.textViewStatus.setText("Completed");
                                                binding.textViewStatus.setTextColor(Color.GREEN);

                                                // For zooming automatically to the location of the marker
                                                CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(12).build();
                                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));


                                            }
                                        });
                                    }
                                });
                            }
                        });




            }


        });


    }

    @Override
    public void onMapReady(@NonNull GoogleMap mMap) {
        googleMap = mMap;

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        // googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    TripDetailInterface mListener;




    public interface TripDetailInterface {
        void goBackToTrips();
    }
}