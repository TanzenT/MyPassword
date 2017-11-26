/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */

package com.smileapps.mypassword.service.noti;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.smileapps.mypassword.R;
import com.smileapps.mypassword.activity.EditPasswordActivity;

public class QuickAddService extends Service {
    NotificationManager Notifi_M;
    NotiServiceThread thread;
    Notification Notifi;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler handler = new myServiceHandler();
        thread = new NotiServiceThread(handler);
        thread.start();
        return START_STICKY;
    }

    //서비스가 종료될 때 할 작업

    public void onDestroy() {
        thread.stopForever();
        thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
    }

    class myServiceHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            Intent intent = new Intent(QuickAddService.this, EditPasswordActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(QuickAddService.this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);

            Notifi = new Notification.Builder(getApplicationContext())
                    .setContentTitle("퀵에드 서비스가 실행중입니다.")
                    .setContentText("MyPassword")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setTicker("MyPassword")
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.PRIORITY_MAX)
                    .build();

            //소리추가
            //Notifi.defaults = Notification.DEFAULT_SOUND;

            //알림 소리를 한번만 내도록
            Notifi.flags = Notification.FLAG_ONLY_ALERT_ONCE;

            //확인하면 자동으로 알림이 제거 되지않도록
            Notifi.flags = Notification.FLAG_NO_CLEAR;
            Notifi_M.notify(777 , Notifi);
            startForeground(777, Notifi);
        }
    };
}