package com.mobdeve.s15.holyseat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment {

    private final String TAG = "ListFragment";

    private RecyclerView recyclerView;
    private ToiletListAdapter toiletListAdapter;

    private FirebaseFirestore db;

    private String roomFilter;
    private String toiletFilter;
    private int ratingFilter;

    public ListFragment() {
        // Required empty public constructor
    }

    public ListFragment(String roomFilter, String toiletFilter, int ratingFilter) {
        this.roomFilter = roomFilter;
        this.toiletFilter = toiletFilter;
        this.ratingFilter = ratingFilter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        this.recyclerView = view.findViewById(R.id.toiletListRecycler);

        this.toiletListAdapter = new ToiletListAdapter();
        this.recyclerView.setAdapter(this.toiletListAdapter);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        db = FirebaseFirestore.getInstance();
        db.collection("Toilets").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Adding toilets");
                    ArrayList<Toilet> toilets = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()){
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        if(document.getDouble("avgRating") >= ratingFilter) {
                            if(roomFilter.equals("All Rooms") && toiletFilter.equals("All Toilets")) {
                                toilets.add(document.toObject(Toilet.class));
                            }
                            else if(!roomFilter.equals("All Rooms") && toiletFilter.equals("All Toilets")) {
                                if(roomFilter.equals(document.get("roomType"))) {
                                    toilets.add(document.toObject(Toilet.class));
                                }
                            }
                            else if(roomFilter.equals("All Rooms") && !toiletFilter.equals("All Toilets")) {
                                if(toiletFilter.equals(document.get("toiletType"))) {
                                    toilets.add(document.toObject(Toilet.class));
                                }
                            }
                            else if(!roomFilter.equals("All Rooms") && !toiletFilter.equals("All Toilets")) {
                                if(roomFilter.equals(document.get("roomType")) && toiletFilter.equals(document.get("toiletType"))) {
                                    toilets.add(document.toObject(Toilet.class));
                                }
                            }
                        }
                    }
                    Log.d(TAG, "onComplete: Found documents");
                    toiletListAdapter.setToilets(toilets);
                    toiletListAdapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        db.collection("Toilets").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Adding toilets");
                    ArrayList<Toilet> toilets = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()){
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        if(document.getDouble("avgRating") >= ratingFilter) {
                            if(roomFilter.equals("All Rooms") && toiletFilter.equals("All Toilets")) {
                                toilets.add(document.toObject(Toilet.class));
                            }
                            else if(!roomFilter.equals("All Rooms") && toiletFilter.equals("All Toilets")) {
                                if(roomFilter.equals(document.getString("roomType"))) {
                                    toilets.add(document.toObject(Toilet.class));
                                }
                            }
                            else if(roomFilter.equals("All Rooms") && !toiletFilter.equals("All Toilets")) {
                                if(toiletFilter.equals(document.getString("toiletType"))) {
                                    toilets.add(document.toObject(Toilet.class));
                                }
                            }
                            else if(!roomFilter.equals("All Rooms") && !toiletFilter.equals("All Toilets")) {
                                if(roomFilter.equals(document.getString("roomType")) && toiletFilter.equals(document.getString("toiletType"))) {
                                    toilets.add(document.toObject(Toilet.class));
                                }
                            }
                        }
                    }
                    Log.d(TAG, "onComplete: Found documents");
                    toiletListAdapter.setToilets(toilets);
                    toiletListAdapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }
}