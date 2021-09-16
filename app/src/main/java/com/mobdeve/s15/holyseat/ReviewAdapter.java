package com.mobdeve.s15.holyseat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.Rating;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder>{
    private final String TAG = "ReviewAdapter";

    private ArrayList<Review> reviews;
    private Context context;

    private StorageReference storage = FirebaseStorage.getInstance().getReference();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private SharedPreferences sp;
    private String profileRefString;

    public ReviewAdapter(Context context, String profileRefString) {
        this.reviews = new ArrayList<>();
        this.context = context;
        this.sp = PreferenceManager.getDefaultSharedPreferences(context);
        this.profileRefString = profileRefString;
    }

    @NonNull
    @Override
    public ReviewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.toiletreview_layout, parent, false);
        ReviewAdapter.MyViewHolder myViewHolder = new ReviewAdapter.MyViewHolder(itemView);
        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.MyViewHolder holder, int position) {
        holder.bind(reviews.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), ProfileActivity.class);
                i.putExtra(ProfileActivity.PROFILE_KEY, reviews.get(holder.getBindingAdapterPosition()).getReviewerID().getId());
                v.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView toiletReviewUser, toiletReviewUpvotes, toiletReviewDetails, toiletReviewDate;
        private LinearLayout toiletReviewUpvoteSection;
        private RatingBar toiletReviewRating;
        private ImageView toiletReviewImg;
        private ImageView toiletReviewUpvoted;
        private Button editReviewBtn;
        private Button deleteReviewBtn;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.toiletReviewUser = itemView.findViewById(R.id.toiletReviewUser);
            this.toiletReviewUpvotes = itemView.findViewById(R.id.toiletReviewUpvotes);
            this.toiletReviewUpvoteSection = itemView.findViewById(R.id.toiletReviewUpvoteSection);
            this.toiletReviewDetails = itemView.findViewById(R.id.toiletReviewDetails);
            this.toiletReviewDate = itemView.findViewById(R.id.toiletReviewDate);
            this.toiletReviewRating = itemView.findViewById(R.id.toiletReviewRating);
            this.toiletReviewImg = itemView.findViewById(R.id.toiletReviewImg);
            this.toiletReviewUpvoted = itemView.findViewById(R.id.toiletReviewUpvoted);
            this.editReviewBtn = itemView.findViewById(R.id.editReviewBtn);
            this.deleteReviewBtn = itemView.findViewById(R.id.deleteReviewBtn);
        }
        public void bind(Review review) {
            this.toiletReviewUser.setText(review.getReviewerName());
            this.toiletReviewUpvotes.setText(String.valueOf(review.getNumUpvotes()));
            this.toiletReviewDetails.setText(review.getDetails());
            this.toiletReviewDate.setText(review.getPostedString());
            this.toiletReviewRating.setRating(review.getRating());

            DocumentReference reviewRef = db.collection("Reviews").document(review.getId());
            DocumentReference profileRef = db.collection("Users").document(sp.getString(ProfileActivity.PROFILE_KEY, ""));

            profileRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (review.getReviewerID().getId().equals(task.getResult().getId())) {
                            editReviewBtn.setVisibility(View.VISIBLE);
                            deleteReviewBtn.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });

            editReviewBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), ReviewEditActivity.class);
                    i.putExtra(ReviewActivity.REVIEW_KEY, review.getId());
                    i.putExtra(ToiletActivity.TOILET_KEY, review.getToiletID().getId());
                    v.getContext().startActivity(i);
                }
            });

            deleteReviewBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog dialog = new Dialog(v.getContext());
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
                            db.collection("Toilets").document(review.getToiletID().getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    DocumentReference toilet = documentSnapshot.getReference();
                                    Double curAvg = documentSnapshot.getDouble("avgRating");
                                    Double curNumReviews = documentSnapshot.getDouble("numReviews");
                                    Double total = curAvg * curNumReviews - review.getRating();
                                    Double newNumReviews = curNumReviews - 1;
                                    Double result = total / newNumReviews;
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
                            db.collection("Reviews").document(review.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "onSuccess: Document deleted.");
                                    reviews.remove(getBindingAdapterPosition());
                                    ReviewAdapter.this.notifyDataSetChanged();
                                }
                            });
                        }
                    });

                    dialog.show();
                }
            });

            if (!review.getImageUri().isEmpty()){
                String path = "review_images/" + review.getToiletID().getId() + "-" + Uri.parse(review.getImageUri()).getLastPathSegment();
                storage.child(path).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful())
                            Picasso.get()
                                    .load(task.getResult())
                                    .error(R.drawable.ic_error_foreground)
                                    .into(toiletReviewImg);
                    }
                });
            }
            else{
                toiletReviewImg.setVisibility(View.GONE);
            }

            db.collection("Upvotes").whereEqualTo("reviewID", reviewRef).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        System.out.println(task.getResult().size());
                        System.out.println(review.getId());
                        toiletReviewUpvotes.setText(String.valueOf(task.getResult().size()));
                        toiletReviewUpvoted.setImageResource(R.drawable.ic_twotone_arrow_drop_up_24);
                        ImageViewCompat.setImageTintList(toiletReviewUpvoted, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey)));
                    }
                }
            });
            db.collection("Upvotes").whereEqualTo("reviewID", reviewRef)
                    .whereEqualTo("userID", profileRef).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        if (!task.getResult().isEmpty()){
                            toiletReviewUpvotes.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
                            ImageViewCompat.setImageTintList(toiletReviewUpvoted, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
                        }
                    }
                }
            });
            db.collection("Upvotes").whereEqualTo("reviewID", reviewRef).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error != null)
                        return;
                    toiletReviewUpvotes.setText(String.valueOf(value.getDocuments().size()));
                }
            });
            toiletReviewUpvoteSection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.collection("Upvotes").whereEqualTo("reviewID", reviewRef)
                            .whereEqualTo("userID", profileRef).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                if (!task.getResult().isEmpty()){
                                    for (QueryDocumentSnapshot doc: task.getResult()){
                                        db.collection("Upvotes").document(doc.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                toiletReviewUpvotes.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey)));
                                                ImageViewCompat.setImageTintList(toiletReviewUpvoted, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey)));
                                            }
                                        });
                                    }

                                }
                                else{
                                    toiletReviewUpvotes.setTextColor(Color.parseColor("#909090"));
                                    Map<String, Object> newUpvote = new HashMap<>();
                                    newUpvote.put("reviewID", reviewRef);
                                    newUpvote.put("userID", profileRef);
                                    db.collection("Upvotes").add(newUpvote).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            toiletReviewUpvotes.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
                                            ImageViewCompat.setImageTintList(toiletReviewUpvoted, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            });
        }

    }

    public void setReviews(ArrayList<Review> reviews){
        this.reviews = reviews;
    }

    public void sortReviews(){
        Collections.sort(this.reviews, new Comparator<Review>(){
            public int compare(Review obj1, Review obj2) {
                // ## Ascending order
//                return obj1.getDate().compareTo(obj2.getDate());
                // ## Descending order
                return obj2.getDate().compareTo(obj1.getDate());
            }
        });
        Collections.sort(this.reviews, new Comparator<Review>(){
            public int compare(Review obj1, Review obj2) {
                // ## Ascending order
//                return obj1.getDate().compareTo(obj2.getDate());
                // ## Descending order
                return obj2.getDate().compareTo(obj1.getDate());
            }
        });
    }
}
