/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */

package com.smileapps.mypassword.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import com.smileapps.mypassword.R;
import com.smileapps.mypassword.adapter.PasswordGroupAdapter;
import com.smileapps.mypassword.app.BaseActivity;
import com.smileapps.mypassword.dialog.GreatePasswordGroupDialog;
import com.smileapps.mypassword.dialog.UpdatePasswdGroupNameDialog;
import com.smileapps.mypassword.model.PasswordGroup;
import com.smileapps.mypassword.model.SettingKey;
import com.smileapps.mypassword.service.Mainbinder;
import com.smileapps.mypassword.service.OnGetAllPasswordGroupCallback;
import com.smileapps.mypassword.service.OnPasswordGroupChangeListener;

public class PasswordGroupFragment extends Fragment implements OnItemClickListener, OnGetAllPasswordGroupCallback {
    private Mainbinder mainbinder;
    private PasswordGroupAdapter passwordGroupAdapter;
    private OnPasswordGroupSelected onPasswordGroupSelected;

    private OnClickListener onAddClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            GreatePasswordGroupDialog cDialog = new GreatePasswordGroupDialog(getActivity(), mainbinder);
            cDialog.show();
        }
    };

    private OnPasswordGroupChangeListener onPasswordGroupListener = new OnPasswordGroupChangeListener() {
        @Override
        public void onNewPasswordGroup(PasswordGroup passwordGroup) {
            passwordGroupAdapter.addPasswordGroup(passwordGroup);
            if (passwordGroupAdapter.getCount() == 1) {
                selectItem(passwordGroup.getGroupName());
            }
        }

        @Override
        public void onDeletePasswordGroup(String passwordGroupName) {
            boolean result = passwordGroupAdapter.removePasswordGroup(passwordGroupName);
            if (result && passwordGroupName.equals(passwordGroupAdapter.getCurrentGroupName())) {
                String selectedname = "";
                if (passwordGroupAdapter.getCount() > 0)
                    selectedname = passwordGroupAdapter.getItem(0).getGroupName();
                selectItem(selectedname);
            }
        }

        @Override
        public void onUpdateGroupName(String oldGroupName, String newGroupName) {
            int count = passwordGroupAdapter.getCount();
            boolean hasMerge = false;
            for (int i = 0; i < count; i++) {
                PasswordGroup item = passwordGroupAdapter.getItem(i);
                if (item.getGroupName().equals(newGroupName)) {
                    hasMerge = true;
                    break;
                }
            }

            if (hasMerge) {
                // 합쳐진 그룹 존재
                for (int i = 0; i < count; i++) {
                    PasswordGroup item = passwordGroupAdapter.getItem(i);
                    if (item.getGroupName().equals(oldGroupName)) {
                        passwordGroupAdapter.removePasswordGroup(oldGroupName);
                        break;
                    }
                }

            } else {
                /* 현재 그룹 또는 다른 그룹 이름 바꾸기 */
                for (int i = 0; i < count; i++) {
                    PasswordGroup item = passwordGroupAdapter.getItem(i);
                    if (item.getGroupName().equals(oldGroupName)) {
                        item.setGroupName(newGroupName);
                        passwordGroupAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
            if (passwordGroupAdapter.getCurrentGroupName().equals(oldGroupName)
                    || passwordGroupAdapter.getCurrentGroupName().equals(newGroupName)) {
                selectItem(newGroupName);
            }
        }
    };

    private OnItemLongClickListener onDeleteClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            // 그룹이름 롱터치
            final String passwordGroupName = ((PasswordGroup) (parent.getItemAtPosition(position))).getGroupName();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            CharSequence[] items = new String[]{getString(R.string.password_group_update_group_name),
                    getString(R.string.password_group_merge), getString(R.string.password_group_delete_group)};

            builder.setItems(items, new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            // 그룹 이름 수정
                            UpdatePasswdGroupNameDialog updatePasswdGroupName = new UpdatePasswdGroupNameDialog(
                                    getActivity(), passwordGroupName, mainbinder);
                            updatePasswdGroupName.show();
                            break;
                        case 1:
                            mergeGroup(passwordGroupName);
                            break;
                        case 2:
                            // 그룹 삭제
                            showDeleteDialog(passwordGroupName);
                            break;
                    }
                }

            });
            builder.show();
            return true;
        }

    };

    /**
     * 그룹 결합
     *
     * @param passwordGroupName 그룹 이름
     */
    private void mergeGroup(final String passwordGroupName) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.password_group_merge_loading));
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(false);
        progressDialog.show();

        // 그룹 데이터 가져오기
        OnGetAllPasswordGroupCallback onGetAllPasswordGroupCallback = new OnGetAllPasswordGroupCallback() {
            @Override
            public void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups) {
                progressDialog.dismiss();
                // 병합 실패
                if (passwordGroups.size() <= 1) {
                    getBaseActivity().showToast(R.string.password_group_merge_error);
                    return;
                }
                final List<String> items = new ArrayList<String>();
                for (PasswordGroup passwordGroup : passwordGroups) {
                    if (!passwordGroup.getGroupName().equals(passwordGroupName)) {
                        items.add(passwordGroup.getGroupName());
                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(items.toArray(new String[items.size()]),
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newGroupName = items.get(which);
                                mainbinder.updatePasswdGroupName(passwordGroupName, newGroupName);
                            }
                        });
                builder.show();
            }
        };
        // 모든 그룹 로드
        mainbinder.getAllPasswordGroup(onGetAllPasswordGroupCallback);
    }

    /**
     * 그룹 삭제
     *
     * @param passwordGroupName 지울 그룹 이름
     */
    private void showDeleteDialog(final String passwordGroupName) {
        Builder builder = new Builder(getActivity());
        builder.setMessage(getString(R.string.delete_password_group_message, passwordGroupName));
        builder.setTitle(R.string.delete_password_group_title);
        builder.setNeutralButton(R.string.delete_password_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainbinder.deletePasswordgroup(passwordGroupName);
            }
        });
        builder.setNegativeButton(R.string.delete_password_cancle, null);
        builder.show();
    }

    public void setDataSource(Mainbinder mainbinder, OnPasswordGroupSelected onPasswordGroupSelected) {
        this.mainbinder = mainbinder;
        this.onPasswordGroupSelected = onPasswordGroupSelected;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        passwordGroupAdapter = new PasswordGroupAdapter(getActivity());
        mainbinder.registOnPasswordGroupListener(onPasswordGroupListener);
        mainbinder.getAllPasswordGroup(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainbinder.unregistOnPasswordGroupListener(onPasswordGroupListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_password_group, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.fragment_password_group_listView);
        listView.setAdapter(passwordGroupAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(onDeleteClickListener);
        View addView = rootView.findViewById(R.id.fragment_password_group_add);
        addView.setOnClickListener(onAddClickListener);
        return rootView;
    }

    private BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PasswordGroup passwordGroup = passwordGroupAdapter.getItem(position);
        selectItem(passwordGroup.getGroupName());
    }

    @Override
    public void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups) {
        BaseActivity baseActivity = getBaseActivity();
        if (baseActivity != null) {
            String lastGroupName = baseActivity.getSetting(SettingKey.LAST_SHOW_PASSWORDGROUP_NAME,
                    getString(R.string.password_group_default_name));
            passwordGroupAdapter.setCurrentGroupName(lastGroupName);
            passwordGroupAdapter.setData(passwordGroups);
        }
    }

    private void selectItem(String selectedname) {
        BaseActivity baseActivity = getBaseActivity();
        baseActivity.putSetting(SettingKey.LAST_SHOW_PASSWORDGROUP_NAME, selectedname);
        passwordGroupAdapter.setCurrentGroupName(selectedname);
        onPasswordGroupSelected.onPasswordGroupSelected(selectedname);
    }

    public interface OnPasswordGroupSelected {
        void onPasswordGroupSelected(String passwordGroupName);
    }
}
