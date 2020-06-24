package com.billy.ppc2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class LocalSendSmsBroadcastReceiver extends BroadcastReceiver {
    private String phoneNumber;
    public final String CONTENT = "content";
    public final String PHONE = "phone";
    public final String TAG = "LocalSendSmsBroadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "permission is not allowed");
            return;
        }
        if (intent == null || intent.getAction() == null || !intent.getAction().equals("POST_PC.ACTION_SEND_SMS")) {
            return;
        }

        String message = intent.getStringExtra(CONTENT);
        phoneNumber = intent.getStringExtra(PHONE);
        if ((message != null) && (phoneNumber != null) && (message.length() > 0 && phoneNumber.length() > 0)) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            new MyNotification("sending sms to " + phoneNumber + ":" + message, context);
        } else {
            Log.e(TAG, "there is an error");
        }
    }

    public void sendMessage() {
        String message = "Ya cow, I'm home!";
        SmsManager manager = SmsManager.getDefault();
        manager.sendTextMessage(phoneNumber, null, message, null, null);
    }
}
