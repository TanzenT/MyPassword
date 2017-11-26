/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */

package com.smileapps.mypassword.service;

import android.content.Context;
import android.os.Binder;

import java.util.ArrayList;
import java.util.List;

import com.smileapps.mypassword.app.MyApplication;
import com.smileapps.mypassword.app.OnSettingChangeListener;
import com.smileapps.mypassword.database.PasswordDatabase;
import com.smileapps.mypassword.model.AsyncResult;
import com.smileapps.mypassword.model.AsyncSingleTask;
import com.smileapps.mypassword.model.Password;
import com.smileapps.mypassword.model.PasswordGroup;
import com.smileapps.mypassword.model.SettingKey;
import com.smileapps.mypassword.service.task.GetAllPasswordTask;

public class Mainbinder extends Binder {
    private MyApplication myApplication;
    private PasswordDatabase passwordDatabase;

    /** 비밀번호 변경 리스너 */
    private List<OnPasswordChangeListener> onPasswordListeners = new ArrayList<OnPasswordChangeListener>();

    /** 암호 그룹 변경 리스너 */
    private List<OnPasswordGroupChangeListener> onPasswordGroupListeners = new ArrayList<OnPasswordGroupChangeListener>();

    private OnSettingChangeListener onSettingChangeListener = new OnSettingChangeListener() {
        @Override
        public void onSettingChange(SettingKey key) {
            // 사용자 암호 변경
            encodePasswd(myApplication.getSetting(SettingKey.LOCK_PATTERN, "[]"));
        }
    };

    public Mainbinder(Context context, MyApplication myApplication) {
        passwordDatabase = new PasswordDatabase(context);
        this.myApplication = myApplication;
        final String passwd = myApplication.getSetting(SettingKey.LOCK_PATTERN, "[]");
        myApplication.registOnSettingChangeListener(SettingKey.LOCK_PATTERN, onSettingChangeListener);
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                passwordDatabase.setCurrentPasswd(passwd);
                passwordDatabase.getWritableDatabase();
                return asyncResult;
            }
        }.execute();
    }

    /** 암호화 */
    private void encodePasswd(final String newPasswd) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                passwordDatabase.encodePasswd(newPasswd);
                return asyncResult;
            }
        }.execute();
    }

    void onDestroy() {
        passwordDatabase.close();
        myApplication.unregistOnSettingChangeListener(SettingKey.LOCK_PATTERN, onSettingChangeListener);
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                onPasswordListeners.clear();
            }
        }.execute();
    }

    public void registOnPasswordGroupListener(final OnPasswordGroupChangeListener onPasswordGroupListener) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                onPasswordGroupListeners.add(onPasswordGroupListener);
            }
        }.execute();
    }

    public void unregistOnPasswordGroupListener(final OnPasswordGroupChangeListener onPasswordGroupListener) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                onPasswordGroupListeners.remove(onPasswordGroupListener);
            }
        }.execute();
    }

    public void registOnPasswordListener(final OnPasswordChangeListener onPasswordListener) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                onPasswordListeners.add(onPasswordListener);
            }
        }.execute();
    }

    public void unregistOnPasswordListener(final OnPasswordChangeListener onPasswordListener) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                onPasswordListeners.remove(onPasswordListener);
            }
        }.execute();
    }

    public void getAllPassword(OnGetAllPasswordCallback onGetAllPasswordCallback, String groupName) {
        GetAllPasswordTask getAllPasswordTask = new GetAllPasswordTask(passwordDatabase, onGetAllPasswordCallback,
                groupName);
        getAllPasswordTask.execute();
    }

    public void getAllPassword(OnGetAllPasswordCallback onGetAllPasswordCallback) {
        GetAllPasswordTask getAllPasswordTask = new GetAllPasswordTask(passwordDatabase, onGetAllPasswordCallback, null);
        getAllPasswordTask.execute();
    }

    public void deletePassword(final int id) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                int result = passwordDatabase.deletePasssword(id);
                asyncResult.setResult(result);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                for (OnPasswordChangeListener onPasswordListener : onPasswordListeners) {
                    onPasswordListener.onDeletePassword(id);
                }
            }
        }.execute();
    }

    public void getPassword(final int id, final OnGetPasswordCallback onGetPasswordCallback) {
        new AsyncSingleTask<Password>() {
            @Override
            protected AsyncResult<Password> doInBackground(AsyncResult<Password> asyncResult) {
                Password password = passwordDatabase.getPassword(id);
                asyncResult.setData(password);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Password> asyncResult) {
                onGetPasswordCallback.onGetPassword(asyncResult.getData());
            }
        }.execute();
    }

    public void updatePassword(final Password password) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                int result = passwordDatabase.updatePassword(password);
                asyncResult.setResult(result);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                for (OnPasswordChangeListener onPasswordListener : onPasswordListeners) {
                    onPasswordListener.onUpdatePassword(password);
                }
            }
        }.execute();
    }

    public void insertPassword(final Password password) {
        new AsyncSingleTask<Password>() {
            @Override
            protected AsyncResult<Password> doInBackground(AsyncResult<Password> asyncResult) {
                String newGroupName = password.getGroupName();

                /** 새 그룹 인지 확인 */
                boolean isNew = true;
                List<PasswordGroup> passwordGroups = passwordDatabase.getAllPasswordGroup();
                for (int i = 0; i < passwordGroups.size(); i++) {
                    PasswordGroup passwordGroup = passwordGroups.get(i);
                    if (passwordGroup.getGroupName().equals(newGroupName)) {
                        isNew = false;
                        break;
                    }
                }

                if (isNew) {
                    // 그룹 존재 하지 않음, 새 그룹으로 추가.
                    PasswordGroup passwordGroup = new PasswordGroup();
                    passwordGroup.setGroupName(newGroupName);
                    passwordDatabase.addPasswordGroup(passwordGroup);
                }
                asyncResult.getBundle().putBoolean("isNew", isNew);

                int result = (int) passwordDatabase.insertPassword(password);
                password.setId(result);
                asyncResult.setData(password);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Password> asyncResult) {
                if (asyncResult.getBundle().getBoolean("isNew")) {
                    PasswordGroup passwordGroup = new PasswordGroup();
                    passwordGroup.setGroupName(asyncResult.getData().getGroupName());

                    for (OnPasswordGroupChangeListener onPasswordGroupListener : onPasswordGroupListeners) {
                        onPasswordGroupListener.onNewPasswordGroup(passwordGroup);
                    }
                }

                for (OnPasswordChangeListener onPasswordListener : onPasswordListeners) {
                    onPasswordListener.onNewPassword(asyncResult.getData());
                }
            }
        }.execute();
    }

    public void insertPasswordGroup(final PasswordGroup passwordGroup) {
        new AsyncSingleTask<PasswordGroup>() {
            @Override
            protected AsyncResult<PasswordGroup> doInBackground(AsyncResult<PasswordGroup> asyncResult) {
                String newGroupName = passwordGroup.getGroupName();

                boolean isNew = true;
                List<PasswordGroup> passwordGroups = passwordDatabase.getAllPasswordGroup();
                for (int i = 0; i < passwordGroups.size(); i++) {
                    PasswordGroup passwordGroup = passwordGroups.get(i);
                    if (passwordGroup.getGroupName().equals(newGroupName)) {
                        isNew = false;
                        break;
                    }
                }

                if (isNew) {
                    PasswordGroup passwordGroup = new PasswordGroup();
                    passwordGroup.setGroupName(newGroupName);
                    passwordDatabase.addPasswordGroup(passwordGroup);
                }
                asyncResult.getBundle().putBoolean("isNew", isNew);
                asyncResult.setData(passwordGroup);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<PasswordGroup> asyncResult) {
                if (asyncResult.getBundle().getBoolean("isNew")) {
                    for (OnPasswordGroupChangeListener onPasswordGroupListener : onPasswordGroupListeners) {
                        onPasswordGroupListener.onNewPasswordGroup(asyncResult.getData());
                    }
                }
            }
        }.execute();
    }

    public void deletePasswordgroup(final String passwordGroupName) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                int count = passwordDatabase.deletePasswordGroup(passwordGroupName);
                asyncResult.setResult(count);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                if (asyncResult.getResult() > 0) {
                    for (OnPasswordGroupChangeListener onPasswordGroupListener : onPasswordGroupListeners) {
                        onPasswordGroupListener.onDeletePasswordGroup(passwordGroupName);
                    }
                }
            }
        }.execute();
    }

    /**
     * 그룹 이름 매니저
     *
     * @param oldGroupName
     * @param newGroupName
     */
    public void updatePasswdGroupName(final String oldGroupName, final String newGroupName) {
        new AsyncSingleTask<Void>() {
            @Override
            protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
                passwordDatabase.updatePasswdGroupName(oldGroupName, newGroupName);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<Void> asyncResult) {
                for (OnPasswordGroupChangeListener onPasswordGroupListener : onPasswordGroupListeners) {
                    onPasswordGroupListener.onUpdateGroupName(oldGroupName, newGroupName);
                }
            }
        }.execute();
    }

    public void getAllPasswordGroup(final OnGetAllPasswordGroupCallback onGetAllPasswordGroupCallback) {
        new AsyncSingleTask<List<PasswordGroup>>() {
            @Override
            protected AsyncResult<List<PasswordGroup>> doInBackground(AsyncResult<List<PasswordGroup>> asyncResult) {
                List<PasswordGroup> list = passwordDatabase.getAllPasswordGroup();
                asyncResult.setData(list);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<List<PasswordGroup>> asyncResult) {
                onGetAllPasswordGroupCallback.onGetAllPasswordGroup(asyncResult.getData());
            }
        }.execute();
    }
}
