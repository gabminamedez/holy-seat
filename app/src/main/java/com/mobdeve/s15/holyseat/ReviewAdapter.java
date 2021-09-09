package com.mobdeve.s15.holyseat;

import android.content.Context;
import android.content.Intent;
import android.media.Rating;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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

    public ReviewAdapter(Context context) {
        this.reviews = new ArrayList<>();
        this.context = context;
        this.sp = PreferenceManager.getDefaultSharedPreferences(context);;
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
                Intent i = new Intent(v.getContext(), ReviewActivity.class);
                i.putExtra(ReviewActivity.REVIEW_KEY, reviews.get(holder.getBindingAdapterPosition()).getId());
                i.putExtra(ToiletActivity.TOILET_KEY, reviews.get(holder.getBindingAdapterPosition()).getToiletID().getId());
                v.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView toiletReviewUser, toiletReviewUpvotes, toiletReviewDetails, toiletReviewDate;
        private RatingBar toiletReviewRating;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.toiletReviewUser = itemView.findViewById(R.id.toiletReviewUser);
            this.toiletReviewUpvotes = itemView.findViewById(R.id.toiletReviewUpvotes);
            this.toiletReviewDetails = itemView.findViewById(R.id.toiletReviewDetails);
            this.toiletReviewDate = itemView.findViewById(R.id.toiletReviewDate);
            this.toiletReviewRating = itemView.findViewById(R.id.toiletReviewRating);
            toiletReviewUser.setOnClickListener(this);
        }
        public void bind(Review review) {
            this.toiletReviewUser.setText(review.getReviewerName());
            this.toiletReviewUpvotes.setText(String.valueOf(review.getNumUpvotes()));
            this.toiletReviewDetails.setText(review.getDetails());
            this.toiletReviewDate.setText(review.getPostedString());
            this.toiletReviewRating.setRating(review.getRating());
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
            DocumentReference reviewRef = db.collection("Reviews").document(review.getId());
            DocumentReference profileRef = db.collection("Users").document(sp.getString(ProfileActivity.PROFILE_KEY, ""));
            db.collection("Upvotes").whereEqualTo("reviewID", reviewRef).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        System.out.println(task.getResult().size());
                        System.out.println(review.getId());
                        toiletReviewUpvotes.setText(String.valueOf(task.getResult().size()));
                    }
                }
            });
            db.collection("Upvotes").whereEqualTo("reviewID", reviewRef)
                    .whereEqualTo("userID", profileRef).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        if (!task.getResult().isEmpty()){
                            toiletReviewUpvotes.setTextColor(Color.parseColor("#FF7557"));
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
            toiletReviewUpvotes.setOnClickListener(new View.OnClickListener() {
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
                                                toiletReviewUpvotes.setTextColor(Color.parseColor("#909090"));
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
                                            toiletReviewUpvotes.setTextColor(Color.parseColor("#FF7557"));
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (v == toiletReviewUser) {
                Intent i = new Intent(v.getContext(), ProfileActivity.class);
                i.putExtra(ProfileActivity.PROFILE_KEY, reviews.get(this.getBindingAdapterPosition()).getReviewerID().getId());
                v.getContext().startActivity(i);
            }
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
