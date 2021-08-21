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


public class ToiletListAdapter extends RecyclerView.Adapter<ToiletListAdapter.MyViewHolder> {
    private ArrayList<Toilet> toilets;
    public ToiletListAdapter() {
        this.toilets = new ArrayList<>();
    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.toiletlist_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(itemView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ToiletListAdapter.MyViewHolder holder, int position) {
        holder.bindToilet(toilets.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), ToiletActivity.class);
                i.putExtra(ToiletActivity.TOILET_KEY, toilets.get(holder.getBindingAdapterPosition()).getId());
                view.getContext().startActivity(i);
            }
        });
    }

    public void setToilets(ArrayList<Toilet> toilets){
        this.toilets = toilets;
    }

    @Override
    public int getItemCount() {
        return toilets.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView toiletListImg;
        private TextView toiletListName, toiletListRating, toiletListRoomType, toiletListToiletType, toiletListReviews, toiletListCheckins;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.toiletListImg = itemView.findViewById(R.id.toiletListImg);;
            this.toiletListName = itemView.findViewById(R.id.toiletListName);
            this.toiletListRating = itemView.findViewById(R.id.toiletListRating);
            this.toiletListRoomType = itemView.findViewById(R.id.toiletListRoomType);
            this.toiletListToiletType = itemView.findViewById(R.id.toiletListToiletType);
            this.toiletListReviews = itemView.findViewById(R.id.toiletListReviews);
            this.toiletListCheckins = itemView.findViewById(R.id.toiletListCheckins);
        }
        public void bindToilet(Toilet toilet) {
//            this.toiletListImg.setImageResource(toilet.get);
            this.toiletListName.setText(toilet.getLocation());
            this.toiletListRating.setText(String.valueOf(toilet.getAvgRating()));
            this.toiletListRoomType.setText(toilet.getRoomType());
            this.toiletListToiletType.setText(toilet.getToiletType());
            this.toiletListReviews.setText(String.valueOf(toilet.getNumReviews()));
            this.toiletListCheckins.setText(String.valueOf(toilet.getNumCheckins()));

        }
    }
}
