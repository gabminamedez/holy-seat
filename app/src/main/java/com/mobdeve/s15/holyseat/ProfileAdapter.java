package com.mobdeve.s15.holyseat;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.MyViewHolder> {
    private ArrayList<Review> reviews;
    private ArrayList<CheckIn> checkIns;
    public ProfileAdapter(ArrayList<Review> reviews, ArrayList<CheckIn> checkIns) {
        this.reviews = reviews;
        this.checkIns = checkIns;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.profilecheckin_layout, parent, false);
        View itemView2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.profilereview_layout, parent, false);
        MyViewHolder myViewHolder1 = new MyViewHolder(itemView1);
        MyViewHolder myViewHolder2 = new MyViewHolder(itemView2);
        myViewHolder1.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        return myViewHolder1;
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileAdapter.MyViewHolder holder, int position) {
//        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews.size() + checkIns.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView profile;
        private TextView displayName, username, date, tweetDetails, likes;
        public MyViewHolder(View itemView) {
            super(itemView);
        }
        public void bindReview(Review review) {
        }
        public void bindCheckIn(CheckIn checkIn) {
        }
    }
}
