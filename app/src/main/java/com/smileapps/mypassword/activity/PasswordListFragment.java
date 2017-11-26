/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */

package com.smileapps.mypassword.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.twotoasters.jazzylistview.JazzyEffect;
import com.twotoasters.jazzylistview.JazzyHelper;
import com.twotoasters.jazzylistview.JazzyListView;

import java.util.List;

import com.smileapps.mypassword.R;
import com.smileapps.mypassword.adapter.PasswordListAdapter;
import com.smileapps.mypassword.app.BaseFragment;
import com.smileapps.mypassword.app.OnSettingChangeListener;
import com.smileapps.mypassword.model.Password;
import com.smileapps.mypassword.model.SettingKey;
import com.smileapps.mypassword.service.Mainbinder;
import com.smileapps.mypassword.service.OnGetAllPasswordCallback;
import com.smileapps.mypassword.service.OnPasswordChangeListener;

/**
 * 계정정보 UI
 *
 * @author zeus8502
 */
public class PasswordListFragment extends BaseFragment implements OnGetAllPasswordCallback, OnSettingChangeListener,
        android.view.View.OnClickListener {

    /** 데이터 */
    private PasswordListAdapter mainAdapter;

    /** 원본 데이터 소스 */
    private Mainbinder mainbinder;

    private JazzyListView listView;
    /** 데이터가 없는 창 */
    private View noDataView;

    private String passwordGroupName;
    private OnPasswordChangeListener onPasswordListener = new OnPasswordChangeListener() {
        @Override
        public void onNewPassword(Password password) {
            if (password.getGroupName().equals(passwordGroupName)) {
                mainAdapter.onNewPassword(password);
                initView();
            }
        }

        @Override
        public void onDeletePassword(int id) {
            mainAdapter.onDeletePassword(id);
            initView();
        }

        @Override
        public void onUpdatePassword(Password newPassword) {
            mainAdapter.onUpdatePassword(newPassword);
            initView();
        }
    };

    public void setDataSource(Mainbinder mainbinder) {
        this.mainbinder = mainbinder;
    }

    public void showPasswordGroup(String passwordGroupName) {
        this.passwordGroupName = passwordGroupName;
        mainbinder.getAllPassword(this, passwordGroupName);
    }

    public String getPasswordGroupName() {
        return passwordGroupName;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainAdapter = new PasswordListAdapter(getActivity());
        getBaseActivity().getMyApplication().registOnSettingChangeListener(SettingKey.JAZZY_EFFECT, this);
        mainbinder.registOnPasswordListener(onPasswordListener);
        showPasswordGroup(getBaseActivity().getSetting(SettingKey.LAST_SHOW_PASSWORDGROUP_NAME,
                getString(R.string.password_group_default_name)));

    }
    /**
     * 특수효과 : 사용자 지정
     * @return
     */
    private JazzyEffect getJazzyEffect() {
        String strKey = getBaseActivity().getSetting(SettingKey.JAZZY_EFFECT, JazzyHelper.STANDARD + "");
        JazzyEffect jazzyEffect = JazzyHelper.valueOf(Integer.valueOf(strKey));
        return jazzyEffect;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregistOnPasswordListener();
        getBaseActivity().getMyApplication().unregistOnSettingChangeListener(SettingKey.JAZZY_EFFECT, this);
    }

    private void unregistOnPasswordListener() {
        if (mainbinder != null) {
            mainbinder.unregistOnPasswordListener(onPasswordListener);
            mainbinder = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_password_list, container, false);
        listView = (JazzyListView) rootView.findViewById(R.id.main_listview);
        listView.setAdapter(mainAdapter);
        listView.setTransitionEffect(getJazzyEffect());

        noDataView = rootView.findViewById(R.id.main_no_passsword);
        noDataView.setOnClickListener(this);
        if (mainbinder == null) {
            noDataView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        } else {
            initView();
        }

        return rootView;
    }

    private void initView() {
        if (noDataView != null) {
            if (mainAdapter.getCount() == 0) {
                noDataView.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
            } else {
                noDataView.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        listView = null;
        noDataView = null;
    }

    @Override
    public void onGetAllPassword(String groupName, List<Password> passwords) {
        if (passwordGroupName.equals(groupName)) {
            mainAdapter.setPasswordGroup(passwordGroupName);
            mainAdapter.setData(passwords, mainbinder);
            initView();
            if (listView != null)
                listView.setSelection(0);
        }
    }

    @Override
    public void onSettingChange(SettingKey key) {
        if (listView != null && key == SettingKey.JAZZY_EFFECT) {
            listView.setTransitionEffect(getJazzyEffect());
            if (listView.getCount() < 6) {
                showToast(R.string.action_jazzy_effect_toast, Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_no_passsword:
                Intent intent = new Intent(getActivity(), EditPasswordActivity.class);
                intent.putExtra(EditPasswordActivity.PASSWORD_GROUP, passwordGroupName);
                getActivity().startActivity(intent);
                break;
            default:
                break;
        }
    }
}
