package com.billy.ppc2;

import java.util.concurrent.TimeUnit;

import android.content.IntentFilter;

import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class TomerIsAnApplication extends android.app.Application {
    private CallbackToFutureAdapter.Completer<ListenableWorker.Result> mCallback;
    private LocalSendSmsBroadcastReceiver broadcastSms;

    public void onCreate() {
        super.onCreate();
        broadcastSms = new LocalSendSmsBroadcastReceiver();
        registerReceiver(broadcastSms, new IntentFilter("POST_PC.ACTION_SEND_SMS"));

        PeriodicWorkRequest workerTask = new PeriodicWorkRequest.Builder(Worker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).
                enqueueUniquePeriodicWork("worker", ExistingPeriodicWorkPolicy.REPLACE, workerTask);
    }
}
