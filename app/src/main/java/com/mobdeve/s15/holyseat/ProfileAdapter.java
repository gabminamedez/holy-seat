package com.mobdeve.s15.holyseat;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.CollectionUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final String TAG = "ProfileAdapter";

    private ArrayList<Activity> activities;
    private ArrayList<Activity> activitiesCopy;
    public ProfileAdapter(){
        this.activities = new ArrayList<>();
        this.activitiesCopy = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (activities.get(position) instanceof Review)
            return 0;
        else
            return 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) { // review
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.profilereview_layout,
                parent, false);
            ReviewViewHolder reviewViewHolder = new ReviewViewHolder(itemView);
            reviewViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            return reviewViewHolder;
        }
        else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.profilecheckin_layout,
                parent, false);
            CheckInViewHolder checkInViewHolder = new CheckInViewHolder(itemView);
            checkInViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            return checkInViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == 0) {
            ReviewViewHolder viewHolder = (ReviewViewHolder) holder;
            ((ReviewViewHolder) viewHolder).bindReview((Review) activities.get(position));
        }
        else{
            CheckInViewHolder viewHolder = (CheckInViewHolder) holder;
            ((CheckInViewHolder) viewHolder).bindCheckIn((CheckIn) activities.get(position));
        }
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {

        private TextView profileReviewUser;
        private TextView profileReviewToilet;
        private RatingBar profileReviewRating;
        private TextView profileReviewDetails;
        private TextView profileReviewDate;

        public ReviewViewHolder(View itemView){
            super(itemView);
            this.profileReviewUser = itemView.findViewById(R.id.profileReviewUser);
            this.profileReviewToilet = itemView.findViewById(R.id.profileReviewToilet);
            this.profileReviewRating = itemView.findViewById(R.id.profileReviewRating);
            this.profileReviewDetails = itemView.findViewById(R.id.profileReviewDetails);
            this.profileReviewDate = itemView.findViewById(R.id.profileReviewDate);
        }
        public void bindReview(Review review) {
            this.profileReviewUser.setText(review.getReviewerName());
            this.profileReviewToilet.setText(String.valueOf(review.getToiletLocation()));
            this.profileReviewRating.setRating(review.getRating());
            this.profileReviewDetails.setText(review.getDetails());
            this.profileReviewDate.setText(review.getPostedString());
        }
    }


    class CheckInViewHolder extends RecyclerView.ViewHolder {

        private TextView profileCheckInUser;
        private TextView profileCheckInToilet;
        private TextView profileCheckInDate;

        public CheckInViewHolder(View itemView){
            super(itemView);
            this.profileCheckInUser = itemView.findViewById(R.id.profileCheckinUser);
            this.profileCheckInToilet = itemView.findViewById(R.id.profileCheckinToilet);
            this.profileCheckInDate = itemView.findViewById(R.id.profileCheckinDate);
        }
        public void bindCheckIn(CheckIn checkIn) {
            this.profileCheckInUser.setText(checkIn.getUserName());
            this.profileCheckInToilet.setText(String.valueOf(checkIn.getToiletLocation()));
            this.profileCheckInDate.setText(checkIn.getCheckedString());
        }
    }

    public void addReviewsAll(ArrayList<Review> reviews){
        this.activities.addAll(reviews);
        this.activitiesCopy.addAll(reviews);
    }
    public void addCheckInsAll(ArrayList<CheckIn> checkIns){
        this.activities.addAll(checkIns);
        this.activitiesCopy.addAll(checkIns);
    }
    public void filterReviews(ArrayList<Review> reviews){
        this.activities.clear();
        this.activities.addAll(reviews);
    }
    public void filterCheckIns(ArrayList<CheckIn> checkIns){
        this.activities.clear();
        this.activities.addAll(checkIns);
    }

    public void sortActivities(){
        Collections.sort(this.activities, new Comparator<Activity>(){
            public int compare(Activity obj1, Activity obj2) {
                // ## Ascending order
//                return obj1.getDate().compareTo(obj2.getDate());
                // ## Descending order
                return obj2.getDate().compareTo(obj1.getDate());
            }
        });
        Collections.sort(this.activitiesCopy, new Comparator<Activity>(){
            public int compare(Activity obj1, Activity obj2) {
                // ## Ascending order
//                return obj1.getDate().compareTo(obj2.getDate());
                // ## Descending order
                return obj2.getDate().compareTo(obj1.getDate());
            }
        });
    }

    public void filterAll(){
        this.activities = (ArrayList<Activity>) activitiesCopy.clone();
    }

    public ArrayList<Activity> getActivities() {
        return activitiesCopy;
    }
}
