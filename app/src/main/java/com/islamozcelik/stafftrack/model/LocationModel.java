package com.islamozcelik.stafftrack.model;

import com.google.android.gms.maps.model.LatLng;

public class LocationModel {

    private LatLng location;
    private double latitude;
    private double longitude;
    private String time;

    public LocationModel(){

    }
    public LocationModel(Double latitude,Double longitude,String time){
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }
    public LocationModel(LatLng location,String time){
        this.location = location;
        this.time = time;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
