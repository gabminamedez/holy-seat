package com.mobdeve.s15.holyseat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import io.grpc.InternalNotifyOnServerBuild;

public class ProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final String TAG = "ProfileActivity";
    public static final String PROFILE_KEY = "PROFILE_KEY";
    private static final String[] paths = {"All Activities", "Reviews", "Check-ins"};

    private ImageButton backButton;
    private TextView profileName;
    private TextView profileUser;
    private TextView profileReviews;
    private TextView profileNone;
    private Button btnEditProfile;
    private Spinner spinner;
    private RecyclerView recyclerActivities;
    private ProfileAdapter profileAdapter;

    private FirebaseFirestore db;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        this.backButton = findViewById(R.id.backButton);
        this.profileName = findViewById(R.id.profileName);
        this.profileUser = findViewById(R.id.profileUser);
        this.profileReviews = findViewById(R.id.profileReviews);
        this.profileNone = findViewById(R.id.profileNone);
        this.btnEditProfile = findViewById(R.id.btnEditProfile);
        this.recyclerActivities = findViewById(R.id.recyclerActivities);
        this.spinner = findViewById(R.id.spinner);


//        ArrayAdapter<String> adapter = new ArrayAdapter<>(ProfileActivity.this,
//                android.R.layout.simple_spinner_dropdown_item,paths);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(this);
//
//
//        this.profileAdapter = new ProfileAdapter();
//        this.recyclerActivities.setAdapter(profileAdapter);
//        this.recyclerActivities.setLayoutManager(new LinearLayoutManager(this));

        Intent i = getIntent();
        String profileRefString = i.getStringExtra(PROFILE_KEY);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnEditProfile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
                intent.putExtra(ProfileEditActivity.PROFILE_KEY, profileRefString);
                startActivity(intent);
                finish();
            }
        });


//        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemSelected: " + parent.getItemAtPosition(position));
        if (parent.getItemAtPosition(position).equals("Reviews")){
            ArrayList<Review> reviews = new ArrayList<>();
            for (Activity a: profileAdapter.getActivities()){
                if (a instanceof Review)
                    reviews.add((Review) a);
            }
            profileAdapter.filterReviews(reviews);
            recyclerActivities.setAdapter(profileAdapter);
            if (profileAdapter.getItemCount() == 0){
                profileNone.setText(R.string.no_activities_reviews);
                profileNone.setVisibility(View.VISIBLE);
            }else{
                profileNone.setVisibility(View.GONE);
            }
        }
        else if (parent.getItemAtPosition(position).equals("Check-ins")){
            ArrayList<CheckIn> checkIns = new ArrayList<>();
            for (Activity a: profileAdapter.getActivities()){
                if (a instanceof CheckIn)
                    checkIns.add((CheckIn) a);
            }
            profileAdapter.filterCheckIns(checkIns);
            recyclerActivities.setAdapter(profileAdapter);
            if (profileAdapter.getItemCount() == 0){
                profileNone.setText(R.string.no_activities_checkins);
                profileNone.setVisibility(View.VISIBLE);
            }else{
                profileNone.setVisibility(View.GONE);
            }
        }
        else{
            profileAdapter.filterAll();
            profileAdapter.notifyDataSetChanged();
            if (profileAdapter.getItemCount() == 0){
                profileNone.setText(R.string.no_activities);
                profileNone.setVisibility(View.VISIBLE);
            }else{
                profileNone.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent i = getIntent();
        String profileRefString = i.getStringExtra(PROFILE_KEY);
        db = FirebaseFirestore.getInstance();
        DocumentReference profileRef = db.collection("Users").document(profileRefString);
        if (!sp.getString(ProfileActivity.PROFILE_KEY, "").equals(profileRefString))
            btnEditProfile.setVisibility(View.INVISIBLE);

        profileRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        profileName.setText(user.getDisplayName());
                        profileUser.setText("@" + user.getUsername());
                        profileReviews.setText(String.valueOf(user.getNumReviews()));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(ProfileActivity.this,
                android.R.layout.simple_spinner_dropdown_item,paths);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        this.profileAdapter = new ProfileAdapter();
        this.recyclerActivities.setAdapter(profileAdapter);
        this.recyclerActivities.setLayoutManager(new LinearLayoutManager(this));
        profileAdapter.clearActivities();

        db.collection("Check Ins").whereEqualTo("userID", profileRef).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<CheckIn> checkIns = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        checkIns.add(document.toObject(CheckIn.class));
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                    profileAdapter.addCheckInsAll(checkIns);
                    db.collection("Reviews").whereEqualTo("reviewerID", profileRef).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                ArrayList<Review> reviews = new ArrayList<>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    reviews.add(document.toObject(Review.class));
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }
                                profileAdapter.clearActivities();
                                profileAdapter.addCheckInsAll(checkIns);
                                profileAdapter.addReviewsAll(reviews);
                                profileAdapter.sortActivities();
                                profileAdapter.notifyDataSetChanged();
                                if (profileAdapter.getItemCount() == 0){
                                    profileNone.setText(R.string.no_activities);
                                    profileNone.setVisibility(View.VISIBLE);
                                }else{
                                    profileNone.setVisibility(View.GONE);
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
        spinner.setSelection(0);
    }

}