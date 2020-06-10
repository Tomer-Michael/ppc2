package com.billy.ppc2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import androidx.core.app.ActivityCompat;

class LocationTracker {
    private static final String TAG = LocationTracker.class.getSimpleName();
    private static final long INTERVAL_UPDATE = 10000;
    private static final long FASTEST_INTERVAL = 3000;
    private Context context;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isTracking;

    public LocationTracker(Context context) {
        this.context = context;
        fusedLocationClient = new FusedLocationProviderClient(context);
    }

    public void setTracking(boolean isTracking) {
        this.isTracking = isTracking;
    }

    public void toggleIsTracking() {
        this.isTracking = !isTracking;
        if (isTracking) {
            startTracking();
        } else {
            stopTracking();
        }
    }

    public boolean isTracking() {
        return this.isTracking;
    }

    public void startTracking() {
        boolean hasPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
        if (hasPermission) {
            locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setFastestInterval(FASTEST_INTERVAL);
            locationRequest.setInterval(INTERVAL_UPDATE);
            RequestLocationsUpdates();
        } else {
            Log.e(TAG, "user is bad person");
        }
    }

    @SuppressLint("MissingPermission")
    public void RequestLocationsUpdates() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Intent locationChanged = new Intent("myAction");
                locationChanged.putExtra("latitude", locationResult.getLastLocation().getLongitude());
                locationChanged.putExtra("longitude", locationResult.getLastLocation().getLatitude());
                locationChanged.putExtra("accuracy", locationResult.getLastLocation().getAccuracy());
                context.sendBroadcast(locationChanged);
            }
        };
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        locationCallback = null;
    }
}
