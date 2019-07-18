package com.smileapps.mypassword.service;

import java.util.List;

import com.smileapps.mypassword.model.PasswordGroup;

public interface OnGetAllPasswordGroupCallback {
    void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups);
}
