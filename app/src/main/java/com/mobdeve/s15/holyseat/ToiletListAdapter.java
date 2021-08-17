package com.mobdeve.s15.holyseat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class ToiletListAdapter extends RecyclerView.Adapter<ToiletListAdapter.MyViewHolder> {
    private ArrayList<Toilet> toilets;
    public ToiletListAdapter(ArrayList<Toilet> toilets) {
        this.toilets = toilets;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.toiletlist_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(itemView);
        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ToiletListAdapter.MyViewHolder holder, int position) {
//        holder.bind(toilets.get(position));
    }

    @Override
    public int getItemCount() {
        return toilets.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView profile;
        private TextView displayName, username, date, tweetDetails, likes;
        public MyViewHolder(View itemView) {
            super(itemView);
        }
        public void bindToilet(Toilet toilet) {
        }
    }
}
