package com.example.go4lunchps.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.go4lunchps.R;
import com.example.go4lunchps.Restaurant;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class ListViewFragment extends Fragment {

    private MapViewModel mapViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
        View root = inflater.inflate(R.layout.fragment_listview, container, false);
        RecyclerView rv = (RecyclerView) root.findViewById(R.id.rv);

        Adapter adapter = new Adapter(mapViewModel.getRestaurantMutableLiveData().getValue());
        // Attach the adapter to the recyclerview to populate items
        rv.setAdapter(adapter);
        // Set layout manager to position the items
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        mapViewModel.getRestaurantMutableLiveData().observe(getViewLifecycleOwner(), new Observer<ArrayList<Restaurant>>() {
            @Override
            public void onChanged(@Nullable ArrayList<Restaurant> restaurantsList) {

            }
        });
        return root;
    }
}