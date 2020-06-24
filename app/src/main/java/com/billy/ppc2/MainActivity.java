package com.billy.ppc2;

import static android.view.View.VISIBLE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ListenableWorker;

public class MainActivity extends AppCompatActivity {
    static final String SHARED_PREFERENCES = "location_activity";
    private static final String PHONE_NUMBER = "phone_number";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private static final int REQUEST_CODE_SEND_SMS = 0;
    private static final float METERS_ALLOWED = 50;
    private Button setHomeLocationButton;
    private Button testSMSButton;
    private Button setSMSButton;
    private Button deleteNumber;
    private Button unsetHomeLocationButton;
    private BroadcastReceiver broadcast;
    private LocationTracker locationTracker;
    private String myNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTracker = new LocationTracker(this);

        final TextView homeTextView = findViewById(R.id.HomeText);
        unsetHomeLocationButton = findViewById(R.id.clearHomeButton);
        setHomeLocationButton = findViewById(R.id.setHomeButton);
        testSMSButton = findViewById(R.id.testSMSButton);
        deleteNumber = findViewById(R.id.deletePhoneNumber);
        setSMSButton = findViewById(R.id.setSMSButton);

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

        TextView trackingToggler = findViewById(R.id.locationButton);
        trackingToggler.setOnClickListener(v -> {
            boolean hasPermission =
                    (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)) == PackageManager.PERMISSION_GRANTED
                            && (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION)) == PackageManager.PERMISSION_GRANTED;
            if (hasPermission) {
                locationTracker.toggleIsTracking();
                String text = locationTracker.isTracking() ? "Stop Tracking" : "Start Tracking";
                trackingToggler.setText(text);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]
                                {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_LOCATION_PERMISSION);
            }
        });

        setSMSButton.setOnClickListener(v -> {
            int permissionCheck = ContextCompat.checkSelfPermission
                    (MainActivity.this, Manifest.permission.SEND_SMS);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                setNumber();
            } else {
                ActivityCompat
                        .requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_CODE_SEND_SMS);
            }
        });

        spCheck();

        deleteNumber.setOnClickListener(v -> {
            sp.edit().remove(PHONE_NUMBER).apply();
            deleteNumber.setVisibility(View.INVISIBLE);
            testSMSButton.setVisibility(View.INVISIBLE);
        });

        testSMSButton.setOnClickListener(v -> {
            Intent smsIntent = new Intent("POST_PC.ACTION_SEND_SMS");
            smsIntent.putExtra("content", "Honey I'm Sending a Test Message!");
            smsIntent.putExtra("phone", myNumber);
            sendBroadcast(smsIntent);
        });

        broadcast = new BroadcastReceiver(locationTracker);
        broadcast.forwardTextView(findViewById(R.id.LatTextView));

        registerReceiver(broadcast, new IntentFilter("myAction"));
    }

    private void spCheck() {
        SharedPreferences sp = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        if (sp.contains(PHONE_NUMBER)) {
            deleteNumber.setVisibility(VISIBLE);
            testSMSButton.setVisibility(VISIBLE);
        }
    }

    private void setNumber() {
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this);

        EditText phoneEditText = new EditText(this);

        alert.setTitle("please enter your phone number");
        alert.setView(phoneEditText);
        alert.setPositiveButton("OK", (dialog, whichButton) -> {
            myNumber = phoneEditText.getText().toString();
            SharedPreferences sp = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(PHONE_NUMBER, myNumber);
            editor.apply();
            testSMSButton.setVisibility(VISIBLE);
            deleteNumber.setVisibility(VISIBLE);
            dialog.dismiss();
        });
        alert.setNegativeButton("cancel", null);
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LocationTracker locationTracker = new LocationTracker(MainActivity.this);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationTracker.startTracking();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                            (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))) {
                        permissionWarningAlert();
                    }
                }
            case REQUEST_CODE_SEND_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setNumber();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                        Toast.makeText(this, "please allow us to send sms", Toast.LENGTH_SHORT).show();
                    }
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

    public boolean doWork(CallbackToFutureAdapter.Completer<ListenableWorker.Result> mCallback) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                mCallback.set(ListenableWorker.Result.success());
            }
        }
        SharedPreferences sp = this.getSharedPreferences("location_activity", MODE_PRIVATE);
        if (sp.contains("longitude") && sp.contains("latitude") && sp.contains("phone_number")) {
            LocationTracker locationTracker = new LocationTracker(getApplicationContext());
            locationTracker.startTracking();
            if (broadcast.accuracy < METERS_ALLOWED) {
                double previousLatitude = Double.longBitsToDouble(sp.getLong("latitude", Double.doubleToLongBits(0)));
                double previousLongitude = Double.longBitsToDouble(sp.getLong("longitude", Double.doubleToLongBits(0)));
                if (previousLatitude != 0 && previousLongitude != 0) {
                    if ((Math.abs(broadcast.latitude - previousLatitude) > METERS_ALLOWED) &&
                            (Math.abs(broadcast.longitude - previousLongitude) > METERS_ALLOWED)) {
                        saveInfoToSp(broadcast.longitude, broadcast.latitude);
                        if ((Math.abs(broadcast.latitude - broadcast.hommieLat) < METERS_ALLOWED) &&
                                (Math.abs(broadcast.longitude - broadcast.hommieLot) < METERS_ALLOWED)) {
                            LocalSendSmsBroadcastReceiver localSendSmsBroadcastReceiver = new LocalSendSmsBroadcastReceiver();
                            localSendSmsBroadcastReceiver.sendMessage();
                        }
                        return mCallback.set(ListenableWorker.Result.success());
                    }
                }
            }
        } else {
            mCallback.set(ListenableWorker.Result.success());
        }

        return false;
    }

    private void saveInfoToSp(double currentLongitude, double currentLatitude) {
        SharedPreferences sp = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putLong(LATITUDE, Double.doubleToRawLongBits(currentLatitude));
        editor.putLong(LONGITUDE, Double.doubleToRawLongBits(currentLongitude));

        editor.apply();
    }

    class BroadcastReceiver extends android.content.BroadcastReceiver {
        private TextView LatLongAccurText;
        private LocationTracker locationTracker;
        private float accuracy;
        double longitude;
        double latitude;
        double hommieLat;
        double hommieLot;

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

            accuracy = intent.getFloatExtra("accuracy", 0);
            longitude = intent.getDoubleExtra("longitude", 0);
            latitude = intent.getDoubleExtra("latitude", 0);

            LatLongAccurText.setText(String.format("Latitude is: %s\nLongitude is: %s\nAccuracy is: %s\n",
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
                    hommieLat = latitude;
                    hommieLot = longitude;
                    ((TextView) findViewById(R.id.HomeText))
                            .setText(String.format("Your home is at:\nLongitude: %s\nLatitude: %s\n", longitude, latitude));
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