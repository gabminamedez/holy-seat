package com.mobdeve.s15.holyseat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ToiletActivity extends AppCompatActivity {

    public final String TAG = "ToiletActivity";
    public static String TOILET_KEY = "TOILET_KEY";

    private ImageView toiletImg;
    private TextView toiletName;
    private TextView roomType;
    private TextView toiletType;
    private TextView toiletRating;
    private TextView toiletReviews;
    private TextView toiletCheckins;
    private Button btnCheckIn;
    private Button btnAddReview;
    private RecyclerView recyclerReviews;
    private ReviewAdapter reviewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toilet);

        this.toiletImg = findViewById(R.id.toiletImg);
        this.toiletName = findViewById(R.id.toiletName);
        this.roomType = findViewById(R.id.roomType);
        this.toiletType = findViewById(R.id.toiletType);
        this.toiletRating = findViewById(R.id.toiletRating);
        this.toiletReviews = findViewById(R.id.toiletReviews);
        this.toiletCheckins = findViewById(R.id.toiletCheckins);
        this.btnCheckIn = findViewById(R.id.btnCheckIn);
        this.btnAddReview = findViewById(R.id.btnAddReview);
        this.recyclerReviews = findViewById(R.id.recyclerReviews);

        this.reviewAdapter = new ReviewAdapter();
        this.recyclerReviews.setAdapter(reviewAdapter);
        this.recyclerReviews.setLayoutManager(new LinearLayoutManager(this));

        btnAddReview.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ToiletActivity.this, ReviewAddActivity.class);
                startActivity(intent);
            }
        });

        Intent i = getIntent();
        String toiletRefString = i.getStringExtra(TOILET_KEY);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference toiletRef = db.collection("Toilets").document(toiletRefString);

        toiletRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Toilet toilet = document.toObject(Toilet.class);
//                        toiletImg
                        toiletName.setText(toilet.getLocation());
                        roomType.setText(toilet.getRoomType());
                        toiletType.setText(toilet.getToiletType());
                        toiletRating.setText(String.valueOf(toilet.getAvgRating()));
                        toiletReviews.setText(String.valueOf(toilet.getNumReviews()));
                        toiletCheckins.setText(String.valueOf(toilet.getNumCheckins()));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        db.collection("Reviews").whereEqualTo("toiletID", toiletRef).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<Review> reviews = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        reviews.add(document.toObject(Review.class));
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                    reviewAdapter.setReviews(reviews);
                    reviewAdapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }
}