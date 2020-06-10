package com.billy.ppc2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private static final String SHARED_PREFERENCES = "location_activity";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private Button setHomeLocationButton;
    private Button unsetHomeLocationButton;
    private BroadcastReceiver broadcast;
    private LocationTracker locationTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTracker = new LocationTracker(this);


        final TextView homeTextView = findViewById(R.id.HomeText);
        unsetHomeLocationButton = findViewById(R.id.clearHomeButton);
        setHomeLocationButton = findViewById(R.id.setHomeButton);

        // Initial load
        final SharedPreferences sp = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        if (sp.contains(LATITUDE) && sp.contains(LONGITUDE)) {
            double latitude = Double.longBitsToDouble(sp.getLong(LATITUDE, Double.doubleToLongBits(0)));
            double longitude = Double.longBitsToDouble(sp.getLong(LONGITUDE, Double.doubleToLongBits(0)));
            homeTextView.setText(String.format("Your home location is:\nLongitude: %s\nLatitude: %s\n", longitude, latitude));
            unsetHomeLocationButton.setVisibility(View.VISIBLE);
        }

        unsetHomeLocationButton.setOnClickListener(v -> {
            sp.edit().clear().apply();
            homeTextView.setText("");
            setHomeLocationButton.setVisibility(View.VISIBLE);
            unsetHomeLocationButton.setVisibility(View.INVISIBLE);
        });

        findViewById(R.id.locationButton).setOnClickListener(v -> {
            boolean hasPermission =
                    (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)) == PackageManager.PERMISSION_GRANTED
                            && (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION)) == PackageManager.PERMISSION_GRANTED;
            if (hasPermission) {
                locationTracker.toggleIsTracking();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]
                                {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_LOCATION_PERMISSION);
            }
        });

        broadcast = new BroadcastReceiver(locationTracker);
        broadcast.forwardTextView(findViewById(R.id.LatTextView));

        registerReceiver(broadcast, new IntentFilter("myAction"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LocationTracker locationTracker = new LocationTracker(MainActivity.this);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationTracker.startTracking();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))) {
                permissionWarningAlert();
            }
        }
    }

    public void permissionWarningAlert() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("yo! hold up dog")
                .setMessage("we need ur low cash hommie")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    class BroadcastReceiver extends android.content.BroadcastReceiver {
        private TextView LatLongAccurText;
        private LocationTracker locationTracker;

        public BroadcastReceiver(LocationTracker locationTracker) {
            this.locationTracker = locationTracker;
        }

        public void forwardTextView(TextView textView) {
            this.LatLongAccurText = textView;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null || !intent.getAction().equals("myAction")) {
                return;
            }

            float accuracy = intent.getFloatExtra("accuracy", 0);
            final double longitude = intent.getDoubleExtra("longitude", 0);
            final double latitude = intent.getDoubleExtra("latitude", 0);

            LatLongAccurText.setText(String.format("Latitude is:\n%s\nLongitude is: %s\nAccuracy is: %s\n",
                    latitude, longitude, accuracy));

            if (locationTracker.isTracking() && accuracy < 50) {
                setHomeLocationButton.setVisibility(View.VISIBLE);
            }
            setHomeLocationButton.setOnClickListener(v -> {
                if (locationTracker.isTracking()) {
                    SharedPreferences sp = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putLong(LATITUDE, Double.doubleToRawLongBits(latitude));
                    editor.putLong(LONGITUDE, Double.doubleToRawLongBits(longitude));
                    editor.apply();
                    ((TextView) findViewById(R.id.HomeText))
                            .setText(String.format("Your home is at:\nLongitude: %s\nLatitude: %s\n", longitude, latitude));
                    setHomeLocationButton.setVisibility(View.INVISIBLE);
                    unsetHomeLocationButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTracker.stopTracking();
        unregisterReceiver(broadcast);
    }
}