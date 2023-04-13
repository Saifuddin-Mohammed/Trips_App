package com.example.hw10;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hw10.databinding.FragmentTripsBinding;
import com.example.hw10.databinding.TripRowItemBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TripsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
/*
Assignment: InClass10
    Name: Juhi Jayant Jadhav
    Name: Saifuddin Mohammed
    Group No: 05
    File Name: TripsFragmentFragment.java
 */

public class TripsFragment extends Fragment {

    FirebaseUser currentUser;
    FirebaseFirestore db;

    ArrayList<Trip> allTrips = new ArrayList<>();

    FragmentTripsBinding binding;

    TripAdapter tripAdapter;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TripsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TripsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TripsFragment newInstance(String param1, String param2) {
        TripsFragment fragment = new TripsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        getActivity().setTitle("Trips");
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getTrips();

        binding.recyclerViewTrips.setLayoutManager(new LinearLayoutManager(getContext()));
        tripAdapter = new TripAdapter();
        binding.recyclerViewTrips.setAdapter(tripAdapter);


        binding.addTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInterface.goToNewTrip();
            }
        });

    }


    private void getTrips() {

        db.collection("trips").whereEqualTo("userId", currentUser.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        allTrips.clear();

                        for (QueryDocumentSnapshot doc: queryDocumentSnapshots) {
                            Trip newTrip = doc.toObject(Trip.class);
                            newTrip.id = doc.getId();
                            newTrip.userId = currentUser.getUid();
                            allTrips.add(newTrip);
                        }
                        tripAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTripsBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mInterface = (TripInterface) context;

    }

    public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

        @NonNull
        @Override
        public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TripRowItemBinding binding = TripRowItemBinding.inflate(getLayoutInflater(), parent, false);
            return new TripViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull TripAdapter.TripViewHolder holder, int position) {
            Trip trip = allTrips.get(position);
            holder.setupUI(trip);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mInterface.goToTripDetails(trip);
                }
            });
        }


        @Override
        public int getItemCount() {
            return allTrips.size();
        }

        public class TripViewHolder extends RecyclerView.ViewHolder{
            TripRowItemBinding tripRowItemBinding;
            Trip mTrip;
            public TripViewHolder(@NonNull TripRowItemBinding binding) {
                super(binding.getRoot());
                tripRowItemBinding = binding;
            }

            public void setupUI(Trip trip) {
                mTrip = trip;
                String completedAt = "N/A";

                if(mTrip.status.equals("On Going")) {
                    completedAt = "N/A";
                    tripRowItemBinding.status.setTextColor(Color.parseColor("#FFA500"));
                }

                if(mTrip.status.equals("Completed")) {
                    completedAt = mTrip.endDateTime;
                    tripRowItemBinding.status.setTextColor(Color.parseColor("#00FF00"));
                }
                tripRowItemBinding.description.setText(mTrip.desc);
                tripRowItemBinding.endTime.setText("Completed At: " + completedAt);
                tripRowItemBinding.status.setText(mTrip.status);
                tripRowItemBinding.startTime.setText("Started At: " + mTrip.startDateTime);

            }

        }

    }


    public interface TripInterface {
        void goToTripDetails(Trip trip);
        void goToNewTrip();
    }

    TripInterface mInterface;



}