package com.mobdeve.s15.holyseat;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewEditActivity extends AppCompatActivity {

    private static final String TAG = "ReviewEditActivity";

    FloatingActionButton imageInputBtn;
    private ImageButton backButton;
    private RatingBar editRating;
    private TextInputEditText editReviewDetails;
    private Button editButton;
    private TextView reviewEditLabel;
    private ImageView editReviewImg;
    private Button btnClearImg;
    private ConstraintLayout imageArea;

    private long curRating;

    private Uri imageUri = null;

    private FirebaseFirestore db;

    StorageReference storage = FirebaseStorage.getInstance().getReference();


    private ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null){
                        imageUri = result.getData().getData();
                        Picasso.get().load(imageUri).into(editReviewImg);
                        imageArea.setBackgroundColor(getResources().getColor(R.color.off_white));
                        imageInputBtn.setVisibility(View.INVISIBLE);
                    }
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_edit);
        backButton = findViewById(R.id.backButton);
        editRating = findViewById(R.id.editRating);
        editReviewDetails = findViewById(R.id.editReviewDetails);
        editButton = findViewById(R.id.editButton);
        reviewEditLabel = findViewById(R.id.reviewEditLabel);
        editReviewImg = findViewById(R.id.editReviewImg);
        imageArea = findViewById(R.id.imageArea);
        imageInputBtn = findViewById(R.id.imageInputBtn);
        btnClearImg = findViewById(R.id.btnClearImg);

        db = FirebaseFirestore.getInstance();

        Intent i = getIntent();
        String reviewRefString = i.getStringExtra(ReviewActivity.REVIEW_KEY);
        String toiletRefString = i.getStringExtra(ToiletActivity.TOILET_KEY);

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
                curRating = review.getRating();
                editRating.setRating(review.getRating());
                editReviewDetails.setText(review.getDetails());
                imageUri = Uri.parse(review.getImageUri());
                if (!review.getImageUri().isEmpty()){
                    String path = "review_images/" + review.getToiletID().getId() + "-" + Uri.parse(review.getImageUri()).getLastPathSegment();
                    storage.child(path).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful())
                                Picasso.get()
                                        .load(task.getResult())
                                        .error(R.drawable.ic_error_foreground)
                                        .into(editReviewImg);
                                imageArea.setBackgroundColor(getResources().getColor(R.color.off_white));
                                imageInputBtn.setVisibility(View.INVISIBLE);
                        }

                    });
                }
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
                editReviewImg.setImageDrawable(null);
                imageInputBtn.setVisibility(View.VISIBLE);
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

                        final ProgressDialog progressDialog = new ProgressDialog(ReviewEditActivity.this);
                        progressDialog.setTitle("Uploading");
                        progressDialog.show();

                        float rating = editRating.getRating();
                        String details = editReviewDetails.getText().toString();
                        Map<String, Object> newReview = new HashMap<>();
                        newReview.put("rating", rating);
                        newReview.put("details", details);
                        newReview.put("imageUri", imageUri == null ? "" : imageUri.toString());
                        newReview.put("numUpvotes", 0);
                        newReview.put("posted", FieldValue.serverTimestamp());
                        if (imageUri != null && !imageUri.equals(Uri.EMPTY)){
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
                            Task t2 = db.collection("Reviews").document(reviewRefString).update(newReview);

                            db.collection("Upvotes").whereEqualTo("reviewID", db.collection("Reviews").document(reviewRefString)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (DocumentSnapshot document : task.getResult()) {
                                            db.collection("Upvotes").document(document.getId()).delete();
                                        }
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                            db.collection("Toilets").document(toiletRefString).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    DocumentReference toilet = documentSnapshot.getReference();
                                    Double curAvg = documentSnapshot.getDouble("avgRating");
                                    Double curNumReviews = documentSnapshot.getDouble("numReviews");
                                    Double total = curAvg * curNumReviews - curRating;
                                    total = total + rating;
                                    Double result = total / curNumReviews;
                                    result = (double) Math.round(result * 10d) / 10d;

                                    toilet.update("avgRating", result)
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

                            Tasks.whenAllSuccess(t1, t2)
                                    .addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                                        @Override
                                        public void onSuccess(List<Object> objects) {
                                            progressDialog.setCanceledOnTouchOutside(true);
                                            progressDialog.setMessage("Success!");
                                            progressDialog.dismiss();

                                            Intent intent = new Intent();
                                            ReviewEditActivity.this.setResult(Activity.RESULT_OK, intent);
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
                            db.collection("Reviews").document(reviewRefString).update(newReview).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    db.collection("Toilets").document(toiletRefString).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            DocumentReference toilet = documentSnapshot.getReference();
                                            Double curAvg = documentSnapshot.getDouble("avgRating");
                                            Double curNumReviews = documentSnapshot.getDouble("numReviews");
                                            Double total = curAvg * curNumReviews - curRating;
                                            total = total + rating;
                                            Double result = total / curNumReviews;
                                            result = (double) Math.round(result * 10d) / 10d;

                                            toilet.update("avgRating", result)
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

                                    progressDialog.setCanceledOnTouchOutside(true);
                                    progressDialog.setMessage("Success!");
                                    progressDialog.dismiss();

                                    Intent intent = new Intent();
                                    ReviewEditActivity.this.setResult(Activity.RESULT_OK, intent);
                                    finish();
                                }
                            });
                        }
                    }
                });
                dialog.show();
            }
        });
    }
}