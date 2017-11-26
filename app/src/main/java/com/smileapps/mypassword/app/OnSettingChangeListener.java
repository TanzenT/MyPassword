/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */

package com.smileapps.mypassword.app;

import com.smileapps.mypassword.model.SettingKey;

/**
 * 설정변경리스너
 */
public interface OnSettingChangeListener {
    void onSettingChange(SettingKey key);
}
