package com.smileapps.mypassword.service;

import com.smileapps.mypassword.model.PasswordGroup;

/**
 * 비밀번호 그룹 변경 리스너
 *
 * @author zeus8502
 */
public interface OnPasswordGroupChangeListener {
    /**
     * 새 암호 추가
     */
    void onNewPasswordGroup(PasswordGroup passwordGroup);
    /**
     * 새 그룹 추가
     */
    void onDeletePasswordGroup(String passwordGroupName);

    void onUpdateGroupName(String oldGroupName, String newGroupName);
}
