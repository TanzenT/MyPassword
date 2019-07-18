package com.smileapps.mypassword.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.smileapps.mypassword.R;
import com.smileapps.mypassword.service.Mainbinder;
import cn.zdx.lib.annotation.FindViewById;
import cn.zdx.lib.annotation.ViewFinder;
import cn.zdx.lib.annotation.XingAnnotationHelper;

/**
 * 그룹 이름 수정
 *
 * @author zeus8502
 */
public class UpdatePasswdGroupNameDialog extends Dialog {
    @FindViewById(R.id.add_passwrdGroup_editview)
    private EditText editText;

    private Mainbinder mainbinder;

    /** 기존 그룹 이름 */
    private String oldGroupName;

    @FindViewById(R.id.add_password_group_cancle_btn)
    private View cancleButton;

    @FindViewById(R.id.add_password_group_sure_btn)
    private View sureButton;

    @FindViewById(R.id.container)
    private View container;

    private View.OnClickListener onCancleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };

    private View.OnClickListener onSureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String name = editText.getText().toString().trim();
            if (!name.equals("") && !name.equals(oldGroupName)) {
                mainbinder.updatePasswdGroupName(oldGroupName, name);
            }
            dismiss();
        }
    };

    public UpdatePasswdGroupNameDialog(Context context, String oldGroupName, Mainbinder mainbinder) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        this.mainbinder = mainbinder;
        this.oldGroupName = oldGroupName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_password_group_name);
        XingAnnotationHelper.findView(this, ViewFinder.create(this));
        initView();
    }

    private void initView() {
        cancleButton.setOnClickListener(onCancleClickListener);
        container.setOnClickListener(onCancleClickListener);
        sureButton.setOnClickListener(onSureClickListener);
        editText.requestFocus();
    }
}
