package com.mobdeve.s15.holyseat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder>{
    private ArrayList<Review> reviews;
    public ReviewAdapter(ArrayList<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_toilet, parent, false);
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
//        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView profile;
        private TextView displayName, username, date, tweetDetails, likes;
        public MyViewHolder(View itemView) {
            super(itemView);
        }
        public void bind(Review review) {
        }
    }
}
