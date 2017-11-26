/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */

package com.smileapps.mypassword.service;

import java.util.List;

import com.smileapps.mypassword.model.Password;

public interface OnGetAllPasswordCallback {
    void onGetAllPassword(String froupName, List<Password> passwords);
}
