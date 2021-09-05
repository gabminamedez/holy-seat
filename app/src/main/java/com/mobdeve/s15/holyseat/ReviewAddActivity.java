package com.mobdeve.s15.holyseat;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewAddActivity extends AppCompatActivity {

    private static final String TAG = "ReviewAddActivity";

    FloatingActionButton imageInputBtn;
    private ImageButton backButton;
    private RatingBar addRating;
    private TextInputEditText addReviewDetails;
    private Button addButton;
    private TextView reviewAddLabel;
    private ImageView reviewImg;
    private Button btnClearImg;
    private ConstraintLayout imageArea;

    private Uri imageUri = null;

    private FirebaseFirestore db;

    private SharedPreferences sp;

    private ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null){
                        imageUri = result.getData().getData();
                        Picasso.get().load(imageUri).into(reviewImg);
                        imageArea.setBackgroundColor(getResources().getColor(R.color.off_white));
                        imageInputBtn.setVisibility(View.INVISIBLE);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_add);
        backButton = findViewById(R.id.backButton);
        addRating = findViewById(R.id.addRating);
        addReviewDetails = findViewById(R.id.addReviewDetails);
        addButton = findViewById(R.id.addButton);
        reviewAddLabel = findViewById(R.id.reviewAddLabel);
        imageInputBtn = findViewById(R.id.imageInputBtn);
        imageArea = findViewById(R.id.imageArea);
        reviewImg = findViewById(R.id.reviewImg);
        btnClearImg = findViewById(R.id.btnClearImg);

        db = FirebaseFirestore.getInstance();

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        Intent i = getIntent();
        String toiletRefString = i.getStringExtra(ToiletActivity.TOILET_KEY);

        db.collection("Toilets").document(toiletRefString).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Toilet toilet = documentSnapshot.toObject(Toilet.class);
                reviewAddLabel.setText("Add Review for " + toilet.getLocation());

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imageArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_OPEN_DOCUMENT);
                myActivityResultLauncher.launch(Intent.createChooser(i, "Select an image"));
            }
        });

        imageInputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_OPEN_DOCUMENT);
                myActivityResultLauncher.launch(Intent.createChooser(i, "Select an image"));
            }
        });

        btnClearImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUri = null;
                imageArea.setBackgroundColor(getResources().getColor(R.color.grey));
                reviewImg.setImageDrawable(null);
                imageInputBtn.setVisibility(View.VISIBLE);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = addRating.getRating();
                String details = addReviewDetails.getText().toString().trim();
                if (rating == 0) {
                    Toast.makeText(ReviewAddActivity.this, "Please input a rating.", Toast.LENGTH_SHORT).show();
                } else if (details.isEmpty()){

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

                            final ProgressDialog progressDialog = new ProgressDialog(ReviewAddActivity.this);
                            progressDialog.setTitle("Adding");
                            progressDialog.show();

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
                                            newReview.put("imageUri", imageUri == null ? "" : imageUri.toString());
                                            newReview.put("reviewerID", user);
                                            newReview.put("reviewerName", displayName); //mark this for edit profile change
                                            newReview.put("toiletID", toilet);
                                            newReview.put("toiletLocation", toiletLocation); //mark this for edit profile change
                                            newReview.put("numUpvotes", 0);
                                            newReview.put("posted", FieldValue.serverTimestamp());

                                            if (imageUri != null){
                                                StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                                                        .child("review_images/" + toiletRefString + "-" + imageUri.getLastPathSegment());
                                                Task t1 = imageRef.putFile(imageUri)
                                                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                                                progressDialog.setCanceledOnTouchOutside(false);
                                                                progressDialog.setMessage("Uploaded  " + (int) progress + "%");
                                                            }
                                                        });
                                                Task t2 = db.collection("Reviews")
                                                        .add(newReview);
                                                Tasks.whenAllSuccess(t1, t2)
                                                        .addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                                                            @Override
                                                            public void onSuccess(List<Object> objects) {
                                                                progressDialog.setCanceledOnTouchOutside(true);
                                                                progressDialog.setMessage("Success!");
                                                                progressDialog.dismiss();

                                                                Intent intent = new Intent();
                                                                ReviewAddActivity.this.setResult(Activity.RESULT_OK, intent);
                                                                finish();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(Exception e) {
                                                                progressDialog.setCanceledOnTouchOutside(true);
                                                                progressDialog.setMessage("Error occurred. Please try again.");
                                                            }
                                                        });
                                            }
                                            else{
                                                db.collection("Reviews")
                                                        .add(newReview).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        progressDialog.setCanceledOnTouchOutside(true);
                                                        progressDialog.setMessage("Success!");
                                                        progressDialog.dismiss();

                                                        Intent intent = new Intent();
                                                        ReviewAddActivity.this.setResult(Activity.RESULT_OK, intent);
                                                        finish();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                    dialog.show();
                }
                else{
                    final ProgressDialog progressDialog = new ProgressDialog(ReviewAddActivity.this);
                    progressDialog.setTitle("Uploading");
                    progressDialog.show();


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
                                    newReview.put("imageUri", imageUri == null ? "" : imageUri.toString());
                                    newReview.put("reviewerID", user);
                                    newReview.put("reviewerName", displayName);
                                    newReview.put("toiletID", toilet);
                                    newReview.put("toiletLocation", toiletLocation);
                                    newReview.put("numUpvotes", 0);
                                    newReview.put("posted", FieldValue.serverTimestamp());

                                    if (imageUri != null){
                                        StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                                                .child("review_images/" + toiletRefString + "-" + imageUri.getLastPathSegment());
                                        Task t1 = imageRef.putFile(imageUri)
                                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                                        progressDialog.setCanceledOnTouchOutside(false);
                                                        progressDialog.setMessage("Uploaded  " + (int) progress + "%");
                                                    }
                                                });
                                        Task t2 = db.collection("Reviews")
                                                .add(newReview);
                                        Tasks.whenAllSuccess(t1, t2)
                                                .addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                                                    @Override
                                                    public void onSuccess(List<Object> objects) {
                                                        progressDialog.setCanceledOnTouchOutside(true);
                                                        progressDialog.setMessage("Success!");

                                                        Intent intent = new Intent();
                                                        ReviewAddActivity.this.setResult(Activity.RESULT_OK, intent);
                                                        finish();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        progressDialog.setCanceledOnTouchOutside(true);
                                                        progressDialog.setMessage("Error occurred. Please try again.");
                                                    }
                                                });
                                    }
                                    else{
                                        db.collection("Reviews")
                                                .add(newReview).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                progressDialog.setCanceledOnTouchOutside(true);
                                                progressDialog.setMessage("Success!");

                                                Intent intent = new Intent();
                                                ReviewAddActivity.this.setResult(Activity.RESULT_OK, intent);
                                                finish();
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }

}