package com.mobdeve.s15.holyseat;

import android.content.Intent;
import android.media.Rating;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder>{
    private final String TAG = "ReviewAdapter";

    private ArrayList<Review> reviews;
    public ReviewAdapter() {
        this.reviews = new ArrayList<>();
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
}
