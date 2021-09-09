package com.mobdeve.s15.holyseat;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.grpc.Context;

public class ToiletActivity extends AppCompatActivity {

    public final String TAG = "ToiletActivity";
    public static String TOILET_KEY = "TOILET_KEY";

    private ImageButton backButton;
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

    private FirebaseFirestore db;

    private StorageReference storage;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toilet);

        this.backButton = findViewById(R.id.backButton);
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

        this.reviewAdapter = new ReviewAdapter(this);
        this.recyclerReviews.setAdapter(reviewAdapter);
        this.recyclerReviews.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        Intent i = getIntent();
        String toiletRefString = i.getStringExtra(TOILET_KEY);

        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference toiletRef = db.collection("Toilets").document(toiletRefString);
                toiletRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String checkInToiletId = documentSnapshot.getId();
                        String checkInToiletName = documentSnapshot.getString("location");

                        DocumentReference profileRef = db.collection("Users").document(sp.getString(ProfileActivity.PROFILE_KEY,""));
                        profileRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        User user = document.toObject(User.class);
                                        Map<String, Object> checkin = new HashMap<>();
                                        checkin.put("userID", db.document("Users/" + user.getId()));
                                        checkin.put("userName", user.getDisplayName());
                                        checkin.put("toiletID", db.document("Toilets/" + checkInToiletId));
                                        checkin.put("toiletLocation", checkInToiletName);
                                        checkin.put("checked", FieldValue.serverTimestamp());
                                        db.collection("Check Ins")
                                                .add(checkin)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());

                                                        Dialog dialog2 = new Dialog(ToiletActivity.this);
                                                        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                                        dialog2.setCancelable(true);
                                                        dialog2.setContentView(R.layout.checkin_dialog);
                                                        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                        Button btnCancel = dialog2.findViewById(R.id.btnCancel);
                                                        Button btnCheckInAddReview = dialog2.findViewById(R.id.btnCheckinAddReview);
                                                        dialog2.show();

                                                        btnCancel.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                dialog2.dismiss();
                                                            }
                                                        });
                                                        btnCheckInAddReview.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                Intent intent = new Intent(ToiletActivity.this, ReviewAddActivity.class);
                                                                intent.putExtra(ToiletActivity.TOILET_KEY, toiletRefString);
                                                                startActivity(intent);
                                                                dialog2.dismiss();
                                                            }
                                                        });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error adding checkin", e);
                                                    }
                                                });
                                        Log.d(TAG, "onComplete: done loading");
                                    } else {
                                        Log.d(TAG, "No such document");
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }

                                db.collection("Toilets").document(checkInToiletId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        DocumentReference toilet = documentSnapshot.getReference();
                                        toilet.update("numCheckins", FieldValue.increment(1))
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
                            }
                        });
                    }
                });
            }
        });

        btnAddReview.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ToiletActivity.this, ReviewAddActivity.class);
                intent.putExtra(ToiletActivity.TOILET_KEY, toiletRefString);
                startActivity(intent);
            }
        });
//        toiletRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        Toilet toilet = document.toObject(Toilet.class);
//                        toiletName.setText(toilet.getLocation());
//                        roomType.setText(toilet.getRoomType());
//                        toiletType.setText(toilet.getToiletType());
//                        toiletRating.setText(String.valueOf(toilet.getAvgRating()));
//                        toiletReviews.setText(String.valueOf(toilet.getNumReviews()));
//                        toiletCheckins.setText(String.valueOf(toilet.getNumCheckins()));
//                        if (!toilet.getImageUri().isEmpty()){
//                            String path = "toilet_images/" + Uri.parse(toilet.getImageUri()).getLastPathSegment();
//                            storage.child(path).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Uri> task) {
//                                    if (task.isSuccessful())
//                                        Picasso.get()
//                                                .load(task.getResult())
//                                                .error(R.drawable.ic_error_foreground)
//                                                .into(toiletImg);
//                                }
//
//                            });
//                        }
//                    } else {
//                        Log.d(TAG, "No such document");
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });
//        db.collection("Reviews").whereEqualTo("toiletID", toiletRef).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    ArrayList<Review> reviews = new ArrayList<>();
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        reviews.add(document.toObject(Review.class));
//                        Log.d(TAG, document.getId() + " => " + document.getData());
//                    }
//                    reviewAdapter.setReviews(reviews);
//                    reviewAdapter.sortReviews();
//                    reviewAdapter.notifyDataSetChanged();
//                } else {
//                    Log.d(TAG, "Error getting documents: ", task.getException());
//                }
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent i = getIntent();
        String toiletRefString = i.getStringExtra(TOILET_KEY);

        DocumentReference toiletRef = db.collection("Toilets").document(toiletRefString);

        btnAddReview.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ToiletActivity.this, ReviewAddActivity.class);
                intent.putExtra(ToiletActivity.TOILET_KEY, toiletRefString);
                startActivity(intent);
            }
        });
        toiletRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Toilet toilet = document.toObject(Toilet.class);
                        toiletName.setText(toilet.getLocation());
                        roomType.setText(toilet.getRoomType());
                        toiletType.setText(toilet.getToiletType());
                        toiletRating.setText(String.valueOf(toilet.getAvgRating()));
                        toiletReviews.setText(String.valueOf(toilet.getNumReviews()));
                        toiletCheckins.setText(String.valueOf(toilet.getNumCheckins()));
                        if (!toilet.getImageUri().isEmpty()){
                            String path = "toilet_images/" + Uri.parse(toilet.getImageUri()).getLastPathSegment();
                            storage.child(path).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful())
                                        Picasso.get()
                                                .load(task.getResult())
                                                .error(R.drawable.ic_error_foreground)
                                                .into(toiletImg);
                                }

                            });
                        }
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
                    reviewAdapter.sortReviews();
                    reviewAdapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            reviewAdapter.notifyDataSetChanged();
        }
    }
}