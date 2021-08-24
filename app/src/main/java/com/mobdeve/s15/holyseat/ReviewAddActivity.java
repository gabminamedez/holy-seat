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
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ReviewAddActivity extends AppCompatActivity {

    private static final String TAG = "ReviewAddActivity";

    FloatingActionButton imageInputBtn;
    private ImageButton backButton;
    private RatingBar addRating;
    private TextInputEditText addReviewDetails;
    private Button addButton;

    private FirebaseFirestore db;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_add);
        backButton = findViewById(R.id.backButton);
        addRating = findViewById(R.id.addRating);
        addReviewDetails = findViewById(R.id.addReviewDetails);
        addButton = findViewById(R.id.addButton);

        db = FirebaseFirestore.getInstance();

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        Intent i = getIntent();
        String toiletRefString = i.getStringExtra(ToiletActivity.TOILET_KEY);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = addRating.getRating();
                String details = addReviewDetails.getText().toString().trim();
                if (details.isEmpty()){
                    Dialog dialog = new Dialog(ReviewAddActivity.this);
                    //We have added a title in the custom layout. So let's disable the default title.
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
                    dialog.setCancelable(true);
                    //Mention the name of the layout of your custom dialog.
                    dialog.setContentView(R.layout.no_review_details_dialog);
                    //Initializing the views of the dialog.
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    Button btnCancel = dialog.findViewById(R.id.btnCancel);
                    Button btnAdd = dialog.findViewById(R.id.btnAdd);

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    btnAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            db.collection("Users").document(sp.getString(ProfileActivity.PROFILE_KEY,"")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    DocumentReference user = documentSnapshot.getReference();
                                    user.update("numReviews", FieldValue.increment(1))
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
                                    String displayName = documentSnapshot.get("displayName").toString();
                                    db.collection("Toilets").document(toiletRefString).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            DocumentReference toilet = documentSnapshot.getReference();
                                            toilet.update("numReviews", FieldValue.increment(1))
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
                                            String toiletLocation = documentSnapshot.get("location").toString();
                                            Map<String, Object> newReview = new HashMap<>();
                                            newReview.put("rating", rating);
                                            newReview.put("details", details);
                                            newReview.put("reviewerID", user);
                                            newReview.put("reviewerName", displayName);
                                            newReview.put("toiletID", toilet);
                                            newReview.put("toiletLocation", toiletLocation);
                                            newReview.put("numUpvotes", 0);
                                            newReview.put("posted", FieldValue.serverTimestamp());
                                            db.collection("Reviews")
                                                    .add(newReview)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            finish();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w(TAG, "Error adding document", e);
                                                        }
                                                    });
                                        }
                                    });
                                }
                            });
                        }
                    });
                    dialog.show();
                }
                else{
                    db.collection("Users").document(sp.getString(ProfileActivity.PROFILE_KEY,"")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            DocumentReference user = documentSnapshot.getReference();
                            user.update("numReviews", FieldValue.increment(1))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "User numReviews incremented.");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error updating document", e);
                                        }
                                    });
                            String displayName = documentSnapshot.get("displayName").toString();
                            db.collection("Toilets").document(toiletRefString).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    DocumentReference toilet = documentSnapshot.getReference();
                                    toilet.update("numReviews", FieldValue.increment(1))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "Toilet numReviews incremented.");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error updating document", e);
                                                }
                                            });
                                    String toiletLocation = documentSnapshot.get("location").toString();
                                    Map<String, Object> newReview = new HashMap<>();
                                    newReview.put("rating", rating);
                                    newReview.put("details", details);
                                    newReview.put("reviewerID", user);
                                    newReview.put("reviewerName", displayName);
                                    newReview.put("toiletID", toilet);
                                    newReview.put("toiletLocation", toiletLocation);
                                    newReview.put("numUpvotes", 0);
                                    newReview.put("posted", FieldValue.serverTimestamp());
                                    db.collection("Reviews")
                                            .add(newReview)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error adding document", e);
                                                }
                                            });
                                }
                            });
                        }
                    });
                }
            }
        });
    }

}