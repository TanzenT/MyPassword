package com.smileapps.mypassword.service;

import java.util.List;

import com.smileapps.mypassword.model.Password;

public interface OnGetAllPasswordCallback {
    void onGetAllPassword(String froupName, List<Password> passwords);
}
