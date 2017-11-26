/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */
package cn.zdx.lib.annotation;

import android.view.View;
import android.view.View.OnClickListener;

import java.lang.reflect.Method;

/**
 * 利用反射方式调用{@link OnClick}绑定的方法
 * */
public class OnAnnotationClickListener implements OnClickListener {
    private Object target;
    private Method method;

    public OnAnnotationClickListener(Object target, Method method) {
        this.target = target;
        this.method = method;
    }

    @Override
    public void onClick(View v) {
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        try {
            method.invoke(target, v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
