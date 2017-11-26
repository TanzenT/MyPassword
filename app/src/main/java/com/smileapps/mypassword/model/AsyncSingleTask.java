/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */

package com.smileapps.mypassword.model;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AsyncSingleTask<D> {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    private AsyncResult<D> asyncResult;
    private boolean isRunned = false;
    private int delay = 0;
    private Runnable mainThreadRunable = new Runnable() {
        @Override
        public void run() {
            runOnUIThread(asyncResult);
        }
    };
    private Runnable backgroundRunable = new Runnable() {
        @Override
        public void run() {
            asyncResult = doInBackground(new AsyncResult<D>());
            handler.postDelayed(mainThreadRunable, delay);
        }
    };

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public synchronized void execute() {
        if (isRunned)
            throw new RuntimeException("작업이 이미 실행됨.");

        isRunned = true;
        executorService.execute(backgroundRunable);
    }

    protected abstract AsyncResult<D> doInBackground(AsyncResult<D> asyncResult);

    protected void runOnUIThread(AsyncResult<D> asyncResult) {
    }
}
