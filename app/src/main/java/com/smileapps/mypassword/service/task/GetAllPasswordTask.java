package com.smileapps.mypassword.service.task;

import java.util.List;

import com.smileapps.mypassword.database.PasswordDatabase;
import com.smileapps.mypassword.model.AsyncResult;
import com.smileapps.mypassword.model.AsyncSingleTask;
import com.smileapps.mypassword.model.Password;
import com.smileapps.mypassword.service.OnGetAllPasswordCallback;

public class GetAllPasswordTask extends AsyncSingleTask<List<Password>> {
    private PasswordDatabase passwordDatabase;
    private OnGetAllPasswordCallback onGetAllPasswordCallback;
    private String groupName;

    public GetAllPasswordTask(PasswordDatabase passwordDatabase, OnGetAllPasswordCallback onGetAllPasswordCallback,
                              String groupName) {
        this.passwordDatabase = passwordDatabase;
        this.onGetAllPasswordCallback = onGetAllPasswordCallback;
        this.groupName = groupName;
    }

    @Override
    protected AsyncResult<List<Password>> doInBackground(AsyncResult<List<Password>> asyncResult) {
        List<Password> passwords;
        if (groupName == null)
            passwords = passwordDatabase.getAllPassword();
        else
            passwords = passwordDatabase.getAllPasswordByGroupName(groupName);
        asyncResult.setData(passwords);
        return asyncResult;
    }

    @Override
    protected void runOnUIThread(AsyncResult<List<Password>> asyncResult) {
        onGetAllPasswordCallback.onGetAllPassword(groupName, asyncResult.getData());
    }
}
