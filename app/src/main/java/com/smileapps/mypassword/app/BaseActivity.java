/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */

package com.smileapps.mypassword.app;

import android.app.Activity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import java.lang.reflect.Field;

import com.smileapps.mypassword.model.SettingKey;
import cn.zdx.lib.annotation.FindViewById;
import cn.zdx.lib.annotation.ViewFinder;
import cn.zdx.lib.annotation.XingAnnotationHelper;

public class BaseActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public BaseActivity getActivity() {
        return this;
    }

    public void showToast(int id) {
        showToast(id, Toast.LENGTH_SHORT);
    }

    public void showToast(int id, int duration) {
        Toast.makeText(this, id, duration).show();
    }

    public MyApplication getMyApplication() {
        return (MyApplication) getApplication();
    }

    public String getSetting(SettingKey key, String defValue) {
        return getMyApplication().getSetting(key, defValue);
    }

    public void putSetting(SettingKey key, String value) {
        getMyApplication().putSetting(key, value);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initAnnotation();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        initAnnotation();
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        super.setContentView(view, params);
        initAnnotation();
    }

    private void initAnnotation() {
        ViewFinder viewFinder = ViewFinder.create(this);
        Class<?> clazz = getClass();
        do {
            findView(clazz, viewFinder);
        } while ((clazz = clazz.getSuperclass()) != BaseActivity.class);
    }

    /** 초기화 {@link FindViewById} */
    private void findView(Class<?> clazz, ViewFinder viewFinder) {
        Field[] fields = clazz.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                XingAnnotationHelper.findView(this, field, viewFinder);
            }
        }
    }
}
