package com.billy.ppc2;

import android.content.Context;

import com.google.common.util.concurrent.ListenableFuture;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

public class Worker extends ListenableWorker {
    CallbackToFutureAdapter.Completer<Result> mCallback;
    Context context;
    MainActivity mainActivity;

    public Worker(Context context, WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        ListenableFuture<Result> future = CallbackToFutureAdapter.getFuture(completer -> {
            mCallback = completer;
            return null;
        });
        mainActivity.doWork(mCallback);
        return future;
    }
}
