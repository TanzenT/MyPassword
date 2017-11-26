/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */

package com.smileapps.mypassword.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smileapps.mypassword.model.SettingKey;

public class MyApplication extends Application implements OnSharedPreferenceChangeListener {
    /** 설정값 */
    private SharedPreferences sharedPreferences;
    private Map<SettingKey, List<OnSettingChangeListener>> onSettingChangeListenerMap = new HashMap<SettingKey, List<OnSettingChangeListener>>();

    @Override
    public void onCreate() {
        super.onCreate();
        loadSettings();
    }

    private void loadSettings() {
        sharedPreferences = getSharedPreferences("settings", Context.MODE_MULTI_PROCESS);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * 설정 불러오기
     *
     * @param key      설정key
     * @param defValue 설정의 기본값
     * @return
     */
    public String getSetting(SettingKey key, String defValue) {
        return sharedPreferences.getString(key.name(), defValue);
    }

    public void putSetting(SettingKey key, String value) {
        sharedPreferences.edit().putString(key.name(), value).commit();
    }

    public void registOnSettingChangeListener(SettingKey key, OnSettingChangeListener onSettingChangeListener) {
        checkUIThread();

        List<OnSettingChangeListener> onSettingChangeListeners;
        if (onSettingChangeListenerMap.containsKey(key)) {
            onSettingChangeListeners = onSettingChangeListenerMap.get(key);
        } else {
            onSettingChangeListeners = new ArrayList<OnSettingChangeListener>();
            onSettingChangeListenerMap.put(key, onSettingChangeListeners);
        }
        onSettingChangeListeners.add(onSettingChangeListener);
    }

    public void unregistOnSettingChangeListener(SettingKey key, OnSettingChangeListener onSettingChangeListener) {
        checkUIThread();
        if (onSettingChangeListenerMap.containsKey(key)) {
            List<OnSettingChangeListener> onSettingChangeListeners = onSettingChangeListenerMap.get(key);
            onSettingChangeListeners.remove(onSettingChangeListener);
            if (onSettingChangeListeners.size() == 0) {
                onSettingChangeListenerMap.remove(key);
            }
        }
    }

    /**
     * 현재 버전 정보 가져오기
     */
    public String getVersionName() {
        PackageManager packageManager = getPackageManager();
        PackageInfo packInfo;
        String version = "";
        try {
            packInfo = packageManager.getPackageInfo(getPackageName(), 0);
            version = packInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 버전 코드 가져오기
     */
    public int getVersionCode() {
        PackageManager packageManager = getPackageManager();
        PackageInfo packInfo;
        int versionCode = 0;
        try {
            packInfo = packageManager.getPackageInfo(getPackageName(), 0);
            versionCode = packInfo.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    private void checkUIThread() {
        if (!isRunOnUIThread())
            throw new RuntimeException("메인쓰레드호출");
    }

    /**
     * 현재 쓰레드가 메인인지 확인
     *
     * @return
     */
    private boolean isRunOnUIThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SettingKey settingKey = SettingKey.valueOf(SettingKey.class, key);
        List<OnSettingChangeListener> onSettingChangeListeners = onSettingChangeListenerMap.get(settingKey);
        if (onSettingChangeListeners != null) {
            for (OnSettingChangeListener onSettingChangeListener : onSettingChangeListeners) {
                onSettingChangeListener.onSettingChange(settingKey);
            }
        }
    }
}
