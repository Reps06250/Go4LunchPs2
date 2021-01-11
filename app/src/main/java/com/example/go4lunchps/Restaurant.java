package com.example.go4lunchps;

import com.google.android.gms.maps.model.LatLng;

public class Restaurant {

    public LatLng latLng;
    public String name;
    public String vicinity;

    public Restaurant(LatLng latLng, String name, String vicinity) {
        this.latLng = latLng;
        this.name = name;
        this.vicinity = vicinity;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }
}
