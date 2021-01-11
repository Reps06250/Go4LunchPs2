package com.example.go4lunchps.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.go4lunchps.R;
import com.example.go4lunchps.Restaurant;

import java.util.ArrayList;

class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private ArrayList<Restaurant> restaurantArrayList;

    public Adapter(ArrayList<Restaurant> restaurantList){
        restaurantArrayList = restaurantList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.restaurant_name);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_restaurant, parent, false);
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant restaurant = restaurantArrayList.get(position);

        // Set item views based on your views and data model
        TextView textView = holder.nameTextView;
        textView.setText(restaurant.getName());
    }

    @Override
    public int getItemCount() {
        return restaurantArrayList.size();
    }
}
