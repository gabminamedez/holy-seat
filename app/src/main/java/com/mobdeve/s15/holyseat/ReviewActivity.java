package com.mobdeve.s15.holyseat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ReviewActivity extends AppCompatActivity {

    private final String TAG = "ReviewActivity";
    public static String REVIEW_KEY = "REVIEW_KEY";

    private ImageButton backButton;
    private ImageView reviewToiletImg;
    private TextView reviewToiletName;
    private TextView reviewReviewer;
    private TextView reviewDetails;
    private RatingBar reviewRating;
    private TextView reviewDate;
    private Button btnEditReview;
    private Button btnDeleteReview;

    private SharedPreferences sp;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_review);

        this.backButton = findViewById(R.id.backButton);
        this.reviewToiletImg = findViewById(R.id.reviewToiletImg);
        this.reviewToiletName = findViewById(R.id.reviewToiletName);
        this.reviewRating = findViewById(R.id.reviewRating);
        this.reviewReviewer = findViewById(R.id.reviewReviewer);
        this.reviewDetails = findViewById(R.id.reviewDetails);
        this.btnEditReview = findViewById(R.id.btnEditReview);
        this.btnDeleteReview = findViewById(R.id.btnDeleteReview);
        this.reviewDate = findViewById(R.id.reviewDate);

        db = FirebaseFirestore.getInstance();

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        Intent i = getIntent();
        String reviewRefString = i.getStringExtra(REVIEW_KEY);
        String toiletRefString = i.getStringExtra(ToiletActivity.TOILET_KEY);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnEditReview.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ReviewActivity.this, ReviewEditActivity.class);
                intent.putExtra(REVIEW_KEY, reviewRefString);
                startActivity(intent);
            }
        });

        btnDeleteReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(ReviewActivity.this);
                //We have added a title in the custom layout. So let's disable the default title.
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
                dialog.setCancelable(true);
                //Mention the name of the layout of your custom dialog.
                dialog.setContentView(R.layout.delete_review_dialog);
                //Initializing the views of the dialog.
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                Button btnCancel = dialog.findViewById(R.id.btnCancel);
                Button btnDelete = dialog.findViewById(R.id.btnAdd);



                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        db.collection("Users").document(sp.getString(ProfileActivity.PROFILE_KEY,"")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                DocumentReference user = documentSnapshot.getReference();
                                user.update("numReviews", FieldValue.increment(-1))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "numReviews incremented.");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error updating document", e);
                                            }
                                        });
                            }
                        });
                        db.collection("Toilets").document(toiletRefString).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                DocumentReference toilet = documentSnapshot.getReference();
                                toilet.update("numReviews", FieldValue.increment(-1))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "numReviews incremented.");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error updating document", e);
                                            }
                                        });
                            }
                        });
                        db.collection("Reviews").document(reviewRefString).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG, "onSuccess: Document deleted.");
                                finish();
                            }
                        });
                    }
                });

                dialog.show();
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference toiletRef = db.collection("Toilets").document(toiletRefString);
        DocumentReference reviewRef = db.collection("Reviews").document(reviewRefString);
        toiletRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Toilet toilet = document.toObject(Toilet.class);
//                        reviewToiletImg
                        reviewToiletName.setText(toilet.getLocation());
                        Log.d(TAG, "onComplete: done loading");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        reviewRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Review review = document.toObject(Review.class);
                        reviewReviewer.setText(review.getReviewerName());
                        reviewDetails.setText(review.getDetails());
                        reviewRating.setRating(review.getRating());
                        reviewDate.setText(review.getPostedString());
                        reviewReviewer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(v.getContext(), ProfileActivity.class);
                                i.putExtra(ProfileActivity.PROFILE_KEY, review.getReviewerID().getId());
                                v.getContext().startActivity(i);
                            }
                        });
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}