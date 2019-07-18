package com.smileapps.mypassword.app;

import com.smileapps.mypassword.model.SettingKey;

/**
 * 설정변경리스너
 */
public interface OnSettingChangeListener {
    void onSettingChange(SettingKey key);
}
