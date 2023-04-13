package com.example.hw10;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.hw10.databinding.FragmentNewTripBinding;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
    File Name: NewTripFragment.java
 */

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewTripFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewTripFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationProviderClient;

    FragmentNewTripBinding binding;
    NewTripInterface mInterface;

    FirebaseUser currentUser;
    FirebaseFirestore db;

    Location location;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NewTripFragment() {
        // Required empty public constructor
    }



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewTripFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewTripFragment newInstance(String param1, String param2) {
        NewTripFragment fragment = new NewTripFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mInterface = (NewTripInterface) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        getActivity().setTitle("Create Trip");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
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
                    location = task.getResult();
                    binding.loading.setText("Success");
                    binding.loading.setTextColor(Color.parseColor("#00FF00"));
                    Log.d("test", "TripFrag: " + location);
                } else {
                    Toast.makeText(getContext(), "Could not get accurate location!", Toast.LENGTH_SHORT).show();
                    task.getException().printStackTrace();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentNewTripBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getCurrentLocation();

        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(location == null && binding.editTextDesc.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(),"Please fill the name and wait for the location coordinates to be picked up", Toast.LENGTH_SHORT).show();
                } else {

                    Map<String,Object> newTrip = new HashMap<>();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

                    newTrip.put("userId", currentUser.getUid());
                    newTrip.put("startLat", location.getLatitude());
                    newTrip.put("startLng", location.getLongitude());
                    newTrip.put("startDateTime", sdf.format(new Date()));
                    newTrip.put("endDateTime", "N/A");
                    newTrip.put("status", "On Going");
                    newTrip.put("distance", "N/A");
                    newTrip.put("desc", binding.editTextDesc.getText().toString());



                    db.getInstance().collection("trips").add(newTrip).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            mInterface.goBackToTrips();
                        }
                    });


                }
            }
        });
    }

    public interface NewTripInterface {
        void goBackToTrips();
    }
}