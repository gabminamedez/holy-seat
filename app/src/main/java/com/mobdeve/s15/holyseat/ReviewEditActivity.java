package com.mobdeve.s15.holyseat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ReviewEditActivity extends AppCompatActivity {

    private static final String TAG = "ReviewEditActivity";

    FloatingActionButton imageInputBtn;
    private ImageButton backButton;
    private RatingBar editRating;
    private TextInputEditText editReviewDetails;
    private Button editButton;
    private TextView reviewEditLabel;

    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_edit);
        backButton = findViewById(R.id.backButton);
        editRating = findViewById(R.id.editRating);
        editReviewDetails = findViewById(R.id.editReviewDetails);
        editButton = findViewById(R.id.editButton);
        reviewEditLabel = findViewById(R.id.reviewEditLabel);

        db = FirebaseFirestore.getInstance();

        Intent i = getIntent();
        String reviewRefString = i.getStringExtra(ReviewActivity.REVIEW_KEY);
        String toiletRefString = i.getStringExtra("TOILET_KEY");

        db.collection("Toilets").document(toiletRefString).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Toilet toilet = documentSnapshot.toObject(Toilet.class);
                reviewEditLabel.setText("Edit Review for " + toilet.getLocation());

            }
        });

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

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(ReviewEditActivity.this);
                //We have added a title in the custom layout. So let's disable the default title.
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
                dialog.setCancelable(true);
                //Mention the name of the layout of your custom dialog.
                dialog.setContentView(R.layout.edit_review_dialog);
                //Initializing the views of the dialog.
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                Button btnCancel = dialog.findViewById(R.id.btnCancel);
                Button btnEdit = dialog.findViewById(R.id.btnEdit);

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        float rating = editRating.getRating();
                        String details = editReviewDetails.getText().toString();
                        Map<String, Object> newReview = new HashMap<>();
                        newReview.put("rating", rating);
                        newReview.put("details", details);
                        newReview.put("numUpvotes", 0);
                        newReview.put("posted", FieldValue.serverTimestamp());
                        db.collection("Reviews").document(reviewRefString).update(newReview).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG, "onSuccess: Review successfully updated.");
                                finish();
                            }
                        });
                    }
                });
                dialog.show();
            }
        });
    }
}