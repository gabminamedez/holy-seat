package com.mobdeve.s15.holyseat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReviewEditActivity extends AppCompatActivity {

    private static final String TAG = "ReviewEditActivity";

    FloatingActionButton imageInputBtn;
    private ImageButton backButton;
    private RatingBar editRating;
    private TextInputEditText editReviewDetails;
    private Button editButton;

    private FirebaseFirestore db;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_edit);
        backButton = findViewById(R.id.backButton);
        editRating = findViewById(R.id.editRating);
        editReviewDetails = findViewById(R.id.editReviewDetails);
        editButton = findViewById(R.id.addButton);

        db = FirebaseFirestore.getInstance();


        Intent i = getIntent();
        String reviewRefString = i.getStringExtra(ReviewActivity.REVIEW_KEY);

        db.collection("Reviews").document(reviewRefString).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Review review = documentSnapshot.toObject(Review.class);
                editRating.setRating(review.getRating());
                editReviewDetails.setText(review.getDetails());

            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}