/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */

package com.smileapps.mypassword.service;

import com.smileapps.mypassword.model.Password;

/**
 * 비밀번호 변경 리스너
 *
 * @author zeus8502
 */
public interface OnPasswordChangeListener {
    /**
     * 사용자가 새 암호 추가
     */
    void onNewPassword(Password password);

    /**
     * 암호 제거시
     */
    void onDeletePassword(int id);

    /**
     * 비밀번호 유형 변경
     */
    void onUpdatePassword(Password password);
}
