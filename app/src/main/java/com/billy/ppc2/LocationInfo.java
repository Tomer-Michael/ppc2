package com.billy.ppc2;

public class LocationInfo {
    private double latitude;
    private double longitude;
    private float accuracy;

    public LocationInfo( double latitude, double longitude, float accuracy){
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
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

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }
}
